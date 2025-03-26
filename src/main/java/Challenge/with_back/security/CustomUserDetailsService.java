package Challenge.with_back.security;

import Challenge.with_back.dto.response.CustomExceptionCode;
import Challenge.with_back.entity.User;
import Challenge.with_back.enums.account.LoginMethod;
import Challenge.with_back.exception.CustomException;
import Challenge.with_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService
{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
    {
        User user = userRepository.findByEmailAndLoginMethod(email, LoginMethod.NORMAL)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, email));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserByUsernameAndLoginMethod(String email, LoginMethod loginMethod)
    {
        User user = userRepository.findByEmailAndLoginMethod(email, loginMethod)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, email));

        return new CustomUserDetails(user);
    }
}
