import os
import json
import time
import mysql.connector
from google import genai
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv()

# 제미나이 클라이언트 설정
api_key = os.getenv("GEMINI_API_KEY")
client = genai.Client(api_key=api_key)

# 모델 설정 (사용자 지정 모델명 유지)
MODEL_NAME = "gemini-3-flash-preview"

def get_db_connection():
    """
    MariaDB 연결을 설정합니다.
    기본 포트는 3310으로 설정되어 있습니다.
    """
    return mysql.connector.connect(
        host=os.getenv("DB_HOST"),
        port=int(os.getenv("DB_PORT", 3310)),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        database=os.getenv("DB_NAME"),
        charset='utf8mb4',
        collation='utf8mb4_general_ci'
    )

def perform_analysis(content_id):
    """
    영화 줄거리를 가져와 제미나이 분석 후 데이터베이스에 저장합니다.
    """
    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)

        print(f"Content ID {content_id}: 분석 프로세스 시작")

        # 영화 줄거리 데이터 조회
        query = "SELECT overview FROM content WHERE id = %s"
        cursor.execute(query, (content_id,))
        row = cursor.fetchone()

        if not row or not row['overview']:
            print(f"데이터 누락: 영화 ID {content_id}의 줄거리가 존재하지 않습니다.")
            return

        overview_text = row['overview']

        # 제미나이 프롬프트 구성 (키워드 10개 추출 및 장르/분위기 반영)
        prompt = f"""
        당신은 영화 마케팅 및 콘텐츠 분석 전문가입니다. 
        제공된 영화 줄거리(Overview)를 분석하여 사용자가 영화의 특징을 한눈에 파악할 수 있는 데이터를 생성하세요.

        분석 지침:
        1. summary: 줄거리의 핵심 갈등과 설정을 포함하여 100자 내외의 한국어 문장으로 요약하십시오.
        2. positive_ratio: 줄거리에서 느껴지는 희망, 즐거움 등 긍정적 요소를 수치화하십시오 (0~100 정수).
        3. top_keywords: 다음 요소를 포함하여 정확히 10개의 핵심 키워드를 선정하십시오.
           - 영화의 장르적 특징
           - 전반적인 분위기 및 톤 (예: 긴장감 넘치는, 몽환적인)
           - 줄거리 내 핵심 소재 및 주제어
           - 키워드는 쉼표(,)로 구분된 하나의 문자열로 반환하십시오.

        분석 대상 줄거리:
        {overview_text}
        """

        # AI 모델 호출
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=prompt,
            config={
                'response_mime_type': 'application/json'
            }
        )

        # 응답 데이터 파싱
        try:
            data = json.loads(response.text)
        except json.JSONDecodeError as e:
            print(f"파싱 에러: AI 응답이 유효한 JSON 형식이 아닙니다. ({e})")
            return

        # 결과 업데이트 (Upsert)
        save_query = """
            INSERT INTO analysis_cache (id, positive_ratio, summary, top_keywords, search_keywords, content_id)
            VALUES (%s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                positive_ratio = VALUES(positive_ratio),
                summary = VALUES(summary),
                top_keywords = VALUES(top_keywords),
                search_keywords = VALUES(search_keywords),
                content_id = VALUES(content_id)
        """

        cursor.execute(save_query, (
            content_id,
            data['positive_ratio'],
            data['summary'],
            data['top_keywords'],
            data['top_keywords'],
            content_id
        ))

        conn.commit()
        print(f"분석 완료: ID {content_id} (긍정 지수: {data['positive_ratio']}%)")

    except Exception as e:
        print(f"시스템 에러 발생 (ID {content_id}): {e}")
    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == "__main__":
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    # 🚀 [지독한 타겟팅] 키워드가 10개 미만인 데이터만 싹 골라옵니다.
    query = """
        SELECT content_id FROM analysis_cache 
        WHERE (LENGTH(top_keywords) - LENGTH(REPLACE(top_keywords, ',', ''))) < 9
        OR summary = '분석 대기 중'
    """
    cursor.execute(query)
    target_movies = cursor.fetchall()

    print(f"🎯 재분석 타겟: {len(target_movies)}건 발견! 요리를 시작합니다.")

    for movie in target_movies:
        perform_analysis(movie['content_id'])
        time.sleep(1)

    print("🏁 3개짜리 찌꺼기들까지 모두 10개로 업그레이드 완료!")
    cursor.close()
    conn.close()