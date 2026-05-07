'''
[ 프로젝트 핵심 모듈 및 환경 설정 불러오기 ]

1. movie_data:
   - 크롤링할 영화 목록 및 타겟 URL 정보가 담긴 데이터 저장소입니다.
2. scraper (MovieScraper):
   - 셀레니움(Selenium)을 이용해 실제로 웹사이트에 접속하고 데이터를 긁어오는 '수집기'입니다.
3. expert_saver (ExpertSaver):
   - 수집된 데이터를 MariaDB에 안전하게 저장하는 '저장소 매니저'입니다.
4. dotenv (load_dotenv):
   - .env 파일에 숨겨둔 DB 비밀번호 등 보안 정보를 읽어오는 '보안 관리자'입니다.
'''

import os
import time
import requests
from dotenv import load_dotenv

# 사용자 정의 모듈 임포트
from movie_data import MOVIE_CATEGORIES
from scraper import MovieScraper
from expert_saver import ExpertSaver, db_host, db_port, db_user, db_pw, db_name
from gemini_analysis import perform_analysis
from embedding_engine import MeaningVectorEngine

# 환경 변수 로드
load_dotenv()

DEFAULT_ANALYSIS_ID = 1  # 분석 전 임시 ID
TMDB_API_KEY = os.getenv("TMDB_API_KEY")


def get_tmdb_genres(tmdb_id):
    """
    TMDB API를 호출하여 해당 영화의 장르 명칭 리스트를 가져옵니다.
    예: ['액션', '모험', 'SF']
    """
    if not tmdb_id or tmdb_id == "0":
        return []

    url = f"https://api.themoviedb.org/3/movie/{tmdb_id}?api_key={TMDB_API_KEY}&language=ko-KR"

    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            data = response.json()
            # 장르 객체에서 이름만 추출
            genres = [g['name'] for g in data.get('genres', [])]
            return genres
        else:
            print(f"   [!] TMDB API 오류 (ID: {tmdb_id}): {response.status_code}")
    except Exception as e:
        print(f"   [!] TMDB API 호출 중 에러: {e}")

    return []


def save_data_and_crawling():
    """
    1단계: 리뷰 수집, TMDB 장르 확보, 의미 좌표 생성 및 DB 적재
    """
    print("\n🚀 [1단계] 데이터 수집 및 DB 적재 시작")

    # 도구 모음 초기화
    scraper = MovieScraper(headless=True)
    engine = MeaningVectorEngine()
    db = ExpertSaver(
        host=db_host,
        port=db_port,
        user=db_user,
        password=db_pw,
        db=db_name
    )

    for genre_key, movies in MOVIE_CATEGORIES.items():
        print(f"\n--- 카테고리: {genre_key} ---")

        for movie_name, ids in movies.items():
            cine21_id, tmdb_id = ids
            print(f"\n🎬 '{movie_name}' 처리 시작...")
            print(f"   - Cine21 ID: {cine21_id} / TMDB ID: {tmdb_id}")

            try:
                # 1. 전문가 리뷰 크롤링
                reviews = scraper.get_expert_reviews(cine21_id, limit=10)

                # 2. 의미 좌표(Vector) 생성 (리뷰가 있을 때만)
                meaning_vector = None
                if reviews:
                    print(f"   🧠 리뷰 {len(reviews)}건 발견! 의미 좌표 추출 중...")
                    all_text = " ".join([r['content'] for r in reviews])
                    meaning_vector = engine.generate_vector(all_text)
                else:
                    print(f"   ⚠️ 리뷰가 없습니다. 영화 정보만 저장합니다.")

                # 3. TMDB에서 실제 장르 정보 가져오기
                print(f"   🌐 TMDB에서 장르 정보 가져오는 중...")
                tmdb_genres = get_tmdb_genres(tmdb_id)
                if not tmdb_genres:
                    # TMDB 정보가 없을 경우 카테고리 이름이라도 사용 (범죄/스릴러 등 처리)
                    tmdb_genres = genre_key.split('/')

                # 4. DB 저장 (영화 정보, 리뷰 리스트, 장르 리스트, 벡터)
                db.save_review(
                    cine21_id=cine21_id,
                    tmdb_id=tmdb_id,
                    analysis_id=DEFAULT_ANALYSIS_ID,
                    movie_title=movie_name,
                    reviews=reviews,  # 빈 리스트일 수 있음
                    genre_list=tmdb_genres,  # TMDB 장르 리스트 전달
                    vector=meaning_vector
                )

            except Exception as e:
                print(f"   ❌ '{movie_name}' 처리 중 오류 발생: {e}")

            # API 및 서버 부하 방지를 위한 짧은 휴식
            time.sleep(0.5)

    scraper.close()
    print("\n🏁 1단계 수집 및 적재 완료!")


def run_all_analysis():
    """
    2단계: DB에 저장된 모든 영화를 제미나이 AI로 분석
    """
    print("\n🤖 [2단계] 제미나이 AI 분석 시작")

    # DB에서 실제 등록된 영화 ID 목록 가져오기
    db_manager = ExpertSaver(host=db_host, port=db_port, user=db_user, password=db_pw, db=db_name)
    conn = db_manager._get_connection()
    movie_ids = []

    try:
        with conn.cursor() as cur:
            cur.execute("SELECT id FROM content")
            movie_ids = [row['id'] for row in cur.fetchall()]
    finally:
        conn.close()

    if not movie_ids:
        print("⚠️ 분석할 영화 데이터가 DB에 없습니다.")
        return

    print(f"✅ 총 {len(movie_ids)}개의 영화를 분석합니다.")

    for movie_id in movie_ids:
        try:
            # gemini_analysis.py의 분석 함수 호출
            perform_analysis(movie_id)
            # API 할당량 제한(Rate Limit) 방지를 위해 1.5초 휴식
            time.sleep(1.5)
        except Exception as e:
            print(f"   ❌ 영화 ID {movie_id} 분석 중 에러: {e}")

    print("\n✨ 모든 분석 작업이 종료되었습니다!")


if __name__ == "__main__":
    # 실행 순서: 크롤링 및 저장 -> AI 분석
    save_data_and_crawling()
    run_all_analysis()