package site.timecapsulearchive.core.domain.friend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import site.timecapsulearchive.core.common.fixture.domain.MemberFixture;
import site.timecapsulearchive.core.common.fixture.dto.FriendDtoFixture;
import site.timecapsulearchive.core.domain.friend.data.dto.SearchFriendSummaryDto;
import site.timecapsulearchive.core.domain.friend.data.dto.SearchFriendSummaryDtoByTag;
import site.timecapsulearchive.core.domain.friend.exception.FriendNotFoundException;
import site.timecapsulearchive.core.domain.friend.repository.member_friend.MemberFriendRepository;
import site.timecapsulearchive.core.domain.friend.service.query.FriendQueryService;
import site.timecapsulearchive.core.domain.member_group.repository.groupInviteRepository.GroupInviteRepository;
import site.timecapsulearchive.core.domain.member_group.repository.memberGroupRepository.MemberGroupRepository;
import site.timecapsulearchive.core.global.common.wrapper.ByteArrayWrapper;

class FriendQueryServiceTest {

    private final MemberFriendRepository memberFriendRepository = mock(
        MemberFriendRepository.class);
    private final MemberGroupRepository memberGroupRepository = mock(MemberGroupRepository.class);
    private final GroupInviteRepository groupInviteRepository = mock(GroupInviteRepository.class);

    private final FriendQueryService friendQueryService = new FriendQueryService(
        memberFriendRepository, memberGroupRepository, groupInviteRepository);

    @Test
    void 사용자는_주소록_기반_핸드폰_번호로_Ahchive_사용자_리스트를_조회_할_수_있다() {
        //given
        Long memberId = 1L;
        List<ByteArrayWrapper> phones = MemberFixture.getPhones(5);

        given(memberFriendRepository.findFriendsByPhone(anyLong(), anyList()))
            .willReturn(FriendDtoFixture.getFriendSummaryDtos(5));

        //when
        List<SearchFriendSummaryDto> dtos = friendQueryService.findFriendsByPhone(memberId, phones);

        //then
        assertThat(dtos.size()).isEqualTo(phones.size());
    }


    @Test
    void 사용자는_주소록_기반_번호_없이_Ahchive_사용자_리스트_조회하면_빈_리스트를_반환한다() {
        //given
        Long memberId = 1L;
        List<ByteArrayWrapper> phones = Collections.emptyList();
        given(memberFriendRepository.findFriendsByPhone(anyLong(), anyList()))
            .willReturn(Collections.emptyList());

        //when
        List<SearchFriendSummaryDto> dtos = friendQueryService.findFriendsByPhone(memberId,
            phones);

        //then
        assertTrue(dtos.isEmpty());
    }

    @Test
    void 사용자는_태그로_Ahchive_사용자를_검색할_수_있다() {
        //given
        Long memberId = 1L;
        String tag = "testTag";
        Optional<SearchFriendSummaryDtoByTag> summaryDtoByTag = FriendDtoFixture.getFriendSummaryDtoByTag();
        SearchFriendSummaryDtoByTag expectDto = summaryDtoByTag.get();

        given(memberFriendRepository.findFriendsByTag(anyLong(), anyString()))
            .willReturn(summaryDtoByTag);

        //when
        SearchFriendSummaryDtoByTag actualResponse = friendQueryService.searchFriend(
            memberId, tag);

        //then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(expectDto.id()).isEqualTo(actualResponse.id());
            softly.assertThat(expectDto.profileUrl()).isEqualTo(actualResponse.profileUrl());
            softly.assertThat(expectDto.nickname()).isEqualTo(actualResponse.nickname());
            softly.assertThat(expectDto.isFriend()).isEqualTo(actualResponse.isFriend());
        });
    }

    @Test
    void 사용자는_존재하지_않는_태그로_Ahchive_사용자를_검색하면_예외가_발생한다() {
        //given
        Long memberId = 1L;
        String tag = "testTag";

        given(memberFriendRepository.findFriendsByTag(anyLong(), anyString()))
            .willReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> friendQueryService.searchFriend(memberId, tag))
            .isInstanceOf(FriendNotFoundException.class);
    }
}