package org.example.merchant_ai_operation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling   //用定时任务功能。
public class RabbitMqConfig {

    /*
    * 促销变量
    * */
    public static final String PROMOTION_EXCHANGE = "ai.commerce.promotion.exchange";

    public static final String PROMOTION_ORDER_CREATE_QUEUE = "ai.commerce.promotion.order.create.queue";

    public static final String PROMOTION_ORDER_CREATE_KEY = "promotion.order.create";

    /*
    * 异步订单变量
    * */
    //exchange机
    public static final String ORDER_EXCHANGE =  "ai.commerce.order.exchange";

    //延迟队列
    public static final String ORDER_DELAY_QUEUE = "ai.commerce.order.delay.queue";

    //关闭队列
    public static final String ORDER_CLOSE_QUEUE = "ai.commerce.order.close.queue";

    //失败/死信队列
    public static final String ORDER_FAILED_QUEUE = "ai.commerce.order.failed.queue";

    //BINDING规则
    //创建订单队列
    public static final String ORDER_CREATED_KEY = "order.created";
    //关闭订单队列
    public static final String ORDER_CLOSE_KEY = "order.close";
    //失败消息队列
    public static final String ORDER_FAILED_KEY = "order.failed";

    @Bean
    //创建一个交换机
    public DirectExchange orderExchange(){
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    /*
    * 订单创建后先进入这里，等待 30 分钟。
    */

    @Bean
    public Queue orderDelayQueue(){
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .withArgument("x-message-ttl", 30 * 60 * 1000)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_CLOSE_KEY)
                .build();
    }


    /*
     * 延迟消息到期后,进入这里关闭订单
     */

    @Bean
    public Queue orderCloseQueue() {
        return QueueBuilder.durable(ORDER_CLOSE_QUEUE)
                //消息过期后，不直接消失，而是转发到：ORDER_EXCHANGE
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                //转发到交换机后，使用：ORDER_CLOSE_KEY = "order.close" 作为路由键。
                .withArgument("x-dead-letter-routing-key", ORDER_FAILED_KEY)
                .build();
    }

    /*
     * 处理失败的消息进入这里，方便后续排查。
     */

    @Bean
    public Queue orderFailedQueue() {
        return QueueBuilder.durable(ORDER_FAILED_QUEUE)
                .build();
    }

    @Bean
    public Binding orderDelayBinding(
            Queue orderDelayQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(orderDelayQueue)
                .to(orderExchange)
                .with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding orderCloseBinding(
            Queue orderCloseQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(orderCloseQueue)
                .to(orderExchange)
                .with(ORDER_CLOSE_KEY);
    }

    @Bean
    public Binding orderFailedBinding(
            Queue orderFailedQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(orderFailedQueue)
                .to(orderExchange)
                .with(ORDER_FAILED_KEY);
    }

    /*
    * 促销变量Binging
    * */
    @Bean
    public DirectExchange promotionExchange() {
        return new DirectExchange(PROMOTION_EXCHANGE, true, false);
    }

    @Bean
    public Queue promotionOrderCreateQueue() {
        return QueueBuilder.durable(PROMOTION_ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Binding promotionOrderCreateBinding(
            Queue promotionOrderCreateQueue,
            DirectExchange promotionExchange
    ) {
        return BindingBuilder
                .bind(promotionOrderCreateQueue)
                .to(promotionExchange)
                .with(PROMOTION_ORDER_CREATE_KEY);
    }
}
