import React, { useState } from 'react'; 
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; 
import logo from '../../assets/img/Project_M_Logo.png'
import './LoginModal.css'

interface LoginModalProps {
  onClose: () => void;
}

const LoginModal: React.FC<LoginModalProps> = ({ onClose }) => {
  const navigate = useNavigate();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isFindMode, setIsFindMode] = useState(false); 
  const [realName, setRealName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');

  const formatPhoneNumber = (value: string) => {
    const digits = value.replace(/[^0-9]/g, '');
    if (digits.length <= 3) return digits;
    if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
  };

  const handleSocialLogin = (provider: 'kakao' | 'google') => {
    window.location.href = `${import.meta.env.VITE_API_URL}/oauth2/authorization/${provider}`;
  };

  const handleFindPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await axios.post(`${import.meta.env.VITE_API_URL}/api/auth/password-reset/request`, null, {
        params: { email, realName, phoneNumber }
      });
      alert("입력하신 이메일로 인증 링크를 발송했습니다!");
      setIsFindMode(false);
    } catch (error) {
      console.error('인증 실패:', error);
      alert('일치하는 사용자 정보를 찾을 수 없습니다.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post(`${import.meta.env.VITE_API_URL}/api/auth/login`, {
        email,
        password
      });

      const loginData = response.data.data; 
      const token = loginData.token;
      const user = loginData.user; 
      const nick = user.nickname;

      if (nick && token) {
        localStorage.setItem('token', token);
        localStorage.setItem('nickname', nick);
        localStorage.setItem('user', JSON.stringify(user)); 
        
        alert(`${nick}님, 환영합니다!`);
        onClose();
        window.location.reload(); 
      }
    } catch (error: any) {
      if (error.response) {
        const status = error.response.status;
        const message = error.response.data.message || "";
        if (status === 403 || message.includes("BANNED") || message.includes("정지")) {
          alert('해당 계정은 정지되었습니다. 관리자에게 문의하세요.');
        } else {
          alert('아이디 또는 비밀번호가 틀렸습니다.');
        }
      } else {
        alert('서버와 연결할 수 없습니다. 관리자에게 문의하세요.');
      }
    }
  };

  const goToRegister = (e: React.MouseEvent) => {
    e.preventDefault();
    onClose();
    navigate('/register');
  };

  const handleShowFindPassword = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsFindMode(true);
    setEmail('');
    setPassword('');
    setRealName('');
    setPhoneNumber('');
  };

  const handleBackToLogin = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsFindMode(false);
    setPassword('');
    setRealName('');
    setPhoneNumber('');
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="close-btn" onClick={onClose}>✕</button>
        <img src={logo} alt="Project_M_Logo" className="login_modal_logo-img" />
        <div className="modal-header">
          <p>{isFindMode ? '비밀번호 찾기' : '반갑습니다! 로그인을 해주세요.'}</p>
        </div>
        
        {isFindMode ? (
          <form className="login-form" onSubmit={handleFindPassword}>
            <div className="input-group_login_modal">
              <label>이메일</label>
              <input 
                type="email" 
                placeholder="example@email.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="input-group_login_modal">
              <label>이름</label>
              <input 
                type="text" 
                placeholder="실명 입력" 
                value={realName}
                onChange={(e) => setRealName(e.target.value)}
                required
              />
            </div>
            <div className="input-group_login_modal">
              <label>전화번호</label>
              <input 
                type="text" 
                placeholder="010-1234-5678" 
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                required
              />
            </div>
            <button type="submit" className="submit-btn">비밀번호 찾기</button>
            <button type="button" className="secondary-btn" onClick={handleBackToLogin}>로그인으로 돌아가기</button>
          </form>
        ) : (
          <form className="login-form" onSubmit={handleSubmit}>
            <div className="input-group_login_modal">
              <label>이메일</label>
              <input 
                type="email" 
                placeholder="example@email.com" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="input-group_login_modal">
              <label>비밀번호</label>
              <input 
                type="password" 
                placeholder="••••••••" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="submit-btn">로그인하기</button>
          </form>
        )}

        {!isFindMode && (
          <div className="modal-links">
            <a href="#" onClick={handleShowFindPassword}>비밀번호 찾기</a>
            <span className="divider">•</span>
            <a href="#" onClick={goToRegister}>회원가입</a> 
          </div>
        )}

        <div className="social-login-section">
          <button className="social-btn btn-kakao" onClick={() => handleSocialLogin('kakao')}>카카오</button>
          <button className="social-btn btn-google" onClick={() => handleSocialLogin('google')}>구글</button>
        </div>
      </div>
    </div>
  );
};

export default LoginModal;