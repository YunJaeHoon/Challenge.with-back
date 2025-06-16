package Challenge.with_back.domain.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateChallengeDto
{
	@NotBlank(message = "챌린지 아이콘을 입력해주세요.")
	private String icon;

	@NotBlank(message = "챌린지 색상을 입력해주세요.")
	private String colorTheme;

	@NotBlank(message = "챌린지 이름을 입력해주세요.")
	@Size(max = 255, message = "챌린지 이름은 최대 255자까지 입력할 수 있습니다.")
	private String name;

	@Size(max = 255, message = "챌린지 설명은 최대 255자까지 입력할 수 있습니다.")
	private String description;

	@NotNull(message = "챌린지 목표 개수를 입력해주세요.")
	private int goalCount;

	@NotBlank(message = "챌린지 단위를 입력해주세요.")
	private String unit;

	@NotNull(message = "혼자서 하는 챌린지인지, 다함께 하는 챌린지인지 입력해주세요.")
	private Boolean isAlone;

	@NotNull(message = "챌린지 공개 여부를 입력해주세요.")
	private Boolean isPublic;

	@NotNull(message = "초대할 사용자 ID 리스트를 입력해주세요.")
	private List<Long> inviteUserIdList;
}
