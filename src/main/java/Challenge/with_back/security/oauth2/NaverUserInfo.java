package Challenge.with_back.security.oauth2;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class NaverUserInfo implements OAuth2UserInfo
{
    private Map<String, Object> attributes;

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return ((Map<?, ?>) attributes.get("response")).get("id").toString();
    }

    @Override
    public String getEmail() {
        return ((Map<?, ?>) attributes.get("response")).get("email").toString();
    }

    @Override
    public String getName() {
        return ((Map<?, ?>) attributes.get("response")).get("name").toString();
    }
}
