package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberRepositoryCustomImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;


    private void initDB() {
        final Team teamA = Team.builder().name("teamA").build();
        final Team teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        final Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        final Member member2 = Member.builder().username("member2").age(20).team(teamA).build();
        final Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        final Member member4 = Member.builder().username("member4").age(40).team(teamB).build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }

    @Test
    public void searchPageSimpleTest() {
        initDB();

        final MemberSearchCondition condition = MemberSearchCondition.builder().build();

        final PageRequest pageRequest = PageRequest.of(0, 3);
        final Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }


    @ParameterizedTest
    @MethodSource("provideConditionAndPage")
    public void searchPageComplexTest(final MemberSearchCondition condition, final PageRequest pageRequest) {
        initDB();

        final Page<MemberTeamDto> result = memberRepository.searchPageComplex(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(pageRequest.getPageSize());
        result.getContent()
                .forEach(
                        memberTeamDto -> assertThat(memberTeamDto.getAge()).isBetween(condition.getAgeGoe(), condition.getAgeLoe())
                );
    }


    private static Stream<Arguments> provideConditionAndPage() {
        return Stream.of(
                Arguments.of(
                        MemberSearchCondition
                                .builder()
                                .ageLoe(20)
                                .ageGoe(0)
                                .build(),
                        PageRequest.of(0, 3)),
                Arguments.of(
                        MemberSearchCondition
                                .builder()
                                .ageLoe(40)
                                .ageGoe(30)
                                .build(),
                        PageRequest.of(0, 3)),
                Arguments.of(
                        MemberSearchCondition
                                .builder()
                                .ageLoe(40)
                                .ageGoe(10)
                                .build(),
                        PageRequest.of(1, 3))
                );
    }

}