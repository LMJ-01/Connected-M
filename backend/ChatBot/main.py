import os
from typing import List

from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from pydantic import BaseModel

# 우리가 만든 파일들 임포트
import models, database, chatbot_logic

app = FastAPI()

# 1. CORS 설정 (프론트엔드 연결을 위해 필수!)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 테스트 단계에선 모두 허용!
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# --- 데이터 규격 (Pydantic 모델) ---

class SessionRequest(BaseModel):
    user_id: int


class ChatRequest(BaseModel):
    user_id: int
    session_id: int
    prompt: str


# --- API 엔드포인트 ---

@app.get("/")
async def root():
    return {"message": "Connected M AI 서버가 정상 작동 중입니다. 🚀"}


# 세션 생성 엔드포인트 (채팅방 만들기)
@app.post("/api/chat/session")
async def create_session(request: SessionRequest, db: Session = Depends(database.get_db)):
    try:
        # 새로운 채팅 세션 생성
        new_session = models.ChatSession(
            user_id=request.user_id,
            session_title="새로운 대화"  # 나중에 AI가 요약해서 업데이트하게 만들 수도 있어요!
        )
        db.add(new_session)
        db.commit()
        db.refresh(new_session)

        print(f"✨ 새로운 세션 생성됨! ID: {new_session.id}")
        return {"session_id": new_session.id}

    except Exception as e:
        db.rollback()
        print(f"❌ 세션 생성 에러: {e}")
        raise HTTPException(status_code=500, detail=f"세션 생성 실패: {str(e)}")


# 채팅 진행 엔드포인트
@app.post("/api/chat")
async def chat(request: ChatRequest, db: Session = Depends(database.get_db)):
    try:
        # 1. DB에서 분석된 영화 데이터 209개 싹 가져오기
        all_analysis = db.query(models.AnalysisCache).join(models.Content).all()

        # 2. 이전 대화 내역 10개 불러오기
        history_logs = db.query(models.ChatMessage) \
            .filter(models.ChatMessage.session_id == request.session_id) \
            .order_by(models.ChatMessage.created_at.asc()) \
            .limit(10).all()

        # 제미나이 규격(user / model)에 맞게 변환
        chat_history = []
        for log in history_logs:
            role = "user" if log.role == "user" else "model"
            chat_history.append({"role": role, "parts": [{"text": log.message}]})

        # 3. 챗봇 로직 실행 (벡터 유사도 검색 + 제미나이 답변)
        ai_answer = await chatbot_logic.get_gemini_response(all_analysis, request.prompt, chat_history)

        # 4. 이번 대화(유저 질문 + AI 답변)를 DB에 저장
        user_msg = models.ChatMessage(role="user", message=request.prompt, session_id=request.session_id)
        ai_msg = models.ChatMessage(role="assistant", message=ai_answer, session_id=request.session_id)

        db.add_all([user_msg, ai_msg])
        db.commit()

        return {"answer": ai_answer}

    except Exception as e:
        db.rollback()
        print(f"❌ 채팅 에러: {e}")
        raise HTTPException(status_code=500, detail="챗봇이 대화 중에 길을 잃었어요... 다시 시도해 주세요!")


# 서버 실행
if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8888)