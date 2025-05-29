package Challenge.with_back.domain.friend.service;

import Challenge.with_back.common.entity.rdbms.FriendRequest;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.FriendBlockRepository;
import Challenge.with_back.common.repository.rdbms.FriendRepository;
import Challenge.with_back.common.repository.rdbms.FriendRequestRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.common.response.exception.CustomExceptionCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService
{
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendBlockRepository friendBlockRepository;
    private final UserRepository userRepository;

    // 친구 요청
    @Transactional
    public void sendFriendRequest(User sender, Long receiverId)
    {
        // 친구 요청을 보낸 사람과 받는 사람이 동일한지 확인
        if(sender.getId().equals(receiverId)) {
            throw new CustomException(CustomExceptionCode.SAME_SENDER_AND_RECEIVER, receiverId);
        }

        // 친구 요청을 받는 사람이 존재하는지 확인
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, receiverId));

        // 기존에 존재하던 친구 요청 데이터
        Optional<FriendRequest> friendRequestOptional = friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), receiverId);

        // 기존에 친구 요청 데이터가 존재한다면, 해당 데이터를 삭제
        friendRequestOptional.ifPresent(friendRequestRepository::delete);
        friendRequestRepository.flush();

        // 새로운 친구 요청 데이터
        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        // 새로운 친구 요청 데이터 추가
        friendRequestRepository.save(friendRequest);
    }
}
