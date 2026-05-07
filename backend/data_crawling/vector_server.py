from flask import Flask, request, jsonify
from embedding_engine import MeaningVectorEngine

import os
import json
import pymysql
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
engine = MeaningVectorEngine()

# [기존 기능 유지]
@app.route('/vectorize', methods=['POST'])
def vectorize():
    data = request.json
    text = data.get('text', '')

    if not text:
        return jsonify({"error" : "텍스트가 없습니다."}), 400

    vector = engine.model.encode(text).tolist()
    return jsonify(vector)

# 1. DB 설정
db_config = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 3310)),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD'),
    'db': os.getenv('DB_NAME', 'connected_m'),
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

movie_ids = []
movie_vectors = []
movie_genres = {} # 🚀 장르 데이터 저장을 위한 공간 추가

# 2. 서버 켜질 때 DB 벡터와 장르 정보를 메모리에 로드
def load_data_to_memory():
    global movie_ids, movie_vectors, movie_genres
    try:
        conn = pymysql.connect(**db_config)
        with conn.cursor() as cur:
            # 🚀 장르 정보를 조인(Join)해서 가져오는 쿼리로 확장
            sql = """
                SELECT ac.content_id, ac.embedding_vector,
                       GROUP_CONCAT(g.name) as genre_names
                FROM analysis_cache ac
                LEFT JOIN content_genre cg ON ac.content_id = cg.content_id
                LEFT JOIN genre g ON cg.genre_id = g.id
                WHERE ac.embedding_vector IS NOT NULL
                GROUP BY ac.content_id
            """
            cur.execute(sql)
            rows = cur.fetchall()
            if rows:
                movie_ids = [row['content_id'] for row in rows]
                movie_vectors = np.array([json.loads(row['embedding_vector']) for row in rows])

                # 장르를 set 형태로 변환하여 저장 (교집합 연산 최적화)
                movie_genres = {
                    row['content_id']: set(row['genre_names'].split(',')) if row['genre_names'] else set()
                    for row in rows
                }
                print(f"✅ {len(movie_ids)}개의 영화 벡터 및 장르 메모리 로드 완료! (하이브리드 준비 끝)")
    except Exception as e:
        print("❌ DB 데이터 로드 에러:", e)
    finally:
        if 'conn' in locals() and conn.open:
            conn.close()

load_data_to_memory()

# 3. 🚀 [기능 확장] AI 벡터 유사도 + 장르 일치도 하이브리드 추천
@app.route('/recommend', methods=['POST'])
def recommend():
    data = request.json
    content_id = data.get('content_id')
    limit = data.get('limit', 3)

    if content_id not in movie_ids:
        load_data_to_memory()
        if content_id not in movie_ids:
            return jsonify({"error": "해당 영화 데이터를 찾을 수 없습니다."}), 404

    # A. AI 벡터 유사도 계산 (전체 영화 대상)
    target_idx = movie_ids.index(content_id)
    target_vector = movie_vectors[target_idx].reshape(1, -1)
    vector_similarities = cosine_similarity(target_vector, movie_vectors).flatten()

    # B. 장르 가산점 및 최종 점수 계산
    target_genres = movie_genres.get(content_id, set())
    final_scores = []

    for i, other_id in enumerate(movie_ids):
        if other_id == content_id:
            continue

        # 1. 벡터 점수 (0 ~ 1)
        ai_score = float(vector_similarities[i])

        # 2. 장르 점수 (Jaccard 유사도: 교집합 / 합집합)
        other_genres = movie_genres.get(other_id, set())
        intersection = len(target_genres.intersection(other_genres))
        union = len(target_genres.union(other_genres))
        genre_score = (intersection / union) if union > 0 else 0

        # 🚀 [비중 설정] AI 유사도 70% + 장르 점수 30% 합산
        total_score = (ai_score * 0.7) + (genre_score * 0.3)

        final_scores.append({
            "id": int(other_id),
            "score": total_score
        })

    # C. 최종 점수 기준 내림차순 정렬
    final_scores.sort(key=lambda x: x['score'], reverse=True)

    return jsonify({"recommendations": final_scores[:limit]})


# 4. 🚀 자바 서버가 호출하는 [시맨틱 텍스트 검색용] 엔드포인트 유지
@app.route('/vector', methods=['POST'])
def get_vector_for_java():
    data = request.json
    text = data.get('text', '')

    if not text:
        return jsonify({"error": "텍스트가 없습니다."}), 400

    vector = engine.model.encode(text).tolist()
    return jsonify({"vector": vector})

if __name__ == '__main__':
    print(" 벡터 서버가 5000번 포트에서 대기 중입니다!")
    app.run(host='0.0.0.0', port=5000)