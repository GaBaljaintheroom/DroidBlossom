package site.timecapsulearchive.core.domain.member.data.mapper;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.timecapsulearchive.core.domain.auth.data.request.SignUpRequest;
import site.timecapsulearchive.core.domain.member.data.dto.MemberDetailResponseDto;
import site.timecapsulearchive.core.domain.member.data.dto.MemberNotificationDto;
import site.timecapsulearchive.core.domain.member.data.dto.SignUpRequestDto;
import site.timecapsulearchive.core.domain.member.data.response.MemberDetailResponse;
import site.timecapsulearchive.core.domain.member.data.response.MemberNotificationResponse;
import site.timecapsulearchive.core.domain.member.data.response.MemberNotificationSliceResponse;
import site.timecapsulearchive.core.domain.member.entity.Member;
import site.timecapsulearchive.core.domain.member.entity.SocialType;
import site.timecapsulearchive.core.global.security.oauth.dto.OAuth2UserInfo;
import site.timecapsulearchive.core.global.util.nickname.MakeRandomNickNameUtil;
import site.timecapsulearchive.core.infra.s3.manager.S3PreSignedUrlManager;

@Component
@RequiredArgsConstructor
public class MemberMapper {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private final S3PreSignedUrlManager s3PreSignedUrlManager;

    public Member OAuthToEntity(
        final String authId,
        final SocialType socialType,
        final OAuth2UserInfo oAuth2UserInfo
    ) {
        return Member.builder()
            .authId(authId)
            .nickname(MakeRandomNickNameUtil.makeRandomNickName())
            .email(oAuth2UserInfo.getEmail())
            .profileUrl(oAuth2UserInfo.getImageUrl())
            .socialType(socialType)
            .tag(NanoIdUtils.randomNanoId())
            .build();
    }

    public SignUpRequestDto signUpRequestToDto(final SignUpRequest request) {
        return new SignUpRequestDto(
            request.authId(),
            request.email(),
            request.profileUrl(),
            request.socialType()
        );
    }

    public Member signUpRequestDtoToEntity(final SignUpRequestDto dto) {
        return Member.builder()
            .authId(dto.authId())
            .nickname(MakeRandomNickNameUtil.makeRandomNickName())
            .email(dto.email())
            .profileUrl(dto.profileUrl())
            .socialType(dto.socialType())
            .tag(NanoIdUtils.randomNanoId())
            .build();
    }

    public MemberDetailResponse memberDetailResponseDtoToResponse(
        final MemberDetailResponseDto dto,
        final String decryptedPhone
    ) {
        return new MemberDetailResponse(dto.nickname(), dto.profileUrl(), decryptedPhone);
    }

    public MemberNotificationSliceResponse notificationSliceToResponse(
        final List<MemberNotificationDto> content,
        final boolean hasNext
    ) {
        List<MemberNotificationResponse> responses = content.stream()
            .map(dto -> MemberNotificationResponse.builder()
                .title(dto.title())
                .text(dto.text())
                .createdAt(dto.createdAt().withZoneSameInstant(ASIA_SEOUL))
                .imageUrl(s3PreSignedUrlManager.getS3PreSignedUrlForGet(dto.imageUrl()))
                .build()
            )
            .toList();

        return new MemberNotificationSliceResponse(responses, hasNext);
    }
}