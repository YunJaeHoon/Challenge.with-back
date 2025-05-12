package Challenge.with_back.common.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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
public class RabbitMQConfig
{
    @Value("${RABBITMQ_HOST}")
    private String host;

    @Value("${RABBITMQ_PORT}")
    private int port;

    @Value("${RABBITMQ_USERNAME}")
    private String username;

    @Value("${RABBITMQ_PASSWORD}")
    private String password;

    @Value("${RABBITMQ_QUEUE_NAME}")
    private String queueName;

    @Value("${RABBITMQ_EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${RABBITMQ_ROUTING_KEY}")
    private String routingKey;

    // 지정된 이름으로 Queue 빈 생성
    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    // 지정된 이름으로 DirectExchange 빈 생성
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    // 주어진 Queue, DirectExchange를 바인딩
    // 라우팅 키를 사용하여 Binding 빈 생성
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
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
