package com.ssafy.virtudy.report.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiAnalysisService {

    /**
     * AI 분석 피드백 생성
     * 계산된 리포트 스탯(지구력, 집중력 등)을 바탕으로 사용자에게 제공할 맞춤형 코멘트를 생성합니다.
     * 현재는 Rule-based 로직(Mock)으로 구현되어 있으며, 추후 Google Gemini나 GPT API로 고도화할 예정입니다.
     *
     * @param endurance 지구력 점수 (0~100)
     * @param focusDepth 집중력 점수 (0~100)
     * @param stability 안정감 점수 (0~100)
     * @param willPower 의지력 점수 (0~100)
     * @param sleepVulnerableTime 졸음 취약 시간대 ("14시" 등)
     * @return 사용자에게 보여질 최종 피드백 메시지 문자열
     */
    public String generateFeedback(int endurance, int focusDepth, int stability, int willPower, String sleepVulnerableTime) {
        StringBuilder feedback = new StringBuilder();

        // 1. 지구력 피드백
        if (endurance < 50) {
            feedback.append("지구력이 조금 부족해요. 25분 공부하고 5분 쉬는 '뽀모도로 공부법'을 시도해보는 건 어떨까요? ");
        } else if (endurance > 90) {
            feedback.append("놀라운 지구력입니다! 긴 호흡으로 공부하는 마라톤 스타일이 잘 맞으시네요. ");
        }

        // 2. 안정감(졸음) 피드백
        if (stability < 60) {
            // 졸음 시간이 감지된 경우 구체적인 시간 언급
            feedback.append(String.format("주로 %s경에 졸음이 자주 감지되었습니다. 식곤증일 수 있으니 식사량을 조절하거나 가벼운 스트레칭을 추천드려요. ", sleepVulnerableTime));
        }

        // 3. 의지력(딴짓) 피드백
        if (willPower > 80) {
            feedback.append("딴짓을 하더라도 금방 다시 집중 모드로 돌아오는 회복 탄력성이 훌륭해요! ");
        } else if (willPower < 40) {
            feedback.append("스마트폰 사용이 잦은 편이에요. 공부할 때는 휴대전화를 멀리 두는 환경 설정이 도움이 될 거예요. ");
        }

        // 4. 기본 격려 메시지 (특이사항이 적을 경우)
        if (feedback.length() == 0) {
            feedback.append("전반적으로 균형 잡힌 학습 습관을 가지고 계시네요. 꾸준함이 최고의 재능입니다. 오늘도 화이팅!");
        }

        return feedback.toString();
    }
}
