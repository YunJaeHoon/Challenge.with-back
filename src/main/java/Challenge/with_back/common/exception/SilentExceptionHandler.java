package Challenge.with_back.common.exception;

import org.springframework.util.ErrorHandler;

public class SilentExceptionHandler implements ErrorHandler
{
    @Override
    public void handleError(Throwable throwable)
    {
        // 아무것도 하지 않음 (기본 로그도 남기지 않음)
    }
}
