package Challenge.with_back.domain.friend.service;

import Challenge.with_back.common.entity.rdbms.Friend;
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
        /// 예외 처리
        /// 1. 친구 요청을 보낸 사람과 받는 사람이 동일한 경우, 예외 처리
        /// 2. 친구 요청을 받는 사람이 존재하지 않는 경우, 예외 처리
        /// 3. 이미 둘이 친구 사이인 경우, 예외 처리

        // 친구 요청을 보낸 사람과 받는 사람이 동일한 경우, 예외 처리
        if(sender.getId().equals(receiverId)) {
            throw new CustomException(CustomExceptionCode.SAME_SENDER_AND_RECEIVER, receiverId);
        }

        // 친구 요청을 받는 사람이 존재하지 않는 경우, 예외 처리
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, receiverId));

        // 이미 둘이 친구 사이인 경우, 예외 처리
        if(
                friendRepository.findByUser1IdAndUser2Id(sender.getId(), receiver.getId()).isPresent() ||
                friendRepository.findByUser1IdAndUser2Id(receiver.getId(), sender.getId()).isPresent()
        ) {
            throw new CustomException(CustomExceptionCode.ALREADY_FRIEND, null);
        }

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

    // 친구 요청 수락
    @Transactional
    public void acceptFriendRequest(User receiver, Long friendRequestId)
    {
        /// 예외 처리
        /// 1. 친구 요청 데이터가 존재하지 않는 경우, 예외 처리
        /// 2. 이미 둘이 친구 사이인 경우, 예외 처리

        // 친구 요청 데이터 조회
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.FRIEND_REQUEST_NOT_FOUND, friendRequestId));

        // 이미 둘이 친구 사이인 경우, 예외 처리
        if(
                friendRepository.findByUser1IdAndUser2Id(friendRequest.getSender().getId(), receiver.getId()).isPresent() ||
                friendRepository.findByUser1IdAndUser2Id(receiver.getId(), friendRequest.getSender().getId()).isPresent()
        ) {
            throw new CustomException(CustomExceptionCode.ALREADY_FRIEND, null);
        }

        // 친구 데이터 생성
        Friend friend = Friend.builder()
                .user1(friendRequest.getSender())
                .user2(friendRequest.getReceiver())
                .build();

        // 친구 데이터 저장
        friendRepository.save(friend);

        // 친구 요청 데이터 삭제
        friendRequestRepository.delete(friendRequest);
    }
}
