package com.example.ajouevent_be_v2.config;

import com.example.ajouevent_be_v2.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("local")
@Component
@RequiredArgsConstructor
public class TestDataInitializer implements ApplicationRunner {

    private final MemberService memberService;

    @Override
    public void run(ApplicationArguments args) {
        memberService.findOrCreateByInfo("test@ajou.ac.kr", "테스트유저", "소프트웨어학과");
        log.info("테스트 계정 초기화 완료 - test@ajou.ac.kr");
    }
}
