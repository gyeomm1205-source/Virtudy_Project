package com.ssafy.virtudy.global.auth.principal;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberStatType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 시큐리티 컨텍스트(SecurityContext)에는 Member 엔티티가 직접 들어가는 게 아니라,
 * 이 UserPrincipal이 들어가기 때문에 따로 만들어줘야함.
 * 
 * UserDetails - 시큐리티가 관리하는 사용자 정보
 */

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Member member; // DB 다시 조회 안하고 바로 꺼내쓰기 위해 저장

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return member.getPassword(); // 로그인 비밀번호
    }

    @Override
    public String getUsername() {
        // jwt payload subject에도 PK로 id 들어가고, DB 조회 시 성능 상 더 효율적
        // 하지만 UserDetails의 getUsername은 로그인 ID를 의미하므로 memberId를 반환해야 함
        return member.getMemberId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 안됨
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 안됨
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 안됨
    }

    @Override
    public boolean isEnabled() {
        return member.getStatus() == MemberStatType.ACTIVE; // 활성화된 회원만 로그인 가능
    }
}
