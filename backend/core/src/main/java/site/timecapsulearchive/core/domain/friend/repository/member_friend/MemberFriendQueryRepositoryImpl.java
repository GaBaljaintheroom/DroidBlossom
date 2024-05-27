package site.timecapsulearchive.core.domain.friend.repository.member_friend;

import static com.querydsl.jpa.JPAExpressions.select;
import static site.timecapsulearchive.core.domain.friend.entity.QFriendInvite.friendInvite;
import static site.timecapsulearchive.core.domain.friend.entity.QMemberFriend.memberFriend;
import static site.timecapsulearchive.core.domain.member.entity.QMember.member;
import static site.timecapsulearchive.core.domain.member_group.entity.QMemberGroup.memberGroup;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import site.timecapsulearchive.core.domain.friend.data.dto.FriendSummaryDto;
import site.timecapsulearchive.core.domain.friend.data.dto.SearchFriendSummaryDto;
import site.timecapsulearchive.core.domain.friend.data.dto.SearchFriendSummaryDtoByTag;
import site.timecapsulearchive.core.domain.friend.data.request.FriendBeforeGroupInviteRequest;
import site.timecapsulearchive.core.domain.friend.entity.QFriendInvite;
import site.timecapsulearchive.core.global.common.wrapper.ByteArrayWrapper;

