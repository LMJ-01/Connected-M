# models.py
from sqlalchemy import BigInteger, String, Text, DateTime, ForeignKey, Integer, func, Float
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship
from datetime import datetime
from typing import List, Optional


class Base(DeclarativeBase):
    pass


# 영화 기본 정보 테이블
class Content(Base):
    __tablename__ = "content"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    overview: Mapped[Optional[str]] = mapped_column(Text)
    tmdb_id: Mapped[Optional[int]] = mapped_column(BigInteger)
    # 관계 설정: 분석 데이터랑 연결
    analysis: Mapped[Optional["AnalysisCache"]] = relationship("AnalysisCache", back_populates="content")


# 제미나이가 분석한 데이터 테이블
class AnalysisCache(Base):
    __tablename__ = "analysis_cache"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    content_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("content.id"))
    summary: Mapped[Optional[str]] = mapped_column(Text)  # 요약본
    top_keywords: Mapped[Optional[str]] = mapped_column(Text)  # 핵심 키워드
    embedding_vector: Mapped[Optional[str]] = mapped_column(Text)  # 🚀 대망의 벡터 데이터!

    content: Mapped["Content"] = relationship("Content", back_populates="analysis")


# 채팅 세션 (채팅방)
class ChatSession(Base):
    __tablename__ = "chat_session"
    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    session_title: Mapped[Optional[str]] = mapped_column(String(100))
    user_id: Mapped[int] = mapped_column(BigInteger, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    messages: Mapped[List["ChatMessage"]] = relationship("ChatMessage", back_populates="session")


# 채팅 메시지 (한 줄 한 줄)
class ChatMessage(Base):
    __tablename__ = "chat_message"
    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    message: Mapped[str] = mapped_column(Text, nullable=False)
    role: Mapped[str] = mapped_column(String(20), nullable=False)  # 'user' or 'assistant'
    session_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("chat_session.id"), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.now)
    session: Mapped["ChatSession"] = relationship("ChatSession", back_populates="messages")