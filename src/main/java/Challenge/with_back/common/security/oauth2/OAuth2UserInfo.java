package Challenge.with_back.common.security.oauth2;

public interface OAuth2UserInfo
{
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
}