@Repository
@RequiredArgsConstructor
public class MemberFriendQueryRepositoryImpl implements MemberFriendQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Slice<FriendSummaryDto> findFriendsSlice(
        final Long memberId,
        final int size,
        final ZonedDateTime createdAt
    ) {
        final List<FriendSummaryDto> friends = jpaQueryFactory
            .select(
                Projections.constructor(
                    FriendSummaryDto.class,
                    memberFriend.friend.id,
                    memberFriend.friend.profileUrl,
                    memberFriend.friend.nickname,
                    memberFriend.createdAt
                )
            )
            .from(memberFriend)
            .innerJoin(member).on(memberFriend.owner.id.eq(member.id))
            .innerJoin(member).on(memberFriend.friend.id.eq(member.id))
            .where(memberFriend.owner.id.eq(memberId).and(memberFriend.createdAt.lt(createdAt)))
            .limit(size + 1)
            .fetch();

        return getFriendSummaryDtos(size, friends);
    }

    private Slice<FriendSummaryDto> getFriendSummaryDtos(final int size,
        final List<FriendSummaryDto> friendSummaryDtos) {
        final boolean hasNext = friendSummaryDtos.size() > size;
        if (hasNext) {
            friendSummaryDtos.remove(size);
        }

        return new SliceImpl<>(friendSummaryDtos, Pageable.ofSize(size), hasNext);
    }

    public Slice<FriendSummaryDto> findFriendsBeforeGroupInvite(
        final FriendBeforeGroupInviteRequest request) {
        final List<FriendSummaryDto> friends = jpaQueryFactory
            .select(
                Projections.constructor(
                    FriendSummaryDto.class,
                    memberFriend.friend.id,
                    memberFriend.friend.profileUrl,
                    memberFriend.friend.nickname,
                    memberFriend.createdAt
                )
            )
            .from(memberFriend)
            .innerJoin(member).on(memberFriend.owner.id.eq(member.id))
            .innerJoin(member).on(memberFriend.friend.id.eq(member.id))
            .where(memberFriend.owner.id.eq(request.memberId())
                .and(memberFriend.createdAt.lt(request.createdAt()))
                .and(memberFriend.friend.id.notIn(
                    select(memberGroup.member.id)
                        .from(memberGroup)
                        .where(memberGroup.group.id.eq(request.groupId()))
                ))
            )
            .limit(request.size() + 1)
            .fetch();

        return getFriendSummaryDtos(request.size(), friends);
    }

    public Slice<FriendSummaryDto> findFriendRequestsSlice(
        final Long memberId,
        final int size,
        final ZonedDateTime createdAt
    ) {
        final List<FriendSummaryDto> friends = jpaQueryFactory
            .select(
                Projections.constructor(
                    FriendSummaryDto.class,
                    friendInvite.owner.id,
                    friendInvite.owner.profileUrl,
                    friendInvite.owner.nickname,
                    friendInvite.createdAt
                )
            )
            .from(friendInvite)
            .join(friendInvite.owner, member)
            .where(friendInvite.friend.id.eq(memberId).and(friendInvite.createdAt.lt(createdAt)))
            .limit(size + 1)
            .fetch();

        return getFriendSummaryDtos(size, friends);
    }

    public List<SearchFriendSummaryDto> findFriendsByPhone(
        final Long memberId,
        final List<byte[]> hashes
    ) {
        final QFriendInvite friendInviteToFriend = new QFriendInvite("friendInviteToFriend");
        final QFriendInvite friendInviteToMe = new QFriendInvite("friendInviteToMe");

        return jpaQueryFactory
            .select(
                Projections.constructor(
                    SearchFriendSummaryDto.class,
                    member.id,
                    member.profileUrl,
                    member.nickname,
                    Projections.constructor(
                        ByteArrayWrapper.class,
                        member.phone_hash
                    ),
                    memberFriend.id.isNotNull(),
                    friendInviteToFriend.id.isNotNull(),
                    friendInviteToMe.id.isNotNull()
                )
            )
            .from(member)
            .leftJoin(memberFriend)
            .on(memberFriend.friend.id.eq(member.id).and(memberFriend.owner.id.eq(memberId)))
            .leftJoin(friendInviteToFriend)
            .on(friendInviteToFriend.friend.id.eq(member.id)
                .and(friendInviteToFriend.owner.id.eq(memberId)))
            .leftJoin(friendInviteToMe)
            .on(friendInviteToMe.friend.id.eq(memberId)
                .and(friendInviteToMe.owner.id.eq(member.id)))
            .where(member.phone_hash.in(hashes))
            .fetch();
    }

    public Optional<SearchFriendSummaryDtoByTag> findFriendsByTag(
        final Long memberId,
        final String tag
    ) {
        final QFriendInvite friendInviteToFriend = new QFriendInvite("friendInviteToFriend");
        final QFriendInvite friendInviteToMe = new QFriendInvite("friendInviteToMe");

        return Optional.ofNullable(jpaQueryFactory
            .select(
                Projections.constructor(
                    SearchFriendSummaryDtoByTag.class,
                    member.id,
                    member.profileUrl,
                    member.nickname,
                    memberFriend.id.isNotNull(),
                    friendInviteToFriend.id.isNotNull(),
                    friendInviteToMe.id.isNotNull()
                )
            )
            .from(member)
            .leftJoin(memberFriend)
            .on(memberFriend.friend.id.eq(member.id).and(memberFriend.owner.id.eq(memberId)))
            .leftJoin(friendInviteToFriend)
            .on(friendInviteToFriend.friend.id.eq(member.id)
                .and(friendInviteToFriend.owner.id.eq(memberId)))
            .leftJoin(friendInviteToMe)
            .on(friendInviteToMe.friend.id.eq(memberId)
                .and(friendInviteToMe.owner.id.eq(member.id)))
            .where(member.tag.eq(tag))
            .fetchOne()
        );
    }

    public List<Long> findFriendIdsByOwnerId(Long memberId) {
        return jpaQueryFactory
            .select(memberFriend.friend.id)
            .from(memberFriend)
            .where(memberFriend.owner.id.eq(memberId))
            .fetch();
    }

    public List<Long> findFriendIds(final List<Long> groupMemberIds, final Long memberId) {
        return jpaQueryFactory.select(memberFriend.friend.id)
            .from(memberFriend)
            .where(
                memberFriend.friend.id.in(groupMemberIds).and(memberFriend.owner.id.eq(memberId)))
            .fetch();
    }
}
