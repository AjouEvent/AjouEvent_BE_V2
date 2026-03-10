package com.example.ajouevent_be_v2.common.auth;

import com.example.ajouevent_be_v2.common.exception.auth.AuthErrorCode;
import com.example.ajouevent_be_v2.common.exception.auth.AuthException;
import com.example.ajouevent_be_v2.common.exception.member.MemberErrorCode;
import com.example.ajouevent_be_v2.common.exception.member.MemberException;
import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.domain.member.Role;
import com.example.ajouevent_be_v2.repository.port.member.MemberRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.hasParameterAnnotation(AuthUser.class)
            || parameter.hasParameterAnnotation(AuthAdmin.class)
            || parameter.hasParameterAnnotation(AuthOptional.class))
            && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken;

        if (parameter.hasParameterAnnotation(AuthOptional.class)) {
            return isAnonymous ? null : resolveMember(authentication);
        }

        if (isAnonymous) {
            throw new AuthException(AuthErrorCode.UNAUTHORIZED);
        }

        Member member = resolveMember(authentication);

        if (parameter.hasParameterAnnotation(AuthAdmin.class) && member.getRole() != Role.ADMIN) {
            throw new AuthException(AuthErrorCode.FORBIDDEN);
        }

        return member;
    }

    private Member resolveMember(Authentication authentication) {
        String email = authentication.getName();
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
