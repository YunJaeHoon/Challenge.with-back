package Challenge.with_back.domain.friend.service;

import Challenge.with_back.common.entity.rdbms.Friend;
import Challenge.with_back.common.entity.rdbms.FriendBlock;
import Challenge.with_back.common.entity.rdbms.FriendRequest;
import Challenge.with_back.common.entity.rdbms.User;
import Challenge.with_back.common.repository.rdbms.FriendBlockRepository;
import Challenge.with_back.common.repository.rdbms.FriendRepository;
import Challenge.with_back.common.repository.rdbms.FriendRequestRepository;
import Challenge.with_back.common.repository.rdbms.UserRepository;
import Challenge.with_back.common.exception.CustomException;
import Challenge.with_back.common.exception.CustomExceptionCode;
import Challenge.with_back.domain.friend.dto.FriendBlockDto;
import Challenge.with_back.domain.friend.dto.FriendBlockListDto;
import Challenge.with_back.domain.friend.dto.FriendDto;
import Challenge.with_back.domain.friend.dto.FriendListDto;
import Challenge.with_back.domain.notification.FriendRequestNotificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService
{
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendBlockRepository friendBlockRepository;
    private final UserRepository userRepository;

    private final FriendRequestNotificationFactory friendRequestNotificationFactory;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

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
        if(friendRepository.findByUser1IdAndUser2Id(sender.getId(), receiver.getId()).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_FRIEND, null);
        }

        /// 차단된 상태인 경우, 친구 요청 데이터를 생성하지 않음
        /// 차단되지 않은 상태인 경우, 친구 요청 데이터 생성

        // 친구 요청 수신자의 친구 요청 송신자에 대한 친구 차단 데이터 조회
        Optional<FriendBlock> friendBlockOptional = friendBlockRepository.findByBlockingUserIdAndBlockedUserId(receiver.getId(), sender.getId());

        // 친구 요청 수신자가 친구 요청 송신자를 차단하지 않은 경우
        if(friendBlockOptional.isEmpty())
        {
            // 기존에 존재하던 친구 요청 데이터
            Optional<FriendRequest> friendRequestOptional = friendRequestRepository.findBySenderIdAndReceiverId(sender.getId(), receiverId);

            // 기존에 친구 요청 데이터가 존재한다면, 해당 데이터를 삭제
            friendRequestOptional.ifPresent(friendRequestRepository::delete);
            friendRequestRepository.flush();

            // TODO: 기존의 친구 요청 알림도 삭제해야 함

            // 새로운 친구 요청 데이터
            FriendRequest friendRequest = FriendRequest.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .build();

            // 새로운 친구 요청 데이터 추가
            friendRequestRepository.save(friendRequest);

            // 친구 요청 알림 생성
            friendRequestNotificationFactory.createNotification(receiver, friendRequest.getId());
        }
    }

    // 친구 요청 수락 또는 거절
    @Transactional
    public void answerFriendRequest(User receiver, Long friendRequestId, boolean isAccept)
    {
        /// 예외 처리
        /// 1. 친구 요청 데이터가 존재하지 않는 경우, 예외 처리
        /// 2. 이미 둘이 친구 사이인 경우, 예외 처리

        // 친구 요청 데이터 조회
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.FRIEND_REQUEST_NOT_FOUND, friendRequestId));

        // 이미 둘이 친구 사이인 경우, 예외 처리
        if(friendRepository.findByUser1IdAndUser2Id(friendRequest.getSender().getId(), receiver.getId()).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_FRIEND, null);
        }

        // 친구 요청을 수락하는 경우
        if(isAccept)
        {
            // 친구 요청 수신자의 친구 요청 송신자에 대한 친구 차단 데이터 조회
            Optional<FriendBlock> friendBlockOptional = friendBlockRepository.findByBlockingUserIdAndBlockedUserId(receiver.getId(), friendRequest.getSender().getId());

            // 친구 요청 수신자가 친구 요청 송신자를 차단한 경우, 예외 처리
            if(friendBlockOptional.isPresent()) {
                throw new CustomException(CustomExceptionCode.ALREADY_BLOCKED_FRIEND, friendRequest.getSender().getId());
            }

            // 친구 데이터 생성
            Friend friend = Friend.builder()
                    .user1(friendRequest.getSender())
                    .user2(friendRequest.getReceiver())
                    .build();

            // 친구 데이터 저장
            friendRepository.save(friend);
        }

        // 친구 요청 데이터 삭제
        friendRequestRepository.delete(friendRequest);
    }

    // 친구 차단
    @Transactional
    public void createFriendBlock(User blockingUser, Long blockedUserId)
    {
        /// 예외 처리
        /// 1. 차단을 하는 사람과 차단을 당하는 사람이 동일한 경우, 예외 처리
        /// 2. 차단을 당하는 사용자 데이터가 존재하지 않는 경우, 예외 처리
        /// 3. 이미 차단한 경우, 예외 처리

        // 친구 요청을 보낸 사람과 받는 사람이 동일한 경우, 예외 처리
        if(blockingUser.getId().equals(blockedUserId)) {
            throw new CustomException(CustomExceptionCode.SAME_BLOCKING_USER_AND_BLOCKED_USER, blockedUserId);
        }

        // 차단을 당하는 사용자 데이터가 존재하지 않는 경우, 예외 처리
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, blockedUserId));

        // 이미 차단한 경우, 예외 처리
        if(friendBlockRepository.findByBlockingUserIdAndBlockedUserId(blockingUser.getId(), blockedUser.getId()).isPresent()) {
            throw new CustomException(CustomExceptionCode.ALREADY_BLOCKED_FRIEND, null);
        }

        /// 친구 차단 데이터 생성

        // 친구 차단 데이터 생성
        FriendBlock friendBlock = FriendBlock.builder()
                .blockingUser(blockingUser)
                .blockedUser(blockedUser)
                .build();

        friendBlockRepository.save(friendBlock);
    }

    // 친구 차단 해제
    @Transactional
    public void deleteFriendBlock(User blockingUser, Long friendBlockId)
    {
        /// 예외 처리
        /// 1. 친구 차단 데이터가 존재하지 않는 경우, 예외 처리
        /// 2. 친구 차단 데이터의 차단한 사용자가 친구 차단 해제 요청자와 동일하지 않은 경우, 예외 처리

        // 친구 차단 데이터가 존재하지 않는 경우, 예외 처리
        FriendBlock friendBlock = friendBlockRepository.findById(friendBlockId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.FRIEND_BLOCK_NOT_FOUND, friendBlockId));

        // 친구 차단 데이터의 차단한 사용자가 친구 차단 해제 요청자와 동일하지 않은 경우, 예외 처리
        if(!friendBlock.getBlockingUser().getId().equals(blockingUser.getId())) {
            throw new CustomException(CustomExceptionCode.DIFFERENT_BLOCKING_USER_AND_REQUESTER, friendBlockId);
        }

        /// 친구 차단 데이터 삭제

        // 친구 차단 데이터 삭제
        friendBlockRepository.delete(friendBlock);
    }

    // 친구 리스트 조회
    @Transactional(readOnly = true)
    public FriendListDto getFriendList(User user, Pageable pageable)
    {
        // 사용자 ID로 친구 데이터 페이지 조회
        Page<Friend> friendPage = friendRepository.findPageByUserId(user.getId(), pageable);

        // 친구가 존재하지 않는 경우, 예외 처리
        if(friendPage.isEmpty()) {
            throw new CustomException(CustomExceptionCode.FRIEND_NOT_FOUND, Map.of(
                    "pageSize", pageable.getPageSize(),
                    "currentPage", pageable.getPageNumber(),
                    "totalPage", friendPage.getTotalPages()
            ));
        }

        // 친구 데이터 페이지를 FriendDto 리스트로 변경
        List<FriendDto> friendList = friendPage.stream()
                .map(friend -> {

                    // 친구를 맺은 사용자 데이터
                    User friendUser = Objects.equals(user.getId(), friend.getUser1().getId()) ? friend.getUser2() : friend.getUser1();

                    return FriendDto.builder()
                            .friendId(friend.getId())
                            .userId(friendUser.getId())
                            .email(friendUser.getEmail())
                            .nickname(friendUser.getNickname())
                            .profileImageUrl(profileImageBucketUrl + friendUser.getProfileImageUrl())
                            .build();
                }).toList();

        return FriendListDto.builder()
                .friendList(friendList)
                .pageSize(pageable.getPageSize())
                .currentPageNumber(pageable.getPageNumber())
                .totalPageCount(friendPage.getTotalPages())
                .isLastPage(friendPage.isLast())
                .build();
    }

    // 친구 차단 리스트 조회
    @Transactional(readOnly = true)
    public FriendBlockListDto getFriendBlockList(User user, Pageable pageable)
    {
        // 사용자 ID로 친구 차단 데이터 페이지 조회
        Page<FriendBlock> friendBlockPage = friendBlockRepository.findByBlockingUserId(user.getId(), pageable);

        // 친구 차단 데이터가 존재하지 않는 경우, 예외 처리
        if(friendBlockPage.isEmpty()) {
            throw new CustomException(CustomExceptionCode.FRIEND_BLOCK_NOT_FOUND, Map.of(
                    "pageSize", pageable.getPageSize(),
                    "currentPage", pageable.getPageNumber(),
                    "totalPage", friendBlockPage.getTotalPages()
            ));
        }

        // 친구 차단 데이터 페이지를 FriendBlockDto 리스트로 변경
        List<FriendBlockDto> friendBlockList = friendBlockPage.stream()
                .map(friendBlock -> FriendBlockDto.builder()
                        .friendBlockId(friendBlock.getId())
                        .userId(friendBlock.getBlockedUser().getId())
                        .email(friendBlock.getBlockedUser().getEmail())
                        .nickname(friendBlock.getBlockedUser().getNickname())
                        .profileImageUrl(profileImageBucketUrl + friendBlock.getBlockedUser().getProfileImageUrl())
                        .build()
                ).toList();

        return FriendBlockListDto.builder()
                .friendBlockList(friendBlockList)
                .pageSize(pageable.getPageSize())
                .currentPageNumber(pageable.getPageNumber())
                .totalPageCount(friendBlockPage.getTotalPages())
                .isLastPage(friendBlockPage.isLast())
                .build();
    }
}
