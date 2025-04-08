package Challenge.with_back.security.oauth2;

import Challenge.with_back.common.response.exception.CustomExceptionCode;
import Challenge.with_back.entity.rdbms.User;
import Challenge.with_back.common.enums.AccountRole;
import Challenge.with_back.common.enums.LoginMethod;
import Challenge.with_back.common.response.exception.CustomException;
import Challenge.with_back.repository.rdbms.UserRepository;
import Challenge.with_back.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;

    @Value("${PROFILE_IMAGE_BUCKET_URL}")
    String profileImageBucketUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo;
        LoginMethod loginMethod;

        if(provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
            loginMethod = LoginMethod.GOOGLE;
        } else if (provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
            loginMethod = LoginMethod.KAKAO;
        } else if (provider.equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
            loginMethod = LoginMethod.NAVER;
        } else {
            throw new CustomException(CustomExceptionCode.PROVIDER_NOT_FOUND, provider);
        }

        if(oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getName() == null)
            throw new CustomException(CustomExceptionCode.INVALID_PROVIDER, null);

        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        Optional<User> existedUser = userRepository.findByEmailAndLoginMethod(email, loginMethod);
        User user;

        if(existedUser.isPresent()) {
            user = existedUser.get();
        } else {
            user = User.builder()
                    .loginMethod(loginMethod)
                    .email(email)
                    .password("")
                    .nickname(name)
                    .profileImageUrl(profileImageBucketUrl + "/profile-image_basic.svg")
                    .selfIntroduction("")
                    .allowEmailMarketing(true)
                    .premiumExpirationDate(LocalDate.now().minusDays(1))
                    .countUnreadNotification(0)
                    .paymentInformationEmail(email)
                    .accountRole(AccountRole.USER)
                    .build();

            userRepository.save(user);
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
