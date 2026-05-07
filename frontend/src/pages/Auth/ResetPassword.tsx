import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import './ResetPassword.css';

const ResetPassword = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const token = query.get('token'); // 이메일 링크에서 token 쿼리 파라미터 추출

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!token) {
      setErrorMessage('유효한 비밀번호 재설정 토큰이 없습니다. 메일을 다시 확인해주세요.');
      return;
    }

    if (password.length < 8) {
      setErrorMessage('비밀번호는 8자 이상이어야 합니다.');
      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage('비밀번호 확인이 일치하지 않습니다.');
      return;
    }

    try {
      // 인증된 토큰을 서버에 전달하여 비밀번호 변경 처리
      await axios.post(`${import.meta.env.VITE_API_URL}/api/auth/password-reset/confirm`, {
        token,
        newPassword: password
      });

      setSuccessMessage('비밀번호가 성공적으로 변경되었습니다. 로그인 화면으로 이동합니다.');
      setTimeout(() => {
        navigate('/');
      }, 1500);
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } } };
      const message = err?.response?.data?.message || '비밀번호 재설정 중 오류가 발생했습니다.';
      setErrorMessage(message);
    }
  };

  return (
    <div className="reset-password-page">
      <div className="reset-password-card">
        <h2>비밀번호 재설정</h2>
        <p>이메일로 받은 링크에서 비밀번호를 재설정할 수 있습니다.</p>

        {!token ? (
          <div className="alert-text">
            링크가 잘못되었거나 토큰이 없습니다. 메일을 다시 확인해주세요.
          </div>
        ) : (
          <form className="reset-password-form" onSubmit={handleSubmit}>
            <div className="input-group_login">
              <label>새 비밀번호</label>
              <input
                type="password"
                placeholder="새 비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="input-group_login">
              <label>비밀번호 확인</label>
              <input
                type="password"
                placeholder="비밀번호를 다시 입력하세요"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            {errorMessage && <div className="error-text">{errorMessage}</div>}
            {successMessage && <div className="success-text">{successMessage}</div>}
            <button type="submit" className="submit-btn">비밀번호 변경</button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ResetPassword;