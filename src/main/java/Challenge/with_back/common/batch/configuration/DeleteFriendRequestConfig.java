package Challenge.with_back.common.batch.configuration;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import Challenge.with_back.common.repository.rdbms.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class DeleteFriendRequestConfig
{
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final FriendRequestRepository friendRequestRepository;

    ///  Job

    // 친구 요청 데이터 자동 삭제 Job
    @Bean
    public Job deleteFriendRequestJob()
    {
        return new JobBuilder("deleteFriendRequestJob", jobRepository)
                .start(deleteFriendRequestStep())
                .build();
    }

    // 친구 요청 데이터 삭제 Step
    @Bean
    public Step deleteFriendRequestStep()
    {
        return new StepBuilder("deleteFriendRequestStep", jobRepository)
                .<FriendRequest, FriendRequest>chunk(100, transactionManager)
                .reader(deleteFriendRequestReader())
                .writer(deleteFriendRequestWriter())
                .build();
    }

    // 친구 요청 데이터 삭제 reader
    @Bean
    @StepScope
    public ItemReader<FriendRequest> deleteFriendRequestReader()
    {
        // 친구 요청 데이터 읽기
        return new RepositoryItemReaderBuilder<FriendRequest>()
                .repository(friendRequestRepository)
                .methodName("findByCreatedAtBefore")
                .arguments(List.of(LocalDateTime.now().minusDays(30)))
                .pageSize(100)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .name("deleteFriendRequestReader")
                .build();
    }

    // 친구 요청 데이터 삭제 writer
    @Bean
    public ItemWriter<FriendRequest> deleteFriendRequestWriter() {
        return friendRequests -> friendRequestRepository.deleteAllInBatch(new ArrayList<>(friendRequests.getItems()));
    }
}
