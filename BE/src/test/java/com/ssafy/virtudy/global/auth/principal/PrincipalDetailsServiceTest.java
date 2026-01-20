package com.ssafy.virtudy.global.auth.principal;

import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PrincipalDetailsServiceTest {

    @InjectMocks
    private PrincipalDetailsService principalDetailsService;

    @Mock
    private MemberRepository memberRepository;

    /**
     * loadUserByUsername 성공 테스트
     * Given: DB에 존재하는 memberId가 주어졌을 때
     * When: principalDetailsService.loadUserByUsername을 호출하면
     * Then: 해당 Member 정보를 담은 UserDetails 객체가 반환되어야 함
     */
    @Test
    @DisplayName("loadUserByUsername 성공 테스트")
    void loadUserByUsername_Success() {
        // given
        String memberId = "testUser";
        Member member = Member.builder()
                .memberId(memberId)
                .password("password")
                .build();
        given(memberRepository.findByMemberId(memberId)).willReturn(Optional.of(member));

        // when
        UserDetails userDetails = principalDetailsService.loadUserByUsername(memberId);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(memberId);
    }

    /**
     * loadUserByUsername 실패 테스트 (존재하지 않는 유저)
     * Given: DB에 존재하지 않는 memberId가 주어졌을 때
     * When: principalDetailsService.loadUserByUsername을 호출하면
     * Then: UsernameNotFoundException 예외가 발생해야 함
     */
    @Test
    @DisplayName("loadUserByUsername 실패 테스트 - 존재하지 않는 유저")
    void loadUserByUsername_NotFound() {
        // given
        String memberId = "unknown";
        given(memberRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> principalDetailsService.loadUserByUsername(memberId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("해당하는 유저를 찾을 수 없습니다");
    }

    /**
     * loadUserById 성공 테스트
     * Given: DB에 존재하는 회원 ID (PK)가 주어졌을 때
     * When: principalDetailsService.loadUserById를 호출하면
     * Then: 해당 Member 정보를 담은 UserDetails 객체가 반환되어야 함
     */
    @Test
    @DisplayName("loadUserById 성공 테스트")
    void loadUserById_Success() {
        // given
        Long id = 1L;
        Member member = Member.builder()
                .id(id)
                .memberId("testUser")
                .build();
        given(memberRepository.findById(id)).willReturn(Optional.of(member));

        // when
        UserDetails userDetails = principalDetailsService.loadUserById(id);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testUser");
    }

    /**
     * loadUserById 실패 테스트 (존재하지 않는 유저)
     * Given: DB에 존재하지 않는 회원 ID (PK)가 주어졌을 때
     * When: principalDetailsService.loadUserById를 호출하면
     * Then: BaseException 예외가 발생해야 함
     */
    @Test
    @DisplayName("loadUserById 실패 테스트 - 존재하지 않는 유저")
    void loadUserById_NotFound() {
        // given
        Long id = 99L;
        given(memberRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> principalDetailsService.loadUserById(id))
                .isInstanceOf(BaseException.class);
    }
}
