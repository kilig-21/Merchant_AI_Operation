-- KEYS[1]：活动商品规则 Hash
-- promotion:item:{itemId}:rules:v{version}
--
-- KEYS[2]：活动商品剩余库存 String
-- promotion:item:{itemId}:stock:v{version}
--
-- KEYS[3]：当前用户已获得的数量 String
-- promotion:item:{itemId}:user:{consumerId}:v{version}
--
-- KEYS[4]：本次请求的幂等记录 String，值为 reservationId
-- promotion:item:{itemId}:request:{requestKey}:v{version}
--
-- ARGV[1]：服务器当前时间戳（毫秒）
-- ARGV[2]：购买数量
-- ARGV[3]：reservationId
--
-- 返回码：
-- 1：抢购资格创建成功
-- 2：重复请求，返回已有 reservationId
-- -1：活动未预热或不存在
-- -2：活动未开始
-- -3：活动已结束
-- -4：库存不足/售罄
-- -5：超过单用户限购

-- 返回结构：{返回码, reservationId}
-- 错误时 reservationId 为空字符串。

-- 同一个请求重试：直接返回第一次获得的资格，不重复扣库存。
local existingReservationId = redis.call('GET', KEYS[4])
if existingReservationId then
    return {2, existingReservationId}
end

-- 活动规则尚未预热到 Redis，暂时不允许抢购。
if redis.call('EXISTS', KEYS[1]) == 0 then
    return {-1, ''}
end


--[[ 这一段的作用是：防止用户在不该抢的时候拿到资格并扣掉活动库存
重复点击？       → 返回第一次的资格
活动已预热？     → 未预热则拒绝
现在到开始时间？ → 未开始则拒绝
现在未到结束？   → 已结束则拒绝
后面才会：限购 → 扣库存 → 写资格
 ]]
local startAt = tonumber(redis.call('HGET', KEYS[1], 'startAt'))
local endAt = tonumber(redis.call('HGET', KEYS[1], 'endAt'))
local now = tonumber(ARGV[1])

-- 规则字段缺失也视为未预热，避免拿不完整配置继续扣库存。
if not startAt or not endAt then
    return {-1, ''}
end

if now < startAt then
    return {-2, ''}
end

if now >= endAt then
    return {-3, ''}
end


--[[这一段的链路是
 KEYS[3] 不存在 → 当作已购买 0 件
 已有资格       → 取已有购买数量
 已购 + 本次 ≤ 限购 → 可以继续检查库存
 已购 + 本次 > 限购 → 返回 -5，不扣库存
 ]]
local limitPerUser = tonumber(redis.call('HGET', KEYS[1], 'limitPerUser'))
local quantity = tonumber(ARGV[2])
local purchasedQuantity = tonumber(redis.call('GET', KEYS[3]) or '0')

-- 限购规则或购买数量不合法时，不能继续预扣。
if not limitPerUser or not quantity or quantity <= 0 then
    return {-1, ''}
end

-- 同一用户可以换 requestKey 重试，但不能绕开活动限购。
if purchasedQuantity + quantity > limitPerUser then
    return {-5, ''}
end

--[[ 库存检查和扣减:

活动剩余库存  -= 本次购买量
该用户已购数 += 本次购买量
请求幂等 Key  = reservationId
返回成功资格  = reservationId

 ]]
local availableStock = tonumber(redis.call('GET', KEYS[2]) or '-1')

-- 库存 Key 缺失或库存不够，都不能获得资格。
if availableStock < quantity then
    return {-4, ''}
end

-- Lua 在 Redis 内顺序、原子执行：下面三次写入不会被其他抢购请求插进来。
redis.call('DECRBY', KEYS[2], quantity)
redis.call('INCRBY', KEYS[3], quantity)
redis.call('SET', KEYS[4], ARGV[3])

return {1, ARGV[3]}