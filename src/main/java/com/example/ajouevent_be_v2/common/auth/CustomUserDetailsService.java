package com.example.ajouevent_be_v2.common.auth;

import com.example.ajouevent_be_v2.domain.member.Member;
import com.example.ajouevent_be_v2.repository.port.member.MemberRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    public UserDetails loadUserByUsername(@NonNull String memberId) throws UsernameNotFoundException {
        Member member = memberRepositoryPort.findById(Long.valueOf(memberId))
            .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저가 없습니다."));
        return new CustomUserDetails(member);
    }
}
