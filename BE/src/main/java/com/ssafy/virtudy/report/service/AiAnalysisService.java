package com.ssafy.virtudy.report.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class AiAnalysisService {

    private final WebClient webClient;
    private final String gmsKey;

    public AiAnalysisService(WebClient.Builder webClientBuilder, @Value("${gms.key}") String gmsKey) {
        this.webClient = webClientBuilder.baseUrl("https://gms.ssafy.io/gmsapi/api.openai.com/v1").build();
        this.gmsKey = gmsKey;
    }

    // DTOs for GPT API Request
    @Getter
    @AllArgsConstructor
    private static class GptRequest {
        private String model;
        private List<Message> messages;
    }

    @Getter
    @AllArgsConstructor
    private static class Message {
        private String role;
        private String content;
    }

    // DTOs for GPT API Response
    @Getter
    @NoArgsConstructor
    private static class GptResponse {
        private List<Choice> choices;
    }

    @Getter
    @NoArgsConstructor
    private static class Choice {
        private Message message;
    }


    /**
     * AI 분석 피드백 생성
     * 계산된 리포트 스탯(지구력, 집중력 등)을 바탕으로 GPT-4o 모델을 통해 사용자에게 제공할 맞춤형 코멘트를 생성합니다.
     *
     * @param endurance 지구력 점수 (0~100)
     * @param focusDepth 집중력 점수 (0~100)
     * @param stability 안정감 점수 (0~100)
     * @param willPower 의지력 점수 (0~100)
     * @param sleepVulnerableTime 졸음 취약 시간대 ("14시" 등)
     * @return 사용자에게 보여질 최종 피드백 메시지 문자열
     */
    public String generateFeedback(int endurance, int focusDepth, int stability, int willPower, String sleepVulnerableTime) {
        String systemPrompt = (
                "당신은 다정하고 명쾌한 학습 코치입니다. 입력받은 데이터를 분석해 따뜻한 말투(~해요)로 핵심만 담은 리포트를 작성하세요. (각 항목은 2문장 이내로 제한)\n\n" +
                        "    1. 진단: (칭찬과 현재 상태 요약)\n" +
                        "    2. 비용: (집중도 저하로 놓친 기회비용)\n" +
                        "    3. 목표: (이번 주를 위한 짧은 제언)\n" +
                        "    4. 온도: (등급 대신 0~100도 사이의 '노력 온도')\n\n" +
                        "    [데이터]\n" +
                        "    {endurance 지구력 점수, focusDepth 집중력 점수, stability 안정감 점수, willPower 의지력 점수, sleepVulnerableTime 졸음 취약 시간대} 가 순차적으로 입력됨"
        );

        String userPrompt = String.format( "%d, %d, %d, %d, %s",
                endurance, focusDepth, stability, willPower, sleepVulnerableTime
        );

        List<Message> messages = List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        );

        GptRequest requestBody = new GptRequest("gpt-4o", messages);

        try {
            GptResponse response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + gmsKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GptResponse.class)
                    .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.error("Error while calling GMS API: {}", e.getMessage());
            return "AI 피드백을 생성하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        return "AI 피드백을 생성하지 못했습니다.";
    }
}

//    GMS 미적용 로직 (최악의 경우, GMS 오류 또는 소진 시 임시 사용해야 할 수 있음)
//    public String generateFeedback(int endurance, int focusDepth, int stability, int willPower, String sleepVulnerableTime) {
//        StringBuilder feedback = new StringBuilder();
//
//        // 1. 지구력 피드백
//        if (endurance < 50) {
//            feedback.append("지구력이 조금 부족해요. 25분 공부하고 5분 쉬는 '뽀모도로 공부법'을 시도해보는 건 어떨까요? ");
//        } else if (endurance > 90) {
//            feedback.append("놀라운 지구력입니다! 긴 호흡으로 공부하는 마라톤 스타일이 잘 맞으시네요. ");
//        }
//
//        // 2. 안정감(졸음) 피드백
//        if (stability < 60) {
//            // 졸음 시간이 감지된 경우 구체적인 시간 언급
//            feedback.append(String.format("주로 %s경에 졸음이 자주 감지되었습니다. 식곤증일 수 있으니 식사량을 조절하거나 가벼운 스트레칭을 추천드려요. ", sleepVulnerableTime));
//        }
//
//        // 3. 의지력(딴짓) 피드백
//        if (willPower > 80) {
//            feedback.append("딴짓을 하더라도 금방 다시 집중 모드로 돌아오는 회복 탄력성이 훌륭해요! ");
//        } else if (willPower < 40) {
//            feedback.append("스마트폰 사용이 잦은 편이에요. 공부할 때는 휴대전화를 멀리 두는 환경 설정이 도움이 될 거예요. ");
//        }
//
//        // 4. 기본 격려 메시지 (특이사항이 적을 경우)
//        if (feedback.length() == 0) {
//            feedback.append("전반적으로 균형 잡힌 학습 습관을 가지고 계시네요. 꾸준함이 최고의 재능입니다. 오늘도 화이팅!");
//        }
//
//        return feedback.toString();
//    }
