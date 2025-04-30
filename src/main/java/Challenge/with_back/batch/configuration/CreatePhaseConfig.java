package Challenge.with_back.batch.configuration;

import Challenge.with_back.batch.dto.CreatePhaseDto;
import Challenge.with_back.entity.rdbms.Challenge;
import Challenge.with_back.entity.rdbms.ParticipateChallenge;
import Challenge.with_back.entity.rdbms.ParticipatePhase;
import Challenge.with_back.entity.rdbms.Phase;
import Challenge.with_back.repository.rdbms.ChallengeRepository;
import Challenge.with_back.repository.rdbms.ParticipateChallengeRepository;
import Challenge.with_back.repository.rdbms.ParticipatePhaseRepository;
import Challenge.with_back.repository.rdbms.PhaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CreatePhaseConfig
{
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ChallengeRepository challengeRepository;
    private final PhaseRepository phaseRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final ParticipatePhaseRepository participatePhaseRepository;

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
                .<Challenge, CreatePhaseDto>chunk(100, transactionManager)
                .reader(createPhaseReader())
                .processor(createPhaseProcessor())
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

    // 페이즈 생성 processor
    @Bean
    public ItemProcessor<Challenge, CreatePhaseDto> createPhaseProcessor()
    {
        return challenge -> {

            // 생성한 페이즈 개수가 충분하다면 건너뛰기
            if(challenge.calcCurrentPhaseNumber() + 10 <= challenge.getCountPhase()) {
                return null;
            }

            // 챌린지의 페이즈 개수 증가
            challenge.increaseCountPhase();

            // 페이즈 시작 날짜 및 종료 날짜 계산
            LocalDate startDate = challenge.calcPhaseStartDate(challenge.getCountPhase());
            LocalDate endDate = challenge.getUnit().calcPhaseEndDate(startDate);

            // 페이즈 생성
            Phase phase = Phase.builder()
                    .challenge(challenge)
                    .name(challenge.getCountPhase() + "번째 페이즈")
                    .description("")
                    .number(challenge.getCountPhase())
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            // 챌린지 참여 정보 리스트
            List<ParticipateChallenge> participateChallengeList = participateChallengeRepository.findAllByChallengeOrderByCreatedAtDesc(phase.getChallenge());

            // 페이즈 참여 정보 리스트
            List<ParticipatePhase> participatePhaseList = participateChallengeList.stream()
                    .map(participateChallenge -> {
                        return ParticipatePhase.builder()
                                .user(participateChallenge.getUser())
                                .phase(phase)
                                .currentCount(0)
                                .isExempt(false)
                                .comment("")
                                .countEvidencePhoto(0)
                                .build();
                    })
                    .toList();

            return CreatePhaseDto.builder()
                    .challenge(challenge)
                    .phase(phase)
                    .participatePhases(participatePhaseList)
                    .build();
        };
    }

    // 페이즈 생성 writer
    @Bean
    public ItemWriter<CreatePhaseDto> createPhaseWriter()
    {
        return createPhaseDtoList -> {

            // 저장할 데이터
            List<Challenge> challenges = new ArrayList<>();
            List<Phase> phases = new ArrayList<>();
            List<ParticipatePhase> participatePhases = new ArrayList<>();

            // 각각의 dto에서 저장할 데이터 추출
            for(CreatePhaseDto createPhaseDto : createPhaseDtoList)
            {
                challenges.add(createPhaseDto.getChallenge());
                phases.add(createPhaseDto.getPhase());
                participatePhases.addAll(createPhaseDto.getParticipatePhases());
            }

            // 저장
            challengeRepository.saveAll(challenges);
            phaseRepository.saveAll(phases);
            participatePhaseRepository.saveAll(participatePhases);
        };
    }
}
