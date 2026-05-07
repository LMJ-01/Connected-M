import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import axios from 'axios'; // ✨ axios 임포트 추가
import './App.css';

// 컴포넌트 & 페이지 임포트
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import Chatbot from './components/chatbot/Chatbot';
import LoginModal from './components/common/LoginModal';
import { useAuthCheck } from './hooks/useAuthCheck';

// 페이지들
import Home from './pages/Home/Home';
import MyPage from './pages/MyPage/MyPage';
import Register from './pages/Register/Register';
import MovieDetail from './pages/MovieDetail/Moviedetail';
import Terms from './pages/Terms/Terms';
import Privacy from './pages/Privacy/Privacy';
import OAuth2RedirectHandler from './pages/Auth/OAuth2RedirectHandler';
import ExtraInfo from './pages/Auth/ExtraInfo';
import ResetPassword from './pages/Auth/ResetPassword';
import EditProfile from './pages/MyPage/EditProfile';
import WishlistPage from './pages/MyPage/WishlistPage';
import MyReviewsPage from './pages/MyPage/MyReviewsPage';
import RecentPage from './pages/MyPage/RecentPage';
import GenrePage from './pages/Genre/GenrePage';
import OttPage from './pages/Ott/OttPage';
import SearchResult from './pages/SearchResult/SearchResult';
import KeywordPage from './pages/Keyword/KeywordPage';
import AdminPage from './pages/admin/adminpage';

// ==========================================================
// ✨ Axios 전역 인터셉터 설정 (실시간 정지 감지기)
// ==========================================================
axios.interceptors.response.use(
  (response) => response, // 응답이 성공적이면 그대로 반환
  (error) => {
    // 서버 JwtAuthenticationFilter에서 보낸 403 에러 및 메시지 확인
    if (error.response && error.response.status === 403) {
      if (error.response.data.message === "계정이 정지되었습니다.") {
        alert("운영 원칙 위반으로 계정이 정지되었습니다. 즉시 로그아웃됩니다.");
        
        // 로컬 정보 삭제 및 메인 페이지로 강제 이동
        localStorage.clear(); 
        window.location.href = "/"; 
      }
    }
    return Promise.reject(error);
  }
);

const AppContent = () => {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  
  // ✨ 소셜 로그인 URL 파라미터 감지 (BANNED_USER 체크 포함)
  useAuthCheck();

  return (
    <div className="app-container">
      <Header onOpenLogin={() => setIsLoginModalOpen(true)} />
      
      <div className="main-content" style={{ minHeight: '80vh' }}> 
        <Routes>
          <Route path="/search" element={<SearchResult />} />
          <Route path="/" element={<Home />} />
          <Route path="/register" element={<Register />} />
          <Route path="/extra-info" element={<ExtraInfo />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/movie/:id" element={<MovieDetail />} />
          <Route path="/terms" element={<Terms />} /> 
          <Route path="/privacy" element={<Privacy />} />
          <Route path="/edit-profile" element={<EditProfile />} />
          <Route path="/wishlist" element={<WishlistPage />} />
          <Route path="/my-reviews" element={<MyReviewsPage />} />
          <Route path="/recent" element={<RecentPage />} />
          <Route path="/genre/:genreName" element={<GenrePage />} />
          <Route path="/ott/:providerName" element={<OttPage />} />
          <Route path="/keyword" element={<KeywordPage/>} />
          <Route path="/admin" element={<AdminPage/>} />
        </Routes>
      </div>
      
      <Footer />
      <Chatbot />
      
      {isLoginModalOpen && (
        <LoginModal onClose={() => setIsLoginModalOpen(false)} />
      )}
    </div>
  );
};

const App = () => (
  <BrowserRouter>
    <AppContent />
  </BrowserRouter>
);

export default App;