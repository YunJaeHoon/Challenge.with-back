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
    @Value("${RABBITMQ_HOST}")
    private String host;

    @Value("${RABBITMQ_PORT}")
    private int port;

    @Value("${RABBITMQ_USERNAME}")
    private String username;

    @Value("${RABBITMQ_PASSWORD}")
    private String password;

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

    // RabbitMQ 연결을 위한 ConnectionFactory 빈 생성
    @Bean
    public ConnectionFactory connectionFactory()
    {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        return connectionFactory;
    }

    // RabbitTemplate 빈 생성
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory)
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // JSON 형식의 메시지와 객체 간의 직렬화 및 역직렬화가 가능하도록 설정
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());

        return rabbitTemplate;
    }

    // Jackson 라이브러리를 통해 메시지를 JSON 형식으로 변환하는 MessageConverter 빈 생성
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 재시도 정책 비활성화
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory)
    {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 예외 발생 시 메시지를 큐에 다시 넣지 않음
        factory.setDefaultRequeueRejected(false);

        // JSON 형식의 메시지와 객체 간의 직렬화 및 역직렬화가 가능하도록 설정
        factory.setMessageConverter(jackson2JsonMessageConverter());

        return factory;
    }
}
