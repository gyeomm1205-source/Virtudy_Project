import ollama

def get_weekly_report(stats):
    prompt = f"""
    당신은 다정하고 명쾌한 학습 코치입니다. 아래 데이터를 분석해 따뜻한 말투(~해요)로 핵심만 담은 리포트를 작성하세요. (각 항목은 2문장 이내로 제한)

    1. 진단: (칭찬과 현재 상태 요약)
    2. 비용: (집중도 저하로 놓친 기회비용)
    3. 목표: (내일을 위한 짧은 제언)
    4. 온도: (등급 대신 0~100도 사이의 '노력 온도')

    [데이터]
    - 순공: {stats['pure']}분 / 전체: {stats['total']}분
    - 집중도: {stats['rate']}% / 주원인: {stats['top_issue']}
    """
    
    try:
        # 스트리밍 활성화
        response = ollama.chat(model='gemma2', messages=[
            {'role': 'system', 'content': '데이터 기반의 엄격한 코치입니다.'},
            {'role': 'user', 'content': prompt}
        ], stream=True)
        
        print("\n" + "="*50)
        print("🤖 AI 코치가 리포트를 작성 중입니다...")
        
        full_text = ""
        for chunk in response:
            content = chunk['message']['content']
            print(content, end='', flush=True) # 실시간으로 터미널에 출력
            full_text += content
        
        print("\n" + "="*50)
        return full_text
        
    except Exception as e:
        return f"AI 서비스 오류: {e}"