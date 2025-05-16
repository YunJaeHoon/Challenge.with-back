package Challenge.with_back.common.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UpdateParticipatePhaseConfig
{
    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_QUEUE_NAME}")
    private String queueName;

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_ROUTING_KEY}")
    private String routingKey;

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_DLX_NAME}")
    private String deadLetterExchangeName;

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_DLQ_NAME}")
    private String deadLetterQueueName;

    @Value("${RABBITMQ_UPDATE_PARTICIPATE_PHASE_DLQ_ROUTING_KEY}")
    private String deadLetterRoutingKey;

    // 페이즈 참여 정보 수정 요청 메시지 DirectExchange 빈 생성
    @Bean
    public DirectExchange updateParticipatePhaseExchange() {
        return new DirectExchange(exchangeName);
    }

    // 페이즈 참여 정보 수정 요청 메시지 Queue 빈 생성
    @Bean
    public Queue updateParticipatPhaseQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    // 페이즈 참여 정보 수정 요청 메시지 DirectExchange-Queue 바인딩
    // 라우팅 키를 사용하여 Binding 빈 생성
    @Bean
    public Binding updateParticipatePhaseBinding() {
        return BindingBuilder
                .bind(updateParticipatPhaseQueue())
                .to(updateParticipatePhaseExchange())
                .with(routingKey);
    }

    // 페이즈 참여 정보 수정 요청 메시지 DLX 빈 생성
    @Bean
    public DirectExchange updateParticipatePhaseDLX() {
        return new DirectExchange(deadLetterExchangeName);
    }

    // 페이즈 참여 정보 수정 요청 메시지 DLQ 빈 생성
    @Bean
    public Queue updateParticipatePhaseDLQ() {
        return new Queue(deadLetterQueueName);
    }

    // 페이즈 참여 정보 수정 요청 메시지 DLX-DLQ 바인딩
    // 라우팅 키를 사용하여 Binding 빈 생성
    @Bean
    public Binding updateParticipatePhaseDLQBinding() {
        return BindingBuilder
                .bind(updateParticipatePhaseDLQ())
                .to(updateParticipatePhaseDLX())
                .with(deadLetterRoutingKey);
    }
}
