package Challenge.with_back.security.oauth2;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.entity.User;
import Challenge.with_back.enums.account.AccountRole;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.repository.UserRepository;
import Challenge.with_back.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = null;

        if(provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else {
            throw new CustomException(CustomExceptionCode.PROVIDER_NOT_FOUND, provider);
        }

        if(oAuth2UserInfo.getEmail() == null)
            throw new CustomException(CustomExceptionCode.NOT_VALID_PROVIDER, null);

        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();
        Optional<User> existedUser = userRepository.findByEmail(email);
        User user;

        if(existedUser.isPresent()) {
            user = existedUser.get();
        } else {
            user = User.builder()
                    .loginMethod(LoginMethod.GOOGLE)
                    .email(email)
                    .password("")
                    .nickname(name)
                    .profileImageUrl("/기본경로")
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
