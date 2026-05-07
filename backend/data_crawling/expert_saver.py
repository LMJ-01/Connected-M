'''
[ DB 연결 및 보안 도구 상자 ]

1. pymysql: 
   - 파이썬에서 만든 데이터를 데이터베이스(MariaDB)로 실어 나르는 '운반 트럭'입니다.
2. os: 
   - 우리 컴퓨터 시스템에 접근하는 '관리자'입니다. 파일 경로를 찾거나 시스템 설정을 읽을 때 씁니다.
3. load_dotenv: 
   - 비밀번호가 적힌 .env 파일을 읽어오는 '보안 요원'입니다. 
'''

import pymysql
import os
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

db_host = os.getenv('DB_HOST')
db_port = int(os.getenv('DB_PORT')) if os.getenv('DB_PORT') else 3310
db_user = os.getenv('DB_USER')
db_pw = os.getenv('DB_PASSWORD')
db_name = os.getenv('DB_NAME')


class ExpertSaver:
    def __init__(self, host, user, password, db, port=3310):
        self.config = {
            'host': host,
            'user': user,
            'password': password,
            'db': db,
            'port': port,
            'charset': 'utf8mb4',
            'cursorclass': pymysql.cursors.DictCursor
        }
        self._init_db()

    def _get_connection(self):
        return pymysql.connect(**self.config)

    def _init_db(self):
        """테이블 생성 및 컬럼/인덱스 자가 치유"""
        conn = self._get_connection()
        try:
            with conn.cursor() as cur:
                # 1. content 테이블 필수 컬럼 체크
                cur.execute("SHOW COLUMNS FROM content")
                columns = [col['Field'] for col in cur.fetchall()]
                if 'cine21_id' not in columns:
                    cur.execute("ALTER TABLE content ADD COLUMN cine21_id VARCHAR(50) UNIQUE")
                if 'tmdb_id' not in columns:
                    cur.execute("ALTER TABLE content ADD COLUMN tmdb_id VARCHAR(50)")

                # 2. expert_review 테이블 생성
                cur.execute('''
                    CREATE TABLE IF NOT EXISTS `expert_review` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        `content_id` BIGINT NOT NULL,
                        `analysis_id` BIGINT NOT NULL,
                        `movie_title` VARCHAR(255) NOT NULL,
                        `critic_name` VARCHAR(100) NOT NULL,
                        `rating` VARCHAR(10) NOT NULL,
                        `comment` TEXT NOT NULL,
                        `source` VARCHAR(50) DEFAULT 'Cine21',
                        `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_content_id (content_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                ''')

                # 3. content_genre 매핑 테이블 생성 (장르 저장을 위해 필수)
                cur.execute('''
                    CREATE TABLE IF NOT EXISTS `content_genre` (
                        `content_id` BIGINT NOT NULL,
                        `genre_id` BIGINT NOT NULL,
                        PRIMARY KEY (`content_id`, `genre_id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                ''')

                # 중복 리뷰 방지를 위한 유니크 인덱스
                try:
                    cur.execute(
                        "CREATE UNIQUE INDEX idx_unique_review ON expert_review (content_id, critic_name, comment(100))")
                except:
                    pass

            conn.commit()
            print(f"✅ DB 초기화 및 장르 매핑 테이블 준비 완료")
        except Exception as e:
            print(f"⚠️ 초기화 알림: {e}")
        finally:
            conn.close()

    def save_review(self, cine21_id, tmdb_id, analysis_id, movie_title, reviews, genre_list, vector=None):
        """
        영화 정보, 리뷰, TMDB 장르 정보를 통합 저장합니다.
        genre_list: ['액션', 'SF'] 형태의 리스트
        """
        conn = self._get_connection()
        try:
            with conn.cursor() as cur:
                # 외래 키 체크 일시 중지 (안전한 트랜잭션 처리)
                cur.execute("SET FOREIGN_KEY_CHECKS = 0;")

                # [1] 의미 좌표(Vector) 업데이트
                if vector:
                    cur.execute(
                        "UPDATE analysis_cache SET embedding_vector = %s WHERE id = %s",
                        (vector, analysis_id)
                    )

                # [2] 영화 정보 확인 및 생성
                cur.execute("SELECT id FROM content WHERE cine21_id = %s", (cine21_id,))
                row = cur.fetchone()

                if row:
                    content_id = row['id']
                    # tmdb_id 강제 업데이트 (데이터 정정)
                    cur.execute("UPDATE content SET tmdb_id = %s WHERE id = %s", (tmdb_id, content_id))
                else:
                    cur.execute(
                        "INSERT INTO content (title, tmdb_id, cine21_id) VALUES (%s, %s, %s)",
                        (movie_title, tmdb_id, cine21_id)
                    )
                    content_id = cur.lastrowid

                # [3] TMDB 장르 매핑 저장 (추가된 핵심 로직!)
                if genre_list:
                    for g_name in genre_list:
                        # genre 테이블에서 ID 조회
                        cur.execute("SELECT id FROM genre WHERE name = %s", (g_name.strip(),))
                        genre_row = cur.fetchone()

                        if genre_row:
                            genre_id = genre_row['id']
                            # content_genre 테이블에 매핑 (중복 무시)
                            cur.execute("INSERT IGNORE INTO content_genre (content_id, genre_id) VALUES (%s, %s)",
                                        (content_id, genre_id))
                    print(f"📁 '{movie_title}' 장르 {len(genre_list)}개 매핑 완료")

                # [4] 리뷰 저장
                if reviews:
                    sql = """
                        INSERT IGNORE INTO expert_review 
                        (content_id, analysis_id, movie_title, critic_name, rating, comment, source) 
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                    """
                    data_insert = [
                        (content_id, analysis_id, movie_title, r['critic'], str(r['score']), r['content'], 'Cine21')
                        for r in reviews
                    ]
                    cur.executemany(sql, data_insert)
                    print(f"✨ '{movie_title}' 리뷰 {len(reviews)}건 저장 완료!")
                else:
                    print(f"✅ '{movie_title}' 영화 정보 등록 완료 (리뷰 없음)")

                # 외래 키 체크 다시 활성화
                cur.execute("SET FOREIGN_KEY_CHECKS = 1;")

            conn.commit()
        except Exception as e:
            print(f"❌ 저장 에러 발생: {e}")
            conn.rollback()
        finally:
            conn.close()