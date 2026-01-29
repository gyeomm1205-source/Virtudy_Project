package com.ssafy.virtudy.tier.controller;

import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.tier.dto.TierResponse;
import com.ssafy.virtudy.tier.service.TierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.aspectj.lang.annotation.Before;

/**
 * TierController 테스트 클래스
 * 
 * @ExtendWith(MockitoExtension.class): Mockito 확장을 사용하여 Mock 객체를 주입받고 관리합니다.
 * MockitoExtension은 JUnit 5와 Mockito를 통합하여 테스트 생명주기를 관리해줍니다.
 */
@ExtendWith(MockitoExtension.class)
class TierControllerTest {

    /**
     * @InjectMocks:
     * - 테스트 대상이 되는 컨트롤러 객체입니다.
     * - @Mock으로 선언된 의존성(TierService 등)이 이 객체에 자동으로 주입됩니다.
     */
    @InjectMocks
    private TierController tierController;

    /**
     * @Mock:
     * - 가짜(Mock) 객체를 생성합니다.
     * - 실제 TierService의 로직을 수행하지 않고, 미리 정의된 동작(Behavior)을 수행하도록 설정할 수 있습니다.
     * - 이를 통해 서비스 레이어와 분리된 컨트롤러 단위 테스트(Unit Test)를 가능하게 합니다.
     */
    @Mock
    private TierService tierService;

    // MockMvc: 스프링 MVC 동작을 재현하여 HTTP 요청/응답을 테스트하는 핵심 도구
    private MockMvc mockMvc;

    /**
     * @BeforeEach:
     * - 각 테스트 메서드(@Test)가 실행되기 전에 반드시 한 번씩 실행되는 초기화 메서드입니다.
     * - 독립적인 테스트 환경을 보장하기 위해 사용합니다.
     */
    @BeforeEach
    void setUp() {
        /**
         * MockMvcBuilders.standaloneSetup(...):
         * - 스프링 컨텍스트 전체를 띄우지 않고, 해당 컨트롤러(tierController)만 띄워서 가볍게 테스트합니다.
         * - 단위 테스트에 적합하며 실행 속도가 빠릅니다.
         * 
         * .setCustomArgumentResolvers(...):
         * - 컨트롤러 메서드의 파라미터 중 @AuthenticationPrincipal UserPrincipal과 같이
         *   특별한 처리가 필요한 인자를 해결해주는 리졸버를 등록합니다.
         * - 여기서는 MockUserPrincipalArgumentResolver를 통해 가짜 인증 사용자 정보를 주입합니다.
         */
        mockMvc = MockMvcBuilders.standaloneSetup(tierController)
                .setCustomArgumentResolvers(new MockUserPrincipalArgumentResolver())
                .build();
    }

    /**
     * 내 티어 조회 성공 테스트
     * 1. given: 이미 인증된 사용자 & mock data를 미리 넣어두어 나중에 조회될 때 해당 데이터를 사용하도록 한다
     * 2. when: /api/tier/my 로 GET 요청을 보낸다
     * 3. then: 이미 mocked 된 데이터를 조회하므로 GET이 성공해야 한다.
     * @throws Exception
     */

    @Test
    @DisplayName("내 티어 조회 성공 테스트")
    void getMyTier_Success() throws Exception {
        // given (준비 단계)
        // 테스트에 필요한 데이터와 Mock 객체의 동작을 정의합니다.
        String memberId = "test-uuid";
        String nickname = "SSAFY";
        
        // 서비스가 반환할 예상 응답 객체 생성
        TierResponse response = TierResponse.builder()
                .nickname(nickname)
                .tierScore(1500)
                .tierRank("SILVER")
                .totalStudyTime(120)
                .build();

        // given(tierService.getMyTier(...) ):
        // - "만약 tierService.getMyTier() 메서드가 호출된다면..." 이라는 상황을 가정합니다.
        // .willReturn(response):
        // - "... 위에서 만든 response 객체를 반환해라" 라고 동작을 지정(Stubbing)합니다.
        given(tierService.getMyTier(memberId)).willReturn(response);

        // when & then (실행 및 검증 단계)
        // mockMvc를 통해 실제 HTTP 요청을 보내는 것처럼 시뮬레이션합니다.
        mockMvc.perform(get("/api/tier/my")) // GET /api/tier/my 요청 발생
                .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 검증
                .andExpect(jsonPath("$.nickname").value(nickname)) // JSON 응답의 nickname 필드가 기대값과 일치하는지 검증
                .andExpect(jsonPath("$.tierScore").value(1500))    // JSON 응답의 tierScore 필드 검증
                .andExpect(jsonPath("$.tierRank").value("SILVER")) // JSON 응답의 tierRank 필드 검증
                .andDo(print()); // 테스트 실행 과정(요청/응답 내용)을 콘솔에 출력 (디버깅용)
    }

    /**
     * MockUserPrincipalArgumentResolver (내부 클래스)
     * 
     * 역할:
     * - 컨트롤러의 @AuthenticationPrincipal 어노테이션이 붙은 UserPrincipal 파라미터를 처리합니다.
     * - 실제 시큐리티 필터를 거치지 않는 단위 테스트 환경에서, 
     *   컨트롤러가 필요로 하는 인증 객체(UserPrincipal)를 가짜로 생성해서 넣어주는 역할을 합니다.
     */
    static class MockUserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
        
        /**
         * supportsParameter:
         * - 이 리졸버가 처리할 수 있는 파라미터 타입인지 확인합니다.
         * - UserPrincipal 클래스 타입이면 true를 반환하여 resolveArgument가 실행되도록 합니다.
         */
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        /**
         * resolveArgument:
         * - 실제 파라미터에 들어갈 객체를 생성하여 반환합니다.
         * - 여기서는 "test-uuid"를 가진 임의의 Member 객체를 포함한 UserPrincipal을 생성합니다.
         */
        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, 
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            // 테스트용 임의 멤버 생성
            Member member = Member.builder()
                    .memberId("test-uuid")
                    .build();
            // UserPrincipal로 감싸서 반환
            return new UserPrincipal(member);
        }
    }
}
