import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Privacy.css'; //  ✨ CSS 파일을 연결해줍니다!

const Privacy: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="privacy-wrapper">
      {/* 닫기 버튼 */}
      <button 
        onClick={() => navigate('/')} 
        className="close-button"
      >
        &times;
      </button>

      <h1 className="privacy-title">개인정보처리방침</h1>

      {/* 1번 섹션 */}
      <section className="privacy-section">
        <h2>1. 외부 데이터 활용 안내</h2>
        <p><strong>콘텐츠 정보:</strong> TMDB의 API를 통해 영화/시리즈의 기본 정보, 포스터, 출연진 데이터를 제공받습니다.</p>
        <p><strong>전문가 리뷰 및 분석:</strong> 씨네21의 공개된 비평 및 메타데이터를 분석하여 서비스 내 리뷰 분석 시스템에 활용합니다.</p>
        <p className="note-text">※ 참고: 위 외부 데이터에는 이용자의 개인정보가 전송되거나 공유되지 않습니다.</p>
      </section>

      {/* 2번 섹션 */}
      <section className="privacy-section">
        <h2>2. 수집하는 개인정보 항목</h2>
        <ul>
          <li><strong>필수 항목:</strong> 이메일(ID), 비밀번호, 닉네임, 프로필 이미지</li>
          <li><strong>선택 항목:</strong> 선호 OTT 서비스, 관심 장르</li>
          <li><strong>자동 수집:</strong> IP주소, 서비스 이용 기록, 접속 로그</li>
        </ul>
      </section>

      {/* 3번 섹션 */}
      <section className="privacy-section">
        <h2>3. 개인정보의 수집 및 이용목적</h2>
        <p><strong>사용자 맞춤형 서비스:</strong> 이용자의 시청 기록 및 선호도를 분석하여 TMDB 데이터 기반의 콘텐츠를 추천합니다.</p>
        <p><strong>리뷰 분석 시스템:</strong> 씨네21 등의 전문가 데이터와 사용자의 리뷰를 결합한 AI 분석 결과 제공(Gemini AI 활용).</p>
      </section>

      {/* 4번 섹션 */}
      <section className="privacy-section">
        <h2>4. 개인정보의 보유 및 이용기간</h2>
        <p>이용자의 개인정보는 회원 탈퇴 시 또는 서비스 종료 시까지 보유하며, 목적이 달성되면 지체 없이 파기합니다.</p>
      </section>

      {/* 5번 섹션 (강조 박스) */}
      <section className="contact-box">
        <h2>5. 개인정보 보호책임자</h2>
        <p><strong>팀명:</strong> Connected_M 프로젝트팀</p>
        <p><strong>담당자:</strong> 이명준</p>
        <p><strong>담당자 URL:</strong> 
          <a href="https://github.com/LMJ-01" target="_blank" rel="noreferrer" className="contact-link">
             https://github.com/LMJ-01
          </a>
        </p>
      </section>
    </div>
  );
};

export default Privacy;