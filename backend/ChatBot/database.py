# database.py
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import os
from dotenv import load_dotenv

# .env 파일에서 환경변수 싹 긁어오기
load_dotenv()

# DB 연결 주소 (MariaDB용)
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL")

# DB랑 소통할 엔진 만들기
engine = create_engine(SQLALCHEMY_DATABASE_URL)

# 실제 소통 창구(세션) 팩토리
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 모델들이 상속받을 베이스 클래스
Base = declarative_base()

# FastAPI에서 DB를 호출하는 함수
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()