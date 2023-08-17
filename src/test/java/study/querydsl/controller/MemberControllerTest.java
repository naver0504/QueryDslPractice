package study.querydsl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @Mock
    private MemberRepository memberRepository;

    @Spy
    private PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(memberController)
                .setCustomArgumentResolvers(pageableHandlerMethodArgumentResolver)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    public void 멤버_조회() throws Exception {
        final String url = "/v1/members";

        doReturn(List.of(
                MemberTeamDto.builder().username("member1").age(10).teamName("teamA").build(),
                MemberTeamDto.builder().username("member2").age(20).teamName("teamA").build(),
                MemberTeamDto.builder().username("member3").age(30).teamName("teamB").build(),
                MemberTeamDto.builder().username("member4").age(40).teamName("teamB").build()
        )).when(memberJpaRepository).search(any(MemberSearchCondition.class));

        final ResultActions result = mockMvc.perform(
                get(url)
                        .content(objectMapper.writeValueAsString(MemberSearchCondition
                                .builder()
                                .ageGoe(10)
                                .ageLoe(40)
                                .build()))
                        .contentType("application/json")

        );

        result.andExpect(MockMvcResultMatchers.status().isOk());
        final String content = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        final MemberTeamDto[] memberTeamDto = objectMapper.readValue(content, MemberTeamDto[].class);
        final List<MemberTeamDto> memberTeamDtoList = Arrays.stream(memberTeamDto).toList();

        assertThat(memberTeamDtoList.size()).isEqualTo(4);
        assertThat(memberTeamDtoList).extracting("username").containsExactly("member1", "member2", "member3", "member4");
        verify(memberJpaRepository, times(1)).search(any(MemberSearchCondition.class));

    }

    @Test
    public void 페이징처리테스트() throws Exception {
        final String url = "/v3/members";
        final int page = 0;
        final int size = 4;

        List<MemberTeamDto> content = List.of(
                MemberTeamDto.builder().username("member1").age(10).teamName("teamA").build(),
                MemberTeamDto.builder().username("member2").age(20).teamName("teamA").build(),
                MemberTeamDto.builder().username("member3").age(30).teamName("teamB").build(),
                MemberTeamDto.builder().username("member4").age(40).teamName("teamB").build()
        );


        doReturn(PageableExecutionUtils.getPage(content, PageRequest.of(0, 4), () -> 4L))
                .when(memberRepository).searchPageComplex(any(MemberSearchCondition.class), any(Pageable.class));

        final ResultActions result = mockMvc.perform(
                get(url)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType("application/json")

        );

        result.andExpect(MockMvcResultMatchers.status().isOk());
        final String resultContent = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        final Map map = objectMapper.readValue(resultContent, HashMap.class);
        List<MemberTeamDto> pageResult = (List<MemberTeamDto>) map.get("content");
        assertThat(pageResult).extracting("username").containsExactly("member1", "member2", "member3", "member4");



    }




}