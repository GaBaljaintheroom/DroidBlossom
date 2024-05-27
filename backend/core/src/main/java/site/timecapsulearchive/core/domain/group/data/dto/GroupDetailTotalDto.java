package site.timecapsulearchive.core.domain.group.data.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;
import site.timecapsulearchive.core.domain.group.data.response.GroupDetailResponse;
import site.timecapsulearchive.core.domain.group.data.response.GroupMemberResponse;

public record GroupDetailTotalDto(
    String groupName,
    String groupDescription,
    String groupProfileUrl,
    ZonedDateTime createdAt,
    Long groupCapsuleTotalCount,
    Boolean canGroupEdit,
    List<GroupMemberWithRelationDto> members
) {

    public static GroupDetailTotalDto as(
        final GroupDetailDto groupDetailDto,
        final Long groupCapsuleTotalCount,
        final Boolean canGroupEdit,
        final List<Long> friendIds
    ) {
        final List<GroupMemberWithRelationDto> membersWithRelation = groupDetailDto.members()
            .stream()
            .map(dto -> dto.toRelationDto(friendIds))
            .toList();

        return new GroupDetailTotalDto(
            groupDetailDto.groupName(),
            groupDetailDto.groupDescription(),
            groupDetailDto.groupProfileUrl(),
            groupDetailDto.createdAt(),
            groupCapsuleTotalCount,
            canGroupEdit,
            membersWithRelation
        );
    }

    public GroupDetailResponse toResponse(final Function<String, String> singlePreSignUrlFunction) {
        List<GroupMemberResponse> members = this.members.stream()
            .map(GroupMemberWithRelationDto::toResponse)
            .toList();

        return GroupDetailResponse.builder()
            .groupName(groupName)
            .groupDescription(groupDescription)
            .groupProfileUrl(singlePreSignUrlFunction.apply(groupProfileUrl))
            .createdAt(createdAt)
            .groupCapsuleTotalCount(groupCapsuleTotalCount)
            .canGroupEdit(canGroupEdit)
            .members(members)
            .build();
    }
}
