package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
@ActiveProfiles("test")
@DataJpaTest
@Slf4j
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testEntity() {
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

        em.flush();
        em.clear();

        final List<Member> resultList = em.createQuery("select m from Member  m", Member.class)
                .getResultList();

        resultList.forEach(member -> {
            log.info("member = {}", member);
            log.info("member.Team = {}", member.getTeam());
        });

    }

}