package Challenge.with_back.domain.challenge.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CreateChallengeDto
{
	private String icon;
	private String colorTheme;
	private String name;
	private String description;
	private int goalCount;
	private String unit;
	private Boolean isAlone;
	private Boolean isPublic;
	private List<Long> inviteUserIdList;
}
