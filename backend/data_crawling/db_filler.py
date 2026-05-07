import os
import pymysql
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer

# 1. 보안 금고(.env) 로드
load_dotenv()

# 2. DB 설정 (기존 설정 유지)
db_config = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 3310)),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD'),
    'db': os.getenv('DB_NAME', 'connected_m'),
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def fill_database():
    print("🚀 [통합 작전] MariaDB 데이터 전수 조사 및 최적화 주입을 시작합니다!")

    # AI 모델 로드
    model = SentenceTransformer('jhgan/ko-sroberta-multitask')

    conn = pymysql.connect(**db_config)
    try:
        with conn.cursor() as cur:
            # --- 파트 0: 누락된 데이터 행(Row) 자동 생성 ---
            # DB 제약 조건(NOT NULL)을 피하기 위해 summary 등에 기본값을 넣습니다.
            cur.execute("""
                SELECT id FROM content
                WHERE id NOT IN (SELECT content_id FROM analysis_cache)
            """)
            missing_contents = cur.fetchall()

            if missing_contents:
                print(f"🆕 분석 테이블에 없는 새 영화 {len(missing_contents)}건 발견! 빈 접시 까는 중...")
                for c in missing_contents:
                    # id 컬럼을 추가하고, content_id와 똑같은 값을 꽂아줍니다!
                    cur.execute("""
                        INSERT INTO analysis_cache (id, content_id, positive_ratio, summary, top_keywords)
                        VALUES (%s, %s, 0.0, '분석 대기 중', '')
                    """, (c['id'], c['id']))  # id와 content_id 자리에 둘 다 영화 ID 투입!
                conn.commit()
                print(f"✅ {len(missing_contents)}건의 빈 행 생성이 완료되었습니다.")


            # --- 파트 1: 벡터(Embedding) 주입 (기존 로직 유지) ---
            cur.execute("SELECT id, content_id FROM analysis_cache WHERE embedding_vector IS NULL")
            v_rows = cur.fetchall()
            print(f"📦 벡터 주입 타겟: {len(v_rows)}건 확보!!")

            for row in v_rows:
                cur.execute("SELECT overview FROM content WHERE id = %s", (row['content_id'],))
                content = cur.fetchone()
                if content and content['overview']:
                    print(f"🔥 ID {row['id']} (영화 {row['content_id']}) 벡터 추출 중...")
                    vector_str = str(model.encode(content['overview']).tolist())
                    cur.execute("UPDATE analysis_cache SET embedding_vector = %s WHERE id = %s", (vector_str, row['id']))


            # --- 파트 2: 키워드(search_keywords) 이식 (기존 로직 유지) ---
            cur.execute("SELECT id, top_keywords FROM analysis_cache WHERE (search_keywords IS NULL OR search_keywords = '') AND top_keywords IS NOT NULL")
            k_rows = cur.fetchall()
            print(f"📦 키워드 이식 타겟: {len(k_rows)}건 확보!!")

            for row in k_rows:
                if row['top_keywords'] and row['top_keywords'].strip():
                    print(f"✨ ID {row['id']} 키워드 이식 중...")
                    cur.execute("UPDATE analysis_cache SET search_keywords = %s WHERE id = %s", (row['top_keywords'], row['id']))

            # 최종 커밋
            conn.commit()
            print("🏁 [작전 성공] 모든 데이터가 지독하게 압도적으로 보급되었습니다!!")

    except Exception as e:
        print(f"❌ 작전 중 사고 발생: {e}")
        conn.rollback()
    finally:
        conn.close()

if __name__ == "__main__":
    fill_database()