package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> search(final MemberSearchCondition condition) {

        final BooleanBuilder builder = createSearchBooleanBuilder(condition);
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        builder
                )
                .fetch();

    }

    private BooleanBuilder createSearchBooleanBuilder(final MemberSearchCondition condition) {
        final BooleanBuilder builder = new BooleanBuilder();

        return  builder
                .and(ageGoe(condition.getAgeGoe()))
                .and(ageLoe(condition.getAgeLoe()))
                .and(teamNameEq(condition.getTeamName()));

    }

    private BooleanExpression ageLoe(final Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(final Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(final String teamName) {
        return hasText(teamName) ? member.team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(final String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }
}
