package com.ssafy.virtudy.member.application;

import com.ssafy.virtudy.global.auth.jwt.JwtUtil;
import com.ssafy.virtudy.global.auth.oauth.KakaoClient;
import com.ssafy.virtudy.global.auth.oauth.KakaoUserInfo;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberPreference;
import com.ssafy.virtudy.member.domain.MemberStatType;
import com.ssafy.virtudy.member.dto.MemberDto;
import com.ssafy.virtudy.member.dto.MemberKakaoLoginResponse;
import com.ssafy.virtudy.member.dto.MemberSignUpRequest;
import com.ssafy.virtudy.member.repository.MemberPreferenceRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

// TODO: 멤버 필드에 적합한 값이 들어갔는지 확인 필요
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final MemberPreferenceRepository memberPreferenceRepository;

    // 1. 카카오 인증 및 로그인 여부 체크
    public MemberKakaoLoginResponse kakaoLogin(String code) {
        // 1. 카카오 토큰 받기
        String kakaoAccessToken = kakaoClient.getAccessToken(code);
        // 2. 카카오 유저 정보 받기
        KakaoUserInfo userInfo = kakaoClient.getUserInfo(kakaoAccessToken);
        String kakaoEmail = userInfo.getKakaoAccount().getEmail();

        // 3. 우리 DB에 있는지 확인
        Optional<Member> memberOpt = memberRepository.findByEmail(kakaoEmail);

        if (memberOpt.isPresent()) {
            // [CASE 1] 이미 가입된 유저 -> 바로 로그인 성공 (JWT 발급)
            Member member = memberOpt.get();

            String accessToken = jwtUtil.createAccessToken(MemberDto.from(member));
            String refreshToken = jwtUtil.createRefreshToken(MemberDto.from(member));

            // 로그인 성공! (토큰에는 우리 DB의 PK인 memberId나 id를 넣습니다)
            // MemberDto.from(member) 내부에서 memberId를 잘 매핑하고 있는지 확인하세요.
            return MemberKakaoLoginResponse.builder()
                    .needSignup(false)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            // [CASE 2] 신규 유저 -> 가입 필요 응답 (정보만 줌)
            // TODO: 확인 필요 - profileImageUrl은 뭐지
            return MemberKakaoLoginResponse.builder()
                    .needSignup(true)
                    .email(kakaoEmail)
                    .tempNickname(userInfo.getKakaoAccount().getProfile().getNickname())
                    .tempProfileImg(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                    .build();
        }
    }

    // 2. 추가 정보 입력 후 최종 회원가입
    public MemberKakaoLoginResponse signup(MemberSignUpRequest request) {
        // 중복 검사 (이미 가입된 이메일인지)
        if (memberRepository.existsByMemberId(request.getEmail())) {
            throw new BaseException(BaseErrorCode.DUPLICATED_MEMBER);
        }

        // 엔티티 생성
        Member newMember = Member.builder()
                .memberId(request.getEmail()) // 이메일을 아이디로 사용
                .password("") // 소셜 로그인은 비번 없음 (빈 문자열 or UUID)
                .nickName(request.getNickname())
                .email(request.getEmail())
                .status(MemberStatType.ACTIVE)
                .avatarGenCount(0)
                .build();

        memberRepository.save(newMember);

        MemberPreference memberPreference = MemberPreference.builder()
                .studyType(request.getStudyType())
                .targetHours(request.getTargetHours())
                .activeTime(request.getActiveTimeType())
                .member(newMember)
                .averageHours(0)
                .prefId(String.valueOf(java.util.UUID.randomUUID())) // [!!] UUID 랜덤으로 삽입 ?
                .build();

        memberPreferenceRepository.save(memberPreference);

        // 가입 완료 후 토큰 발급
        String accessToken = jwtUtil.createAccessToken(MemberDto.from(newMember));
        String refreshToken = jwtUtil.createRefreshToken(MemberDto.from(newMember));

        return MemberKakaoLoginResponse.builder()
                .needSignup(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}