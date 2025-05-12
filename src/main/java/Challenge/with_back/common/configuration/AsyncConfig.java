package Challenge.with_back.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig
{
    // 이메일 스레드 풀
    @Bean(name = "emailThreadPool")
    public Executor getEmailAsyncExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("email-thread-");
        executor.initialize();

        return executor;
    }

    // 페이즈 참여 데이터 갱신 스레드 풀
    @Bean(name = "participatePhaseThreadPool")
    public Executor getParticipatePhaseAsyncExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(1000);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("participate-phase-thread-");
        executor.initialize();

        return executor;
    }
}
