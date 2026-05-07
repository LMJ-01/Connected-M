import os
import json
import numpy as np
from typing import List, Dict, Any, Optional
from google import genai
from google.api_core import exceptions
from dotenv import load_dotenv

load_dotenv()


class MovieChatbot:
    """영화 추천 및 대화를 담당하는 Connected M AI 로직 클래스"""

    def __init__(self):
        # 환경 설정 및 모델 정의
        self.api_key = os.getenv("GEMINI_API_KEY")
        self.client = genai.Client(api_key=self.api_key)
        self.embedding_model = "gemini-embedding-2"
        self.chat_model = "gemini-3-flash-preview"

        # 에러 메시지 매핑
        self.error_messages = {
            exceptions.InvalidArgument: "질문 형식이 올바르지 않습니다.",
            exceptions.Unauthenticated: "인증 오류가 발생했습니다. API 키를 확인해 주세요.",
            exceptions.PermissionDenied: "모델 접근 권한이 없습니다.",
            exceptions.NotFound: "지정된 모델을 찾을 수 없습니다.",
            exceptions.ResourceExhausted: "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.",
            exceptions.InternalServerError: "AI 서버에 일시적인 문제가 생겼습니다."
        }

    def _calculate_cosine_similarity(self, target_vec: np.ndarray, source_vecs: np.ndarray) -> np.ndarray:
        """NumPy를 사용한 벡터 유사도 계산 (벡터화 최적화)"""
        dot_product = np.dot(source_vecs, target_vec)
        norms = np.linalg.norm(source_vecs, axis=1) * np.linalg.norm(target_vec)
        return dot_product / (norms + 1e-9)  # 0으로 나누기 방지

    async def _get_embedding(self, text: str) -> Optional[List[float]]:
        """텍스트를 벡터로 변환 (예외 처리 포함)"""
        try:
            res = self.client.models.embed_content(model=self.embedding_model, contents=text)
            return res.embeddings[0].values
        except Exception as e:
            self._handle_api_error(e, "Embedding")
            return None

    def _handle_api_error(self, e: Exception, stage: str):
        """중앙 집중형 에러 로그 처리"""
        error_type = type(e)
        msg = self.error_messages.get(error_type, f"알 수 없는 에러: {e}")
        print(f"❌ [{stage} Error] {msg}")

    def _format_context(self, movies: List[Dict]) -> str:
        """검색된 영화 데이터를 프롬프트용 텍스트로 변환"""
        if not movies:
            return ""
        return "\n".join([f"- {m['title']}: {m['summary']}" for m in movies])

    async def get_response(self, db_data: List[Any], user_prompt: str, history: List[Dict]) -> str:
        """메인 응답 생성 로직"""

        # 1. 벡터 검색 시도
        user_vec = await self._get_embedding(user_prompt)
        top_movies = []

        if user_vec is not None and db_data:
            # DB 데이터를 NumPy 배열로 변환하여 한꺼번에 계산
            valid_data = [d for d in db_data if d.embedding_vector]
            if valid_data:
                source_vectors = np.array([json.loads(d.embedding_vector) for d in valid_data])
                target_vector = np.array(user_vec)

                # 유사도 계산
                similarities = self._calculate_cosine_similarity(target_vector, source_vectors)

                # 상위 3개 추출
                top_indices = np.argsort(similarities)[-3:][::-1]
                for idx in top_indices:
                    movie = valid_data[idx]
                    top_movies.append({
                        "title": movie.content.title,
                        "summary": movie.summary
                    })

        # 2. 시스템 프롬프트 설정
        context = self._format_context(top_movies)
        if context:
            sys_instr = (
                "너는 영화 비서 'Connected M'이야. 아래 정보를 바탕으로 답해줘.\n"
                f"{context}\n\n"
                "규칙: 마지막 질문에 집중하고, 3편 이내로 아주 짧게 대답해."
            )
        else:
            sys_instr = "너는 영화 비서 'Connected M'이야. 현재 DB 검색이 안 되니 지식으로 짧게 답해줘."

        # 3. 답변 생성
        try:
            response = self.client.models.generate_content(
                model=self.chat_model,
                contents=history + [{"role": "user", "parts": [{"text": user_prompt}]}],
                config={"system_instruction": sys_instr, "temperature": 0.7}
            )
            return response.text.strip()
        except Exception as e:
            self._handle_api_error(e, "Generation")
            return "죄송합니다. 대화를 이어가는 중에 문제가 발생했습니다."


# 싱글톤 패턴처럼 사용할 인스턴스 생성
chatbot = MovieChatbot()