package org.example.merchant_ai_operation.merchant.ai.guard;


import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 商家 AI 调用保护器。
 *
 * <p>当前提供两层进程内保护：</p>
 * <ul>
 *     <li>全局最多同时进行 4 次模型调用；</li>
 *     <li>同一个商家用户一分钟最多发起 10 次调用。</li>
 * </ul>
 *
 * <p>这是单实例限流。将来后端部署多个实例时，需要改为
 * Redis 或网关级分布式限流。</p>
 */
@Component
public class AiChatGuard {

    private static final int MAX_CONCURRENT_REQUESTS = 4;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    /**
     * 记录单个用户当前限流窗口的状态。
     *
     * @param windowStartedAtMillis 当前一分钟窗口的开始时间
     * @param requestCount          当前窗口内已经接受的请求数量
     */
    private record UserWindow(long windowStartedAtMillis, int requestCount) {}

    private final Semaphore concurrency = new Semaphore(MAX_CONCURRENT_REQUESTS, true);
    private final ConcurrentHashMap<Long, UserWindow> userWindows = new ConcurrentHashMap<>();
    private final Clock clock;

    /**
     * 创建 AI 调用保护器。
     *
     * @param clock 提供当前时间；生产环境使用系统时间，
     *              自动测试可传入固定或可推进的时间
     */
    public AiChatGuard(Clock clock) {this.clock = clock;}

    /**
     * 尝试为当前用户取得一次 AI 调用资格。
     *
     * <p>先检查全局并发数量，再检查当前用户的一分钟调用频率。
     * 两项都通过时返回许可证；调用方必须关闭许可证，以归还并发名额。</p>
     *
     * @param userId 当前登录商家用户 ID，必须来自服务端安全上下文
     * @return 本次 AI 调用持有的并发许可证
     * @throws AiChatException 全局并发已满或当前用户请求频率超限时抛出
     */
    public Permit acquire(Long userId) {
        if (!concurrency.tryAcquire()) {
            throw new AiChatException(
                    429,
                    "当前 AI 请求较多，请稍后重试",
                    null
            );
        }

        if (!tryAcquireRateLimit(userId)) {
            concurrency.release();

            throw new AiChatException(
                    429,
                    "AI 请求过于频繁，请一分钟后再试",
                    null
            );
        }

        return new Permit(concurrency);
    }

    /**
     * 一次 AI 调用持有的并发许可证。
     *
     * <p>实现 {@link AutoCloseable} 后，可以配合 try-with-resources 使用，
     * 确保模型调用无论成功还是失败，最终都会归还 Semaphore 名额。</p>
     */
    public static final class Permit implements AutoCloseable {

        private final Semaphore concurrency;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private Permit(Semaphore concurrency) {
            this.concurrency = concurrency;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                concurrency.release();
            }
        }
    }

    /**
     * 原子检查并更新当前用户的一分钟请求窗口。
     *
     * <p>首次请求或旧窗口已过期时创建新窗口；窗口未过期且
     * 请求数低于上限时递增计数；达到上限后保持原窗口并拒绝请求。</p>
     *
     * @param userId 当前登录商家用户 ID
     * @return true 表示本次请求未超过频率限制，false 表示已经超限
     */
    private boolean tryAcquireRateLimit(Long userId){
        long now = clock.millis();
        AtomicBoolean allowed = new AtomicBoolean(true);

        userWindows.compute(userId,(key,currentWindow) ->{
            if (currentWindow == null || now - currentWindow.windowStartedAtMillis() >= 60_000) {
                return new UserWindow(now, 1);
            }
            if (currentWindow.requestCount() >= MAX_REQUESTS_PER_MINUTE) {
                allowed.set(false);
                return currentWindow;
            }
            return new UserWindow(
                    currentWindow.windowStartedAtMillis(),
                    currentWindow.requestCount() + 1
            );

        });
        return allowed.get();
    }

}