package Challenge.with_back.domain.invite_challenge.service;

import Challenge.with_back.common.entity.rdbms.*;
import Challenge.with_back.common.enums.ChallengeRole;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.common.repository.rdbms.*;
import Challenge.with_back.domain.account.service.AccountService;
import Challenge.with_back.domain.notification.InviteChallengeNotificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final ParticipateChallengeRepository participateChallengeRepository;
    private final FriendRepository friendRepository;
    private final InviteChallengeRepository inviteChallengeRepository;

    private final AccountService accountService;

    private final InviteChallengeNotificationFactory inviteChallengeNotificationFactory;

    /// 서비스

    // 챌린지 초대
    @Transactional
    public void inviteChallenge(User sender, List<Long> userIdList, Long challengeId)
    {
        /// 챌린지 조회

        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHALLENGE_NOT_FOUND, challengeId));

        /// 예외 처리
        /// 1. 초대하는 사용자가 해당 챌린지에 참여하고 있는지 확인
        /// 2. 초대하는 사용자가 챌린지의 관리자 이상의 권한을 가지고 있는지 확인

        // 챌린지 참여 데이터 조회
        ParticipateChallenge participateChallenge = participateChallengeRepository.findByUserAndChallenge(sender, challenge)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHALLENGE_NOT_FOUND, null));

        // 초대하는 사용자가 챌린지의 관리자 이상의 권한을 가지고 있는지 확인
        if(participateChallenge.getChallengeRole() == ChallengeRole.USER) {
            throw new CustomException(CustomExceptionCode.LOW_CHALLENGE_ROLE, null);
        }

        /// 초대 사용자 리스트 조회

        // 초대 사용자 ID 리스트를 초대 사용자 리스트로 매핑
        List<User> userList = mapUserIdListToUserList(sender, userIdList);

        /// 이미 해당 챌린지에 참여하고 있는 사용자 필터링

        // 이미 해당 챌린지에 참여하고 있는 사용자 필터링
        userList = userList.stream()
                .filter(user -> {
                    return participateChallengeRepository.findByUserAndChallenge(user, challenge).isEmpty();
                })
                .toList();

        /// 챌린지에 초대 가능한 인원수가 초대할 사용자의 인원수를 수용할 수 있는지 확인

        // 챌린지에 초대 가능한 인원수가 초대할 사용자의 인원수를 수용할 수 있는지 확인
        if(userList.size() + participateChallengeRepository.countAllByChallenge(challenge) > challenge.getMaxParticipantCount()) {
            throw new CustomException(CustomExceptionCode.FULL_CHALLENGE, null);
        }

        /// 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송

        // 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송
        inviteChallenge(sender, userList, challenge);
    }

    /// 공통 로직

    // 초대 사용자 ID 리스트를 초대 사용자 리스트로 매핑
    @Transactional(readOnly = true)
    public List<User> mapUserIdListToUserList(User sender, List<Long> userIdList)
    {
        // 초대 사용자 리스트
        List<User> userList = userRepository.findAllById(userIdList);

        // 필터링
        return userList.stream().filter(user -> {

                    // 초대한 사용자가 최대 개수로 챌린지를 참여하고 있는지 확인
                    if(accountService.isParticipatingInMaxChallenges(user)) {
                        return false;
                    }

                    // 초대한 사용자와 친구 사이인지 확인
                    return friendRepository.findByUser1IdAndUser2Id(sender.getId(), user.getId()).isPresent();

                }).toList();
    }

    // 챌린지 초대 데이터 생성 및 챌린지 초대 알림 전송
    @Transactional
    public void inviteChallenge(User sender, List<User> userList, Challenge challenge)
    {
        /// 기존에 이미 초대를 받은 사용자는 챌린지 초대 데이터와 알림의 생성 날짜만 갱신

        // 새롭게 챌린지에 초대된 사용자 리스트
        List<User> newInvitedUserList = new ArrayList<>();

        // 기존에 이미 초대를 받은 사용자는 알림 생성 날짜만 갱신
        userList.forEach(user -> {

            // 챌린지 초대 데이터 조회
            Optional<InviteChallenge> inviteChallengeOptional = inviteChallengeRepository.findBySenderIdAndReceiverIdAndChallengeId(
                    sender.getId(),
                    user.getId(),
                    challenge.getId()
            );

            // 챌린지 초대 데이터가 존재하는 경우, 챌린지 초대 데이터와 알림의 생성 날짜만 갱신
            // 챌린지 초대 데이터가 존재하지 않는 경우, 새롭게 챌린지에 초대된 사용자 리스트에 추가
            if(inviteChallengeOptional.isPresent()) {

                // 챌린지 초대 데이터 생성 날짜 갱신
                inviteChallengeOptional.get().renew();

                // 알림 생성 날짜 갱신
                inviteChallengeOptional.get().getNotification().renew();

            } else {
                newInvitedUserList.add(user);
            }
        });

        // 챌린지 초대 데이터 생성
        List<InviteChallenge> inviteChallengeList = newInvitedUserList.stream().map(user -> {

            // 챌린지 초대 알림 생성
            Notification notification = inviteChallengeNotificationFactory.createNotification(user, sender.getId());

            // 챌린지 초대 데이터 생성
            return InviteChallenge.builder()
                    .sender(sender)
                    .receiver(user)
                    .challenge(challenge)
                    .notification(notification)
                    .build();

        }).toList();

        // 챌린지 초대 데이터 저장
        inviteChallengeRepository.saveAll(inviteChallengeList);
    }
}
