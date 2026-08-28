-- KEYS[1]：活动商品库存
-- promotion:item:{itemId}:stock:v1
--
-- KEYS[2]：用户已购买数量
-- promotion:item:{itemId}:user:{consumerId}:v1
--
-- KEYS[3]：本次补偿幂等标记
-- promotion:item:{itemId}:compensation:{reservationId}:v1
--
-- ARGV[1]：需要恢复的数量
-- ARGV[2]：reservationId

-- 已经补偿过，不能重复恢复库存
local existingMarker = redis.call('GET', KEYS[3])
if existingMarker then
    return {2, existingMarker}
end

local quantity = tonumber(ARGV[1])
if not quantity or quantity <= 0 then
    return {-1, 'INVALID_QUANTITY'}
end

-- 任一关键 Key 缺失，都不能盲目补偿
if redis.call('EXISTS', KEYS[1]) == 0
        or redis.call('EXISTS', KEYS[2]) == 0 then
    return {-2, 'REDIS_KEY_NOT_FOUND'}
end

-- 恢复活动库存
redis.call('INCRBY', KEYS[1], quantity)

-- 回退用户限购数量
local remainingUserQuantity =
        redis.call('DECRBY', KEYS[2], quantity)

-- 防止异常数据导致用户数量变成负数
if remainingUserQuantity < 0 then
    redis.call('SET', KEYS[2], 0)
    remainingUserQuantity = 0
end

-- 写入补偿幂等标记
redis.call('SET', KEYS[3], ARGV[2])

return {1, tostring(remainingUserQuantity)}