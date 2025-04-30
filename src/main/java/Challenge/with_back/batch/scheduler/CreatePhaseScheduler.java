package Challenge.with_back.batch.scheduler;

import Challenge.with_back.batch.configuration.CreatePhaseConfig;
import Challenge.with_back.response.exception.CustomException;
import Challenge.with_back.response.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreatePhaseScheduler
{
    private final JobLauncher jobLauncher;
    private final Job createPhaseJob;

    // 매일 오전 4시에 실행
    @Scheduled(cron = "30 * * * * *")
    public void run()
    {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("runTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                    .toJobParameters();

            jobLauncher.run(createPhaseJob, jobParameters);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(CustomExceptionCode.SCHEDULER_ERROR, e.getMessage());
        }
    }
}
