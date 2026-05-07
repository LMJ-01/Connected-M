import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Terms.css'; // ✨ CSS 파일을 연결해줍니다!

const Terms: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="terms-container">
      {/* 닫기 버튼 */}
      <button 
        className="close-button"
        onClick={() => navigate('/')} 
      >
        &times;
      </button>
      
      <h1 className="terms-title">이용약관</h1>

      <section className="terms-section">
        <h2>제1조 (목적 및 서비스 성격)</h2>
        <p>본 서비스는 개발자의 기술적 역량 강화와 데이터 분석 학습을 목적으로 제작된 <strong>비상업적 연구 프로젝트</strong>입니다.</p>
        <p>본 서비스에서 제공하는 모든 기능과 정보는 100% 무료로 제공되며, 어떠한 영리적 이익도 취하지 않습니다.</p>
      </section>

      <section className="terms-section">
        <h2>제2조 (외부 데이터 활용 및 저작권 고지)</h2>
        <p>본 서비스는 신뢰도 높은 정보 제공을 위해 아래와 같은 외부 데이터를 활용하며, 각 데이터의 저작권은 원저작권자에게 있음을 명시합니다.</p>
        <ul className="data-list">
          <li>
            <strong>TMDB (The Movie Database):</strong> 영화 및 OTT 콘텐츠의 기본 메타데이터(제목, 줄거리, 포스터, 평점 등)를 API를 통해 제공받습니다. 본 서비스는 TMDB의 공식 인증을 받은 제품이 아니며, API 가이드를 준수합니다.
          </li>
          <li>
            <strong>씨네21 (Cine21):</strong> 전문가 리뷰 및 비평 데이터를 분석 학습용으로 참조(Source)합니다. 모든 분석 결과는 원문의 출처를 존중하며, 비상업적 학술 용도로만 활용됩니다.
          </li>
        </ul>
      </section>

      <section className="terms-section">
        <h2>제3조 (이용자의 의무)</h2>
        <p>이용자는 본 서비스가 비상업적 OTT 통합플랫폼임을 인지하고 사용해야 합니다.</p>
        <p>본 서비스의 데이터를 무단으로 복제하여 상업적으로 이용하는 행위를 금지합니다.</p>
      </section>

      <section className="terms-section">
        <h2>제4조 (책임의 제한)</h2>
        <p>본 서비스는 학습용 프로젝트로서 데이터의 실시간 정확성이나 서비스의 영구적 유지보수를 보장하지 않습니다.</p>
        <p>외부 데이터(TMDB, 씨네21)의 변경이나 제공처의 사정에 따라 서비스 내용이 변경될 수 있습니다.</p>
      </section>
    </div>
  );
};

export default Terms;