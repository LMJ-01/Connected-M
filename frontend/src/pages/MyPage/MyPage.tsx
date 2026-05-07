import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './MyPage.css';

interface UserInfo {
  id: number;
  email: string;
  nickname: string;
  realName: string;
  phoneNumber: string;
  role: string; 
}

const MyPage: React.FC = () => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate();
  const [isAdmin, setIsAdmin] = useState<boolean>(false);

  /* ──────────────────────────────────────────────────────────
      1. 회원탈퇴 상태 및 모달 관리
  ────────────────────────────────────────────────────────── */
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [isWithdrawing, setIsWithdrawing] = useState<boolean>(false);

  /* ──────────────────────────────────────────────────────────
      2. 실제 회원탈퇴 처리 함수 (401 방어 로직 추가)
  ────────────────────────────────────────────────────────── */
  const confirmDeleteAccount = async (): Promise<void> => {
    if (isWithdrawing) return;

    const token = localStorage.getItem('token');
    
    // [방어 로직] 토큰이 없으면 요청을 보내지 않고 입구 컷
    if (!token) {
      alert("로그인 세션이 만료되었습니다. 다시 로그인해주세요.");
      navigate('/');
      return;
    }

    try {
      setIsWithdrawing(true); 

      // 401 방지를 위해 헤더를 지독하게 체크하며 전송
      await axios.patch(`${import.meta.env.VITE_API_URL}/api/user/me/withdraw`, {}, {
        headers: {
          'Authorization': `Bearer ${token}` 
        }
      });

      localStorage.removeItem('user');
      localStorage.removeItem('nickname');
      localStorage.removeItem('token');
      
      alert("회원탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
      
      navigate('/');
      window.location.reload();
    } catch (error: any) {
      console.error("탈퇴 실패:", error);
      
      // 401 에러 시 사용자에게 알림
      if (error.response?.status === 401) {
        alert("인증에 실패했습니다. 다시 로그인 후 시도해주세요.");
      } else {
        alert("처리 중 오류가 발생했습니다.");
      }
      
      setIsWithdrawing(false);
    }
  };

  /* ──────────────────────────────────────────────────────────
      3. 이벤트 핸들러
  ────────────────────────────────────────────────────────── */
  const handleDeleteAccount = () => {
    setIsModalOpen(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    localStorage.removeItem('nickname');
    localStorage.removeItem('token');
    alert("로그아웃 되었습니다.");
    navigate('/');
    window.location.reload();
  };

  /* ──────────────────────────────────────────────────────────
      4. 유저 정보 로딩 로직
  ────────────────────────────────────────────────────────── */
  useEffect(() => {
    const fetchUser = async () => {
      if (isWithdrawing) return;

      try {
        setLoading(true);
        const storedUser = localStorage.getItem('user');
        
        if (!storedUser) {
          alert("로그인이 필요합니다.");
          navigate('/');
          return;
        }

        const parsedUser = JSON.parse(storedUser);
        const userId = parsedUser.id;

        if (parsedUser.role === 'ROLE_ADMIN') {
          setIsAdmin(true);
        }
        
        const response = await axios.get(`${import.meta.env.VITE_API_URL}/api/user/${userId}`);
        const serverUserData = response.data.data;
        setUser(serverUserData);

      } catch (error) {
        console.error("유저 정보 로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, [navigate, isWithdrawing]);

  if (loading) return <div className="loading-mypage">로딩 중...</div>;
  if (!user) return null;

  return (
    <div className="mypage-layout-mypage">
      <div className="mypage-container-mypage">
        <section className="profile-summary-card-mypage">
          <div className="us-pro-mypage">
            <div className="profile-avatar-mypage">{user.nickname.charAt(0)}</div>
            <div className="profile-info-text-mypage">
              <h1 className="user-nickname-mypage">{user.nickname}님, 안녕하세요!</h1>
              <p className="user-welcome-msg-mypage">오늘도 Connected M과 함께 즐거운 시간 보내세요.</p>
            </div>
          </div>
          {isAdmin && (
            <button className="btn-admin-action-mypage" onClick={() => navigate('/admin')}>
              관리자페이지
            </button>
          )}
        </section>

        <div className="mypage-content-grid-mypage">
          <aside className="content-side-left-mypage">
            <section className="info-card-fixed-mypage">
              <div className="info-header-fixed-mypage">
                <h2 className="info-title-fixed-mypage">내 정보</h2>
              </div>
              <div className="info-body-fixed-mypage">
                <div className="info-row-fixed-mypage">
                  <span className="info-label-fixed-mypage">이메일</span>
                  <span className="info-value-fixed-mypage">{user.email}</span>
                </div>
                <div className="info-row-fixed-mypage">
                  <span className="info-label-fixed-mypage">이름</span>
                  <span className="info-value-fixed-mypage">{user.realName}</span>
                </div>
                <div className="info-row-fixed-mypage">
                  <span className="info-label-fixed-mypage">전화번호</span>
                  <span className="info-value-fixed-mypage">{user.phoneNumber || '010-0000-0000'}</span>
                </div>
                
                <button className="btn-edit-action-mypage" onClick={() => navigate('/edit-profile')}>
                  정보 수정
                </button>
                <button className="delete-account-link-mypage" onClick={handleDeleteAccount}>
                  회원탈퇴
                </button>
              </div>
            </section>
          </aside>

          <div className="content-side-right-mypage">
            <section className="simple-link-card-mypage recent-item" onClick={() => navigate('/recent')}>
              <span className="link-title-mypage">최근에 본 목록</span>
              <span className="link-arrow-mypage">❯</span>
            </section>
            <section className="simple-link-card-mypage wish-item" onClick={() => navigate('/wishlist')}>
              <span className="link-title-mypage">찜 목록</span>
              <span className="link-arrow-mypage">❯</span>
            </section>
            <section className="simple-link-card-mypage review-item" onClick={() => navigate('/my-reviews')}>
              <span className="link-title-mypage">내가 작성한 리뷰</span>
              <span className="link-arrow-mypage">❯</span>
            </section>
          </div>
        </div>
      </div>

      {isModalOpen && (
        <div className="modal-overlay-mypage">
          <div className="modal-content-mypage">
            <div className="modal-header-mypage">
              <span className="modal-warning-icon-mypage">⚠️</span>
              정말 탈퇴하시겠습니까?
            </div>
            <p className="modal-body-mypage">
              탈퇴 시 모든 데이터 및 개인정보가 <strong>영구 삭제</strong>되며 복구되지 않습니다.
            </p>
            <div className="modal-footer-mypage">
              <button 
                className="btn-confirm-mypage" 
                onClick={confirmDeleteAccount}
                disabled={isWithdrawing}
              >
                {isWithdrawing ? "처리 중..." : "확인"}
              </button>
              <button className="btn-cancel-mypage" onClick={() => setIsModalOpen(false)}>취소</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyPage;