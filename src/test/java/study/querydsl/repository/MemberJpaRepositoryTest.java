package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest
@Transactional
@Profile("test")
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

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
    public void basicTest() {
        final Member member = Member.builder().username("member1").age(10).build();
        memberJpaRepository.save(member);

        final Member findMember = memberJpaRepository.findById(1L).get();
        assertThat(findMember).isEqualTo(member);

        final List<Member> resultAll = memberJpaRepository.findAll();
        assertThat(resultAll).containsExactly(member);

        final List<Member> resultMember1 = memberJpaRepository.findByUsername("member1");
        assertThat(resultMember1).containsExactly(member);

    }

    @Test
    public void basicQuerydslTest() {
        Member member = Member.builder().username("member1").age(10).build();
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> resultAll = memberJpaRepository.findAll_Querydsl();
        assertThat(resultAll).containsExactly(member);

        List<Member> resultMember1 = memberJpaRepository.findByUsername_Querydsl("member1");
        assertThat(resultMember1).containsExactly(member);

    }

    @ParameterizedTest
    @MethodSource("provideCondition")
    public void searchTest(final MemberSearchCondition condition) {

        initDB();

        final String teamName = condition.getTeamName();

        final List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        assertThat(result).extracting("teamName")
                .contains(teamName);

    }

    @ParameterizedTest
    @MethodSource("provideCondition")
    public void searchTest2(final MemberSearchCondition condition) {

        initDB();

        final String teamName = condition.getTeamName();

        final List<MemberTeamDto> result = memberJpaRepository.search(condition);

        assertThat(result).extracting("teamName")
                .contains(teamName);

    }

    private static Stream<Arguments> provideCondition() {
        return Stream.of(
                Arguments.of(MemberSearchCondition.builder()
                                .ageGoe(35)
                                .ageLoe(40)
                                .teamName("teamB")
                                .build()),
                Arguments.of(MemberSearchCondition.builder()
                                .ageGoe(20)
                                .ageLoe(30)
                                .teamName("teamA")
                                .build()),
                Arguments.of(MemberSearchCondition.builder()
                        .ageGoe(8)
                        .ageLoe(35)
                        .teamName("teamA")
                        .build())
                );

    }





}