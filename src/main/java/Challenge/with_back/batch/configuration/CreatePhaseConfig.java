package Challenge.with_back.batch.configuration;

import Challenge.with_back.domain.challenge.util.ChallengeUtil;
import Challenge.with_back.entity.rdbms.Challenge;
import Challenge.with_back.repository.rdbms.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CreatePhaseConfig
{
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ChallengeRepository challengeRepository;

    private final ChallengeUtil challengeUtil;

    /// Job

    // 페이즈 자동 생성 Job
    @Bean
    public Job createPhaseJob()
    {
        return new JobBuilder("createPhaseJob", jobRepository)
                .start(createPhaseStep())
                .build();
    }

    // 페이즈 생성 Step
    @Bean
    public Step createPhaseStep()
    {
        return new StepBuilder("createPhaseStep", jobRepository)
                .<Challenge, Challenge>chunk(100, transactionManager)
                .reader(createPhaseReader())
                .writer(createPhaseWriter())
                .build();
    }

    // 페이즈 생성 reader
    @Bean
    public ItemReader<Challenge> createPhaseReader()
    {
        // 모든 챌린지 읽기
        return new RepositoryItemReaderBuilder<Challenge>()
                .repository(challengeRepository)
                .methodName("findAll")
                .pageSize(100)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .name("createPhaseReader")
                .build();
    }

    // 페이즈 생성 writer
    @Bean
    public ItemWriter<Challenge> createPhaseWriter()
    {
        return challengeList -> {
            for(Challenge challenge : challengeList)
            {
                // 생성한 페이즈 개수가 충분하다면 건너뛰기
                if(challenge.calcCurrentPhaseNumber() + 9 <= challenge.getCountPhase()) {
                    continue;
                }

                challengeUtil.createPhases(challenge, challenge.calcCurrentPhaseNumber() + 10 - challenge.getCountPhase());
            }
        };
    }
}
