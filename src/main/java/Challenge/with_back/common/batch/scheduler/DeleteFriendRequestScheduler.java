package Challenge.with_back.common.batch.scheduler;

import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class DeleteFriendRequestScheduler
{
    private final JobLauncher jobLauncher;
    private final Job deleteFriendRequestJob;

    // 매일 오전 5시에 실행
    @Scheduled(cron = "* * 5 * * *")
    public void run()
    {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("runTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                    .toJobParameters();

            jobLauncher.run(deleteFriendRequestJob, jobParameters);
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.DELETE_FRIEND_REQUEST_SCHEDULER_ERROR, e.getMessage());
        }
    }
}
