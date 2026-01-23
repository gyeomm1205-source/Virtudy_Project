package com.ssafy.virtudy.member.application;

import com.ssafy.virtudy.global.auth.jwt.JwtUtil;
import com.ssafy.virtudy.global.auth.oauth.KakaoClient;
import com.ssafy.virtudy.global.auth.oauth.KakaoUserInfo;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.*;
import com.ssafy.virtudy.member.dto.MemberDto;
import com.ssafy.virtudy.member.dto.MemberKakaoLoginResponse;
import com.ssafy.virtudy.member.dto.MemberSignUpRequest;
import com.ssafy.virtudy.member.dto.TokenResponse;
import com.ssafy.virtudy.member.repository.MemberGameStatRepository;
import com.ssafy.virtudy.member.repository.MemberPreferenceRepository;
import com.ssafy.virtudy.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

// TODO: 멤버 필드에 적합한 값이 들어갔는지 확인 필요
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final StringRedisTemplate redisTemplate; // [RTR 로직 구현] Redis Template 주입
<<<<<<< HEAD
    private final MemberGameStatRepository memberGameStatRepository;
=======
>>>>>>> c54e331 (add: oauth 회원탈퇴, 로그아웃 로직 구현)

    /**
     * [카카오 로그인] 인증 및 로그인 로직
     * @param code
     * @return
     */
    public MemberKakaoLoginResponse kakaoLogin(String code) {
        // 1. 카카오 토큰 받기
        String kakaoAccessToken = kakaoClient.getAccessToken(code);
        // 2. 카카오 유저 정보 받기
        KakaoUserInfo userInfo = kakaoClient.getUserInfo(kakaoAccessToken);
        String kakaoEmail = userInfo.getKakaoAccount().getEmail();

        // 3. 우리 DB에 있는지 확인
        Optional<Member> memberOpt = memberRepository.findByEmail(kakaoEmail);

        if (memberOpt.isPresent()) {

            Member member = memberOpt.get();

            // [추가] 탈퇴한 회원인지 체크!
            if (member.getStatus() == MemberStatType.EXPIRED) {
                // 방법 A: 아예 로그인 막기 (에러 발생)
                throw new BaseException(BaseErrorCode.MEMBER_STATUS_NOT_VALID_ERROR);

                // TODO 방법 B: "재가입 하시겠습니까?" 묻기 위해 needSignup=true로 보내기
                // (기획에 따라 선택하세요. 지금은 A방식 추천)
            }

            // [CASE 1] 이미 가입된 유저 -> 바로 로그인 성공 (JWT 발급)
            String accessToken = jwtUtil.createAccessToken(MemberDto.from(member));
            String refreshToken = jwtUtil.createRefreshToken(MemberDto.from(member));

            // [변경] Redis에 Refresh Token 저장
            saveRefreshToken(member.getEmail(), refreshToken);

            // 로그인 성공! (토큰에는 우리 DB의 PK인 memberId나 id를 넣습니다)
            // MemberDto.from(member) 내부에서 memberId를 잘 매핑하고 있는지 확인하세요.
            return MemberKakaoLoginResponse.builder()
                    .needSignup(false)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            // [CASE 2] 신규 유저 -> 가입 필요 응답 (정보만 줌)
            return MemberKakaoLoginResponse.builder()
                    .needSignup(true)
                    .email(kakaoEmail)
                    .tempNickname(userInfo.getKakaoAccount().getProfile().getNickname())
                    .tempProfileImg(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                    .build();
        }
    }

    /**
     * [회원가입] 추가 정보 입력 받고 최종 회원가입
     * @param request
     * @return
     */
    public MemberKakaoLoginResponse signup(MemberSignUpRequest request) {
        // 중복 검사 (이미 가입된 이메일인지)
        if (memberRepository.existsByMemberId(request.getEmail())) {
            throw new BaseException(BaseErrorCode.DUPLICATED_MEMBER);
        }

        Member newMember = Member.builder()
                // [식별자 & 기본 정보]
<<<<<<< HEAD

                .memberId(java.util.UUID.randomUUID().toString())
=======
                .memberId(java.util.UUID.randomUUID().toString())       // TODO 이메일을 식별자(ID)로 사용
>>>>>>> c54e331 (add: oauth 회원탈퇴, 로그아웃 로직 구현)
                .email(request.getEmail())          // 실제 이메일 데이터
                .nickName(request.getNickname())    // 사용자 입력 닉네임
                .password("")                       // 소셜 로그인은 비밀번호 없음 (빈 값)

                // [사용자 선택 정보]
                .jobType(request.getJobType())      // 직업 (DTO Enum -> Entity Enum 바로 매핑)

                // [약관 동의 정보]
                .isServiceAgreed(request.getIsServiceAgreed())
                .isPersonalAgreed(request.getIsPersonaAgreed())
                .isVideoAgreed(request.getIsVideoAgreed())

                // [시스템 기본값 초기화] (NOT NULL 에러 방지)
                .status(MemberStatType.ACTIVE)      // 가입 즉시 활성 상태
                .avatarGenCount(0)                  // 아바타 생성 횟수 0회 초기화
                .avatar(null)                // 이미지 URL 빈 값 초기화; TODO 아바타 이미지 생성 url 투입
                .build();
        memberRepository.save(newMember);

        MemberPreference memberPreference = MemberPreference.builder()
                .studyType(request.getStudyType())          // 1. 학습 성향
                .targetHours(request.getTargetHours())      // 2. 1일 목표 공부 시간
                .activeTime(request.getActiveTime())        // 3. 활동 시간대
                .member(newMember)
                .averageHours(request.getAverageHours())    // 4. 1일 평균 공부 시간
                .prefId(String.valueOf(java.util.UUID.randomUUID()))
                .jobType(request.getJobType())
                .build();

        memberPreferenceRepository.save(memberPreference);

        // TODO MemberGameStat 도 여기서 초기화해줘야 함
        MemberGameStat memberGameStat = MemberGameStat.builder()
                .member(newMember)
                .point(0)
                .totalStudyTime(0)
                .tierScore(0)
                .build();

        memberGameStatRepository.save(memberGameStat);


        // 가입 완료 후 토큰 발급
        String accessToken = jwtUtil.createAccessToken(MemberDto.from(newMember));
        String refreshToken = jwtUtil.createRefreshToken(MemberDto.from(newMember));

        // [변경] Redis에 Refresh Token 저장
        saveRefreshToken(newMember.getEmail(), refreshToken);

        return MemberKakaoLoginResponse.builder()
                .needSignup(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * [로그아웃]
     * Redis BlackList에 AT를 저장
     * @param accessToken BL에 저장할 AT에 해당
     */
    public void logout(String accessToken) {
        // 1. Access Token 검증 및 이메일 추출
        if (!jwtUtil.validateToken(accessToken)) {
            throw new BaseException(BaseErrorCode.INVALID_TOKEN);
        }
        String email = jwtUtil.getEmail(accessToken);

        // 2. Redis에서 Refresh Token 삭제
        // 키가 있으면? 지우고 true 리턴
        // 키가 없으면? 에러 안 내고 그냥 false 리턴
        redisTemplate.delete("RT:" + email);

        // 3. Access Token 블랙리스트 등록 (남은 유효시간만큼만)
        long expiration = jwtUtil.getExpiration(accessToken);
        if (expiration > 0) {
            redisTemplate.opsForValue()
                    .set("BL:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * [회원탈퇴] Soft Delete
     * DB 삭제(delete) 대신 상태를 EXPIRED로 변경
     */
    public void withdraw(String accessToken) {
        // 1. 토큰에서 사용자 이메일 추출
        String email = jwtUtil.getEmail(accessToken);

        // 2. DB에서 유저 찾기 (없으면 에러)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

        // 3. 상태만 변경 (ACTIVE -> EXPIRED)
        // JPA의 Dirty Checking(변경 감지) 덕분에 save() 안 불러도 트랜잭션 끝나면 update 쿼리 나감
        member.updateStatus(MemberStatType.EXPIRED);

        // [변경] 로그아웃 처리와 동일하게 Redis 정리
        logout(accessToken);
    }

    /**
     * [Helper] Redis에 Refresh Token 저장 (만료시간 설정)
     * @param email RT 주인의 이메일을 key로 사용한다
     * @param refreshToken 레디스에 저장할 RT
     */
    private void saveRefreshToken(String email, String refreshToken) {
        // Key: RT:{email}, Value: {refreshToken}, TTL: 14일
        redisTemplate.opsForValue()
                .set("RT:" + email, refreshToken, 14, TimeUnit.DAYS);
    }

    /**
     * [RTR] 토큰 재발급 로직
     * @param oldRefreshToken 과거에 사용자가 들고 있던 RT
     * @return RT 검증 통과 시 유저 정보 조회 & RT 재발급 실행
     */
<<<<<<< HEAD
    public TokenResponse reissue(String oldRefreshToken) {
=======
    public MemberKakaoLoginResponse reissue(String oldRefreshToken) {
>>>>>>> c54e331 (add: oauth 회원탈퇴, 로그아웃 로직 구현)
        // 1. Refresh Token 검증
        if (!jwtUtil.validateToken(oldRefreshToken)) {
            throw new BaseException(BaseErrorCode.TOKEN_EXPIRED); // 혹은 INVALID_TOKEN
        }

        String email = jwtUtil.getEmail(oldRefreshToken);

        // 2. Redis에 저장된 RT 가져오기
        // get 메서드는 키가 없으면(만료되었거나 삭제됨) 반드시 null을 반환
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + email);

        // 3. Redis에 없거나, 가져온 토큰과 다르면 탈취 가능성 -> 차단
        // 1. null이면: 만료되어서 사라진 것
        // 2. 다르면: 해커가 탈취해서 이미 쓴 것
        if (savedRefreshToken == null || !savedRefreshToken.equals(oldRefreshToken)) {
            throw new BaseException(BaseErrorCode.INVALID_TOKEN);
        }

        // 4. 유저 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

        // 5. 새 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(MemberDto.from(member));
        String newRefreshToken = jwtUtil.createRefreshToken(MemberDto.from(member));

        // 6. Redis 업데이트 (Rotation) - 기존 키 덮어쓰기
        saveRefreshToken(email, newRefreshToken);

<<<<<<< HEAD
        return TokenResponse.builder()
=======
        return MemberKakaoLoginResponse.builder()
>>>>>>> c54e331 (add: oauth 회원탈퇴, 로그아웃 로직 구현)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}