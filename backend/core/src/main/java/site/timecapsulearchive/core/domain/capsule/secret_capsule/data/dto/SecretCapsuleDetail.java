package site.timecapsulearchive.core.domain.capsule.secret_capsule.data.dto;

import java.time.ZonedDateTime;
import site.timecapsulearchive.core.domain.capsule.entity.CapsuleType;

public record SecretCapsuleDetail(
    Long capsuleId,
    String capsuleSkinUrl,
    ZonedDateTime dueDate,
    String nickname,
    String profileUrl,
    ZonedDateTime createdAt,
    String address,
    String title,
    String content,
    Boolean isOpened,
    CapsuleType capsuleType
) {

}