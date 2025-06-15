package Challenge.with_back.domain.invite_challenge.service;

import Challenge.with_back.common.entity.rdbms.Challenge;
import Challenge.with_back.common.entity.rdbms.InviteChallenge;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.ChallengeRepository;
import Challenge.with_back.common.repository.rdbms.InviteChallengeRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import Challenge.with_back.domain.notification.InviteChallengeNotificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteChallengeService
{
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final InviteChallengeRepository inviteChallengeRepository;

    private final InviteChallengeNotificationFactory inviteChallengeNotificationFactory;

    // 챌린지 초대
    public void inviteChallenge(User sender, List<User> userList, Challenge challenge)
    {
        /// 챌린지 초대 데이터 생성

        // 챌린지 초대 데이터 생성
        List<InviteChallenge> inviteChallengeList = userList.stream().map(user -> {
            return InviteChallenge.builder()
                    .sender(sender)
                    .receiver(user)
                    .challenge(challenge)
                    .build();
        }).toList();

        // 챌린지 초대 데이터 저장
        inviteChallengeRepository.saveAll(inviteChallengeList);

        /// 초대한 사용자들에게 챌린지 초대 알림 전송

        // 각각의 초대한 사용자에 대해, 챌린지 초대 알림 생성
        userList.forEach(user -> {
            inviteChallengeNotificationFactory.createNotification(user, challenge.getId());
        });
    }
}
