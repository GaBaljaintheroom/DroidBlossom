package site.timecapsulearchive.core.domain.group_member.api.command;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.timecapsulearchive.core.domain.group_member.service.GroupMemberCommandService;
import site.timecapsulearchive.core.global.common.response.ApiSpec;
import site.timecapsulearchive.core.global.common.response.SuccessCode;

@RestController
@RequestMapping("/group-members")
@RequiredArgsConstructor
public class GroupMemberCommandApiController implements GroupMemberCommandApi {

    private final GroupMemberCommandService groupMemberCommandService;

    @DeleteMapping(value = "/{group_id}/members/quit")
    @Override
    public ResponseEntity<ApiSpec<String>> quitGroup(
        @AuthenticationPrincipal final Long memberId,
        @PathVariable("group_id") final Long groupId
    ) {
        groupMemberCommandService.quitGroup(memberId, groupId);

        return ResponseEntity.ok(ApiSpec.empty(SuccessCode.SUCCESS));
    }

    @PostMapping(value = "/invite/{group_id}/member/{target_id}")
    @Override
    public ResponseEntity<ApiSpec<String>> inviteGroup(
        @AuthenticationPrincipal final Long memberId,
        @PathVariable("group_id") final Long groupId,
        @PathVariable("target_id") final Long targetId
    ) {
        groupMemberCommandService.inviteGroup(memberId, groupId, targetId);

        return ResponseEntity.ok(
            ApiSpec.empty(
                SuccessCode.ACCEPTED
            )
        );
    }

    @PostMapping(value = "/accept/{group_id}/member/{target_id}")
    @Override
    public ResponseEntity<ApiSpec<String>> acceptGroupInvitation(
        @AuthenticationPrincipal final Long memberId,
        @PathVariable("group_id") final Long groupId,
        @PathVariable("target_id") final Long targetId
    ) {
        groupMemberCommandService.acceptGroupInvite(memberId, groupId, targetId);

        return ResponseEntity.ok(
            ApiSpec.empty(
                SuccessCode.ACCEPTED
            )
        );
    }

    @DeleteMapping(value = "/reject/{group_id}/member/{target_id}")
    public ResponseEntity<ApiSpec<String>> rejectGroupInvitation(
        @AuthenticationPrincipal final Long memberId,
        @PathVariable("group_id") final Long groupId,
        @PathVariable("target_id") final Long targetId) {

        groupMemberCommandService.rejectRequestGroup(memberId, groupId, targetId);

        return ResponseEntity.ok(
            ApiSpec.empty(
                SuccessCode.SUCCESS
            )
        );
    }

    @DeleteMapping(value = "/{group_id}/members/{group_member_id}")
    @Override
    public ResponseEntity<ApiSpec<String>> kickGroupMember(
        @AuthenticationPrincipal final Long memberId,
        @PathVariable("group_id") final Long groupId,
        @PathVariable("group_member_id") final Long groupMemberId
    ) {
        groupMemberCommandService.kickGroupMember(memberId, groupId, groupMemberId);

        return ResponseEntity.ok(ApiSpec.empty(SuccessCode.SUCCESS));
    }
}
