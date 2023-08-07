package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        Member member = Member.builder().username("member1").age(10).build();
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> resultAll = memberJpaRepository.findAll();
        assertThat(resultAll).containsExactly(member);

        List<Member> resultMember1 = memberJpaRepository.findByUsername("member1");
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





}