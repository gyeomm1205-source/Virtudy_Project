package com.ssafy.virtudy.global.auth.principal;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * DB에서 Member를 조회해서 UserPrincipal로 변환해 리턴해주는 로딩 서비스
 * 이 서비스는 JwtProvider나 AuthenticationManager가 로그인 검증할 때 호출
 */
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 로그인 시에는 memberId(String)를 사용하여 조회
     * @param username
     * @return UserDetails 
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
        
        return new UserPrincipal(member);
    }

    /**
     * JWT 인증 시에는 PK(Long)를 사용하여 조회
     * 
     * @param id
     * @return UserDetails
     */
    public UserDetails loadUserById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

        return new UserPrincipal(member);
    }
}
