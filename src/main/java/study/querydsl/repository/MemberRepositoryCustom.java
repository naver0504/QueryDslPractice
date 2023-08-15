package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(final MemberSearchCondition condition);

    Page<MemberTeamDto> searchPageSimple(final MemberSearchCondition condition, final Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(final MemberSearchCondition condition, final Pageable pageable);

}
