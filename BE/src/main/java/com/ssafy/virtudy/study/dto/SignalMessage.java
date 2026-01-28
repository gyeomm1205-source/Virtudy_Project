package com.ssafy.virtudy.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {
    private String type;      // offer, answer, candidate, join, leave 등
    private String sender;    // 보내는 사람의 고유 ID (memberId)
    private String receiver;  // 받는 사람의 고유 ID (메시지 종류에 따라 필요 없을 수 있음)
    private Object data;      // SDP 또는 ICE candidate 데이터
}
