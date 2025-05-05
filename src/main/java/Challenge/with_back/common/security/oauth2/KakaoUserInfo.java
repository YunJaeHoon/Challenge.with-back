package Challenge.with_back.common.security.oauth2;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@AllArgsConstructor
@Slf4j
public class KakaoUserInfo implements OAuth2UserInfo
{
    private Map<String, Object> attributes;

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return ((Map<?, ?>) attributes.get("kakao_account")).get("email").toString();
    }

    @Override
    public String getName() {
        return ((Map<?, ?>) ((Map<?, ?>) attributes.get("kakao_account")).get("profile")).get("nickname").toString();
    }
}
