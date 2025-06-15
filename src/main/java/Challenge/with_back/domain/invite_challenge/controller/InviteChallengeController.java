package Challenge.with_back.domain.invite_challenge.controller;

import Challenge.with_back.domain.invite_challenge.service.InviteChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InviteChallengeController
{
    private final InviteChallengeService inviteChallengeService;
}
