import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Footer.css';

const Footer: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const navigate = useNavigate();

  const teamMembers = [
    { id: 2, name: 'Member 2', github: 'https://github.com/Joohang', img: '/image/한규대팀원 깃허브로고.png', color: '#FFFF99' },
    { id: 3, name: 'Member 3', github: 'https://github.com/rjsdyd', img: '/image/이건용팀원 깃허브로고.png', color: '#f472b6' },
    { id: 4, name: 'Member 4', github: 'https://github.com/Sleep404NF', img: '/image/이승우팀원 깃허브로고.png', color: '#e5e7eb' },
    { id: 5, name: 'Member 5', github: 'https://github.com/dnwls0022', img: '/image/조우진 팀원깃허브로고.png', color: '#4b5563' },
    { id: 1, name: 'Member 1', github: 'https://github.com/LMJ-01', img: '/image/팀장님 깃허브로고.png', color: '#fbbf24' },
  ];

  const goToUrl = (url: string) => {
    if (url) window.open(url, '_blank', 'noopener,noreferrer');
  };

  const navigateTo = (path: string) => {
    navigate(path);
    window.scrollTo(0, 0);
  };

  return (
    <footer className="footer-wrapper">
      <div className="footer-container">
        
        {/* 1. 왼쪽 섹션: 팀 로고 + 아래 협력사 로고 */}
        <div className="footer-left-section">
          <div className="footer-logo" onClick={() => navigateTo('/')}>
            <img src="/image/팀 로고.png" alt="Team Logo" />
          </div>
          <div className="link-group">
            <img 
              src='/image/TMDB로고.png' 
              alt="TMDB"
              className="tmdb-logo-img"
              onClick={() => goToUrl('https://www.themoviedb.org/')}
            />
            <img 
              src='/image/씨네21로고.png' 
              alt="씨네21"
              className="cine21-logo-img"
              onClick={() => goToUrl('http://www.cine21.com/')}
            />
          </div>
        </div>

        {/* 2. 중앙 텍스트 정보 */}
        <div className="footer-center">
          <p className="main-text">이 사이트는 TMDB와 씨네21 데이터를 기반으로 하여 제작되었습니다.</p>
          <p className="sub-text">이윤을 추구하지 않는 비영리 프로젝트로써 <span className="brand-name">Connected_M</span>은 무료입니다.</p>
          <p className="copyright" onClick={() => setIsModalOpen(true)}>© 2026 Connected_M ⓘ</p>
        </div>

        {/* 3. 오른쪽 섹션: 팀원 아이콘 + 내비게이션 */}
        <div className="footer-right">
          <div className="team-icons">
            {teamMembers.map((member) => (
              <div 
                key={member.id} 
                className="member-icon"
                onClick={() => goToUrl(member.github)}
                style={{ border: `3px solid ${member.color}` }}
              >
                <img src={member.img} alt={member.name} />
              </div>
            ))}
          </div>
          <div className="footer-nav">
            <div className="nav-links">
              <span onClick={() => navigateTo('/terms')}>이용약관</span>
              <span onClick={() => navigateTo('/privacy')}>개인정보처리방침</span>
            </div>
          </div>
        </div>
      </div>

      {/* 모달 */}
      {isModalOpen && (
        <div className="modal-overlay-footer" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content-footer" onClick={(e) => e.stopPropagation()}>
            <h3>Connected_M</h3>
            <p>안녕하세요! Connected_M은 OTT 시대에 사용자에게 <br />
              최적화된 콘텐츠 분석을 제공하기 위해 <br />
              시작된 비상업적 프로젝트입니다.</p>
            <button className="modal-close-btn" onClick={() => setIsModalOpen(false)}>닫기</button>
          </div>
        </div>
      )}
    </footer>
  );
};

export default Footer;