package com.ssafy.virtudy.member.application;

import com.ssafy.virtudy.global.auth.jwt.JwtUtil;
import com.ssafy.virtudy.global.auth.oauth.KakaoClient;
import com.ssafy.virtudy.global.auth.oauth.KakaoUserInfo;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.*;
import com.ssafy.virtudy.member.dto.MemberDto;
import com.ssafy.virtudy.member.dto.MemberKakaoLoginResponse;
import com.ssafy.virtudy.member.dto.MemberSignUpRequest;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberPreferenceRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MemberPreferenceRepository memberPreferenceRepository;

    @Mock
    private MemberGameStatRepository memberGameStatRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // --- Helper for Mocking Redis ---
    private void mockRedis() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private MemberSignUpRequest createSignupRequest(String email) {
        MemberSignUpRequest request = new MemberSignUpRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "nickname", "TestNick");
        ReflectionTestUtils.setField(request, "isServiceAgreed", true);
        ReflectionTestUtils.setField(request, "isVideoAgreed", true);
        ReflectionTestUtils.setField(request, "isPersonaAgreed", true);
        ReflectionTestUtils.setField(request, "studyType", StudyType.MARATHON);
        ReflectionTestUtils.setField(request, "activeTime", ActiveTimeType.MORNING);
        ReflectionTestUtils.setField(request, "jobType", JobType.OFFICE_WORKER);
        ReflectionTestUtils.setField(request, "targetHours", StudyTimeCategoryType.ONE_TO_TWO);
        ReflectionTestUtils.setField(request, "averageHours", StudyTimeCategoryType.ONE_TO_TWO);
        return request;
    }

    // ===================================================================================
    // 1. Kakao Login Tests
    // ===================================================================================

    @Test
    @DisplayName("[Kakao Login] New User -> needSignup = true")
    void kakaoLogin_NewUser() {
        // Given
        String code = "auth-code";
        String accessToken = "access-token";
        String email = "newuser@test.com";

        KakaoUserInfo userInfo = mock(KakaoUserInfo.class);
        KakaoUserInfo.KakaoAccount account = mock(KakaoUserInfo.KakaoAccount.class);
        KakaoUserInfo.KakaoAccount.Profile profile = mock(KakaoUserInfo.KakaoAccount.Profile.class);

        when(kakaoClient.getAccessToken(code)).thenReturn(accessToken);
        when(kakaoClient.getUserInfo(accessToken)).thenReturn(userInfo);
        when(userInfo.getKakaoAccount()).thenReturn(account);
        when(account.getEmail()).thenReturn(email);
        when(account.getProfile()).thenReturn(profile);
        when(profile.getNickname()).thenReturn("TempNick");
        when(profile.getProfileImageUrl()).thenReturn("img_url");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        MemberKakaoLoginResponse response = authService.kakaoLogin(code);

        // Then
        assertTrue(response.isNeedSignup());
        assertEquals(email, response.getEmail());
        assertEquals("TempNick", response.getTempNickname());
    }

    @Test
    @DisplayName("[Kakao Login] Existing Active User -> needSignup = false, Tokens Issued")
    void kakaoLogin_ExistingUser() {
        // Given
        mockRedis();
        String code = "auth-code";
        String email = "existing@test.com";
        Member member = Member.builder()
                .email(email)
                .status(MemberStatType.ACTIVE)
                .build();

        // Kakao mocks
        KakaoUserInfo userInfo = mock(KakaoUserInfo.class);
        KakaoUserInfo.KakaoAccount account = mock(KakaoUserInfo.KakaoAccount.class);
        when(kakaoClient.getAccessToken(code)).thenReturn("token");
        when(kakaoClient.getUserInfo("token")).thenReturn(userInfo);
        when(userInfo.getKakaoAccount()).thenReturn(account);
        when(account.getEmail()).thenReturn(email);

        // DB & JWT mocks
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(jwtUtil.createAccessToken(any(MemberDto.class))).thenReturn("new-at");
        when(jwtUtil.createRefreshToken(any(MemberDto.class))).thenReturn("new-rt");

        // When
        MemberKakaoLoginResponse response = authService.kakaoLogin(code);

        // Then
        assertFalse(response.isNeedSignup());
        assertEquals("new-at", response.getAccessToken());
        assertEquals("new-rt", response.getRefreshToken());

        // Verify Redis save
        verify(valueOperations).set(eq("RT:" + email), eq("new-rt"), eq(14L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("[Kakao Login] Expired (Withdrawn) User -> needSignup = true")
    void kakaoLogin_RejoinUser() {
        // Given
        String code = "auth-code";
        String email = "expired@test.com";

        Member member = Member.builder()
                .email(email)
                .status(MemberStatType.EXPIRED) // 탈퇴 상태
                .build();

        // Kakao mocks
        KakaoUserInfo userInfo = mock(KakaoUserInfo.class);
        KakaoUserInfo.KakaoAccount account = mock(KakaoUserInfo.KakaoAccount.class);
        KakaoUserInfo.KakaoAccount.Profile profile = mock(KakaoUserInfo.KakaoAccount.Profile.class);

        when(kakaoClient.getAccessToken(code)).thenReturn("token");
        when(kakaoClient.getUserInfo("token")).thenReturn(userInfo);
        when(userInfo.getKakaoAccount()).thenReturn(account);
        when(account.getEmail()).thenReturn(email);
        when(account.getProfile()).thenReturn(profile);
        when(profile.getNickname()).thenReturn("TempNick");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // When
        MemberKakaoLoginResponse response = authService.kakaoLogin(code);

        // Then
        assertTrue(response.isNeedSignup());
        assertEquals("TempNick", response.getTempNickname());
    }

    // ===================================================================================
    // 2. Signup Tests
    // ===================================================================================

    @Test
    @DisplayName("[Signup] New User -> Creates Entities")
    void signup_NewUser() {
        // Given
        mockRedis();
        String email = "new@test.com";
        MemberSignUpRequest request = createSignupRequest(email);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtUtil.createAccessToken(any(MemberDto.class))).thenReturn("at");
        when(jwtUtil.createRefreshToken(any(MemberDto.class))).thenReturn("rt");

        // When
        MemberKakaoLoginResponse response = authService.signup(request);

        // Then
        assertFalse(response.isNeedSignup());
        verify(memberRepository).save(any(Member.class));
        verify(memberPreferenceRepository).save(any(MemberPreference.class));
        verify(memberGameStatRepository).save(any(MemberGameStat.class));
    }

    @Test
    @DisplayName("[Signup] Rejoin User -> Updates Status to ACTIVE, Resets Stats")
    void signup_RejoinUser() {
        // Given
        mockRedis();
        String email = "rejoin@test.com";
        MemberSignUpRequest request = createSignupRequest(email);

        Member expiredMember = Member.builder()
                .email(email)
                .status(MemberStatType.EXPIRED)
                .build();

        // Mocks for finding existing member and related entities
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(expiredMember));
        MemberPreference mockPref = mock(MemberPreference.class);
        when(memberPreferenceRepository.findByMember(expiredMember)).thenReturn(Optional.of(mockPref));
        MemberGameStat mockStat = mock(MemberGameStat.class);
        when(memberGameStatRepository.findByMember(expiredMember)).thenReturn(Optional.of(mockStat));

        when(jwtUtil.createAccessToken(any(MemberDto.class))).thenReturn("at");
        when(jwtUtil.createRefreshToken(any(MemberDto.class))).thenReturn("rt");

        // When
        MemberKakaoLoginResponse response = authService.signup(request);

        // Then
        assertFalse(response.isNeedSignup());
        assertEquals(MemberStatType.ACTIVE, expiredMember.getStatus()); // Status changed to ACTIVE
        verify(mockPref).updatePreference(any(), any(), any(), any()); // Preferences updated
        verify(mockStat).resetStat(); // Stats reset
    }

    @Test
    @DisplayName("[Signup] Duplicate Active User -> Throws Exception")
    void signup_DuplicateUser() {
        // Given
        String email = "active@test.com";
        MemberSignUpRequest request = createSignupRequest(email);

        Member activeMember = Member.builder()
                .email(email)
                .status(MemberStatType.ACTIVE)
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(activeMember));

        // When & Then
        assertThrows(BaseException.class, () -> authService.signup(request));
    }
}
