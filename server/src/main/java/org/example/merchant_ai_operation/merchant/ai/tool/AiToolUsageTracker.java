package org.example.merchant_ai_operation.merchant.ai.tool;


import org.example.merchant_ai_operation.merchant.ai.exception.AiChatException;
import org.springframework.stereotype.Component;

@Component
public class AiToolUsageTracker {
    //最大调用工具次数
    private static final int MAX_TOOL_CALLS_PER_CHAT = 1;

    private final ThreadLocal<Boolean> businessDataUsed = new ThreadLocal<>();
    private final ThreadLocal<Integer> toolCallCount = new ThreadLocal<>();

    //将值绑定线程
    public Scope openScope() {
        businessDataUsed.set(false);
        toolCallCount.set(0);
        return new Scope(this);
    }

    /**
     * 一个静态内部类
     * */
    public static final class Scope implements AutoCloseable {

        private final AiToolUsageTracker tracker;
        private boolean closed;

        //构造器
        private Scope(AiToolUsageTracker tracker) {
            this.tracker = tracker;
        }

        public boolean businessDataUsed() {
            return tracker.isBusinessDataUsed();
        }

        @Override
        public void close() {
            if (!closed) {
                tracker.clear();
                closed = true;
            }
        }
    }

    /**
    * 只由工具类在查询成功后调用 => 只要用了工具就来标记这个为TRUE
    */
    public void markBusinessDataUsed() {
        Integer currentCount = toolCallCount.get();

        //抓住异常,即使以后异步或 SSE 改坏线程上下文，也会明确失败，不会因为空指针被误包装成普通服务不可用
        if (currentCount == null) {
            throw new IllegalStateException("AI 工具调用必须在对话作用域中执行");
        }

        int nextCount = currentCount + 1;

        if (nextCount > MAX_TOOL_CALLS_PER_CHAT) {
            throw new AiChatException(
                    400,
                    "一次对话最多调用一次经营查询工具",
                    null
            );
        }

        toolCallCount.set(nextCount);
        businessDataUsed.set(true);
    }

    /**
    * 判断是否方法被调用?(是否为true?)
    * */
    private boolean isBusinessDataUsed() {
        //注意:有可能businessDataUsed.get()得到的是null,所以用boolean.TRUE来防止空指针
        return Boolean.TRUE.equals(businessDataUsed.get());
    }

    /**
    * 把当前线程里这份变量完全移除，防止服务器线程复用时遗留状态
    * */
    private void clear() {
        businessDataUsed.remove();
        toolCallCount.remove();
    }


}
