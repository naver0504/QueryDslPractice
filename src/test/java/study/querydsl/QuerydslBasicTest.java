package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@DataJpaTest
@Slf4j
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void init() {

        queryFactory = new JPAQueryFactory(em);

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
    public void startJPQL() {
        final Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {

        final QMember m = member;

        final Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search() {

        final QMember member = QMember.member;
        final Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void searchAndParam() {

        final QMember member = QMember.member;
        final Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void resultFetch() {

        final QMember member = QMember.member;

        final List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

//        final Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();

       final Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

       final QueryResults<Member> results =
                queryFactory
                        .selectFrom(member)
                        .fetchResults();

        results.getTotal();

        final List<Member> content = results.getResults();

        final long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /***
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {

        final QMember member = QMember.member;

        em.persist(Member.builder().username(null).age(100).build());
        em.persist(Member.builder().username("member5").age(100).build());
        em.persist(Member.builder().username("member6").age(100).build());

        final List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        final Member member1 = result.get(0);
        final Member member2 = result.get(1);
        final Member member3 = result.get(2);

        assertThat(member1.getUsername()).isEqualTo("member5");
        assertThat(member2.getUsername()).isEqualTo("member6");
        assertThat(member3.getUsername()).isNull();


    }

    @Test
    public void paging1() {
        final List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        result.forEach(
                member -> log.info("member = {}", member)
        );

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        final List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        final Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }


    /**
     *
     * 팀의 이름과 각 팀의 평균 연령을 구하라
     */
    @Test
    public void group() {
        final List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        final Tuple teamA = result.get(0);
        final Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

        System.out.println("teamA = " + teamA);
        System.out.println("teamB = " + teamB);

    }

    /**
     *  팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {

        final List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }


    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());
        em.persist(Member.builder().username("teamC").build());


        final List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .orderBy(member.username.asc())
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }


    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team on t.name ='teamA';
     */
    @Test
    public void join_on_filtering() {

        final List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 회원 외부 조인
     *
     */
    @Test
    public void join_on_no_relation() {

        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());
        em.persist(Member.builder().username("teamC").build());


        final List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);

            final List<Member> fetch = queryFactory
                    .selectFrom(member)
                    .join(team)
                    .on(member.username.eq(team.name))
                    .fetch();

            for (Member fetch1 : fetch) {
                System.out.println("fetch1 = " + fetch1);
            }
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        final Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();


    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        final Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();

    }

    /***
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {

        final QMember memberSub = new QMember("memberSub");
        final Member result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetchOne();

        assertThat(result.getAge()).isEqualTo(40);
    }

    /***
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryGoe() {

        final QMember memberSub = new QMember("memberSub");
        final List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /***
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryIn() {

        final QMember memberSub = new QMember("memberSub");
        final List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(member.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() {
        final QMember memberSub = new QMember("memberSub");

        final List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        result.forEach(
                tuple -> System.out.println("tuple = " + tuple)
        );
    }


    @Test
    public void basicCase() {
        final List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase() {
        final List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("31살 이상"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void constant() {
        final List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() {

        //{username}_{age}
        final String s = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("s = " + s);
    }

}
