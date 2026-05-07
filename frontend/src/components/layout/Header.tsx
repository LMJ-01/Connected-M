import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../../assets/img/Project_M_Logo_Dark_R.png';
import { FaPlus } from "react-icons/fa";
import './Header.css';

// 🚀 왓챠 리얼 최종 로고 유지 
const WATCHA_LOGO = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCI+PHJlY3Qgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiByeD0iMTIiIGZpbGw9IiMwMDAiLz48cGF0aCBmaWxsPSIjRkYwNTU4IiBkPSJNMTAgMTYuNUgxNi41TDE5LjUgMzBMMjMuNSAxNC41SDI5TDMyLjUgMzBMNDAgNkg0NkwzNyAzNkgyOUwyNiAyMkwyMiAzNkgxM1oiLz48L3N2Zz4=';

interface HeaderProps {
  onOpenLogin: () => void;
}

const Header: React.FC<HeaderProps> = ({ onOpenLogin }) => {
  const [nickname, setNickname] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  const navigate = useNavigate();

  // 검색어 상태값 
  const [searchTerm, setSearchTerm] = useState<string>("");

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('token');
      const savedNickname = localStorage.getItem('nickname');
      
      if (token && token.trim() !== "") {
        setIsLoggedIn(true);
        setNickname(savedNickname || "사용자"); 
      } else {
        setIsLoggedIn(false);
        setNickname(null);
      }
    };

    checkAuth();
    window.addEventListener('nicknameUpdate', checkAuth);
    return () => {
      window.removeEventListener('nicknameUpdate', checkAuth);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('user'); 
    localStorage.removeItem('nickname');
    localStorage.removeItem('token');
    
    setIsLoggedIn(false);
    setNickname(null);
    alert("로그아웃 되었습니다.");
    
    navigate('/');
    window.location.reload();
  };

  const handleLogoClick = (e: React.MouseEvent) => {
    e.preventDefault();
    navigate('/');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && searchTerm.trim() !== "") {
      navigate(`/search?q=${encodeURIComponent(searchTerm)}`);
      setSearchTerm(""); 
    }
  };

  return (
    <header className="header">
      <div className="header-inner">
        
        <div className="header-left">
          <a href="/" onClick={handleLogoClick} className="logo">
            <img src={logo} alt="Project_M_Logo" className="logo-img" />
          </a>
          <nav className="nav-menu">
            <div className="category-parent">
              <span className="category-title">카테고리 <FaPlus /></span>
              <div className="category-dropdown">
                <p onClick={() => navigate('/genre/액션')}>액션</p>
                <p onClick={() => navigate('/genre/코미디')}>코미디</p>
                <p onClick={() => navigate('/genre/범죄')}>범죄</p>
                <p onClick={() => navigate('/genre/스릴러')}>스릴러</p>
                <p onClick={() => navigate('/genre/드라마')}>드라마</p>
                <p onClick={() => navigate('/genre/가족')}>가족</p>
                <p onClick={() => navigate('/genre/모험')}>모험</p>
                <p onClick={() => navigate('/genre/판타지')}>판타지</p>
                <p onClick={() => navigate('/genre/미스터리')}>미스터리</p>
                <p onClick={() => navigate('/genre/공포')}>공포</p>
                <p onClick={() => navigate('/genre/SF')}>SF</p>
                <p onClick={() => navigate('/genre/애니메이션')}>애니메이션</p>
                <p onClick={() => navigate('/genre/로맨스')}>로맨스</p>
                <p onClick={() => navigate('/genre/역사')}>역사</p>
                <p onClick={() => navigate('/genre/전쟁')}>전쟁</p>
                <p onClick={() => navigate('/genre/음악')}>음악</p>
                <p onClick={() => navigate('/genre/TV영화')}>TV영화</p>
                <p onClick={() => navigate('/genre/서부')}>서부</p>
              </div>
            </div>
            
            <div className="category-parent">
              <span className="category-title" style={{ fontWeight: 'normal' }}>OTT <FaPlus /></span>
              <div className="category-dropdown ott-dropdown">
                <img src="https://image.tmdb.org/t/p/original/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg" alt="Netflix" onClick={() => navigate('/ott/netflix')} className="ott-logo-btn" title="넷플릭스" />
                <img src="https://image.tmdb.org/t/p/original/97yvRBw1GzX7fXprcF80er19ot.jpg" alt="Disney+" onClick={() => navigate('/ott/disney')} className="ott-logo-btn" title="디즈니+" />
                <img src="https://image.tmdb.org/t/p/original/qHThQdkJuROK0k5QTCrknaNukWe.jpg" alt="TVING" onClick={() => navigate('/ott/tving')} className="ott-logo-btn" title="티빙" />
                <img src="https://image.tmdb.org/t/p/original/hPcjSaWfMwEqXaCMu7Fkb529Dkc.jpg" alt="Wavve" onClick={() => navigate('/ott/wavve')} className="ott-logo-btn" title="웨이브" />
                <img src={WATCHA_LOGO} alt="Watcha" onClick={() => navigate('/ott/watcha')} className="ott-logo-btn" title="왓챠" />
              </div>
            </div>

            <a href="/keyword" className="nav-link">키워드</a>
          </nav>
        </div>

        <div className="header-right">
          <input 
            type="text" 
            placeholder="검색어를 입력하세요" 
            className="search-input" 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          
          {isLoggedIn ? (
            <div className="user-info">
              <span className="user-nickname">
                <strong>{nickname || "사용자"}</strong>님
              </span>
              <button className="text-btn" onClick={handleLogout}>로그아웃</button>
            </div>
          ) : (
            <button className="text-btn" onClick={onOpenLogin}>로그인</button>
          )}
          
          <button 
            className="btn-brand" 
            onClick={() => isLoggedIn ? navigate('/mypage') : onOpenLogin()}
          >
            마이페이지
          </button>
        </div>

      </div>
    </header>
  );
};

export default Header;