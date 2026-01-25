package com.ssafy.virtudy.rank.scheduler;

import com.ssafy.virtudy.rank.service.RankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduler {
    private final RankService rankService;

    // 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleRankUpdate() {
        log.info("랭킹 업데이트 스케줄러 시작");

        rankService.updateRankingBatch();

        log.info("랭킹 업데이트 스케줄러 종료");
    }
}
