package study.querydsl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(memberController)
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
    }




}