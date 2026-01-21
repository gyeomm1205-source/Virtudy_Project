package com.ssafy.virtudy.global.auth.resolver;

import com.ssafy.virtudy.global.auth.annotation.CurrentMember;
import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            String memberId = userPrincipal.getUsername();
            return memberRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));
        }

        throw new BaseException(BaseErrorCode.INVALID_JWT);
    }
}
