package Challenge.with_back.common.elastic_search;

import Challenge.with_back.common.entity.Challenge;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "challenge", createIndex = true)
@Setting(settingPath = "challenge-setting.json")
@Mapping(mappingPath = "challenge-mapping.json")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ChallengeDocument
{
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Long)
    private Long superAdminId;

    @Field(type = FieldType.Text)
    private String icon;

    @Field(type = FieldType.Text)
    private String colorTheme;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Integer)
    private int goalCount;

    @Field(type = FieldType.Text)
    private String unit;

    @Field(type = FieldType.Integer)
    private int maxParticipantCount;

    @Field(type = FieldType.Date, format = {DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis})
    private LocalDateTime createdAt;

    public static ChallengeDocument from(Challenge challenge)
    {
        return ChallengeDocument.builder()
                .id(challenge.getId())
                .superAdminId(challenge.getSuperAdmin().getId())
                .icon(challenge.getIcon())
                .colorTheme(challenge.getColorTheme().name())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .goalCount(challenge.getGoalCount())
                .unit(challenge.getUnit().name())
                .maxParticipantCount(challenge.getMaxParticipantCount())
                .createdAt(challenge.getCreatedAt())
                .build();
    }
}
