import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Register.css';
import logo from '../../assets/img/Project_M_Logo.png'

const Register = () => {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    realName: '',
    nickname: '',
    email: '',
    password: '',
    passwordConfirm: '',
    phoneNumber: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^0-9]/g, ''); 
    
    let formattedValue = '';
    if (value.length <= 3) {
      formattedValue = value;
    } else if (value.length <= 7) {
      formattedValue = `${value.slice(0, 3)}-${value.slice(3)}`;
    } else {
      formattedValue = `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7, 11)}`;
    }

    setFormData(prev => ({ ...prev, phoneNumber: formattedValue }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.passwordConfirm) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    const phoneRegex = /^01(?:0|1|[6-9])-(?:\d{3,4})-\d{4}$/;
    if (!phoneRegex.test(formData.phoneNumber)) {
      alert("올바른 전화번호 형식이 아닙니다.\n번호를 다시 확인해주세요! (예: 010-1234-5678)");
      return;
    }

    try {
      const { passwordConfirm, ...signupData } = formData;
      const response = await axios.post('http://localhost:8080/api/auth/signup', signupData);
      
      // 🚨 핵심 수정: navigate 대신 window.location.href 사용
      // 알림창 확인 버튼을 누르는 순간 홈으로 강제 이동하며 상태를 초기화합니다.
      alert('회원가입이 완료되었습니다! 반갑습니다.');
      window.location.href = "/"; 

    } catch (error: any) {
      const serverMessage = error.response?.data?.message;
      alert(serverMessage || '회원가입에 실패했습니다.');
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <img src={logo} alt="Project_M_Logo" className="login_modal_logo-img" />
          <p className="register-subtitle">당신의 인생 작품을 기록할 준비가 되셨나요?</p>
        </div>

        <form onSubmit={handleSubmit} className="register-form">
          <div className="input-group">
            <label>이름</label>
            <input type="text" name="realName" value={formData.realName} onChange={handleChange} autoComplete="off" required placeholder="성함을 입력해주세요" />
          </div>

          <div className="input-group">
            <label>닉네임</label>
            <input type="text" name="nickname" value={formData.nickname} onChange={handleChange} autoComplete="off" required placeholder="사용하실 닉네임을 입력해주세요" />
          </div>

          <div className="input-group">
            <label>이메일</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} autoComplete="email" required placeholder="example@email.com" />
          </div>

          <div className="input-group">
            <label>비밀번호</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange} autoComplete="new-password" required placeholder="8자 이상의 비밀번호" />
          </div>

          <div className="input-group">
            <label>비밀번호 확인</label>
            <input type="password" name="passwordConfirm" value={formData.passwordConfirm} onChange={handleChange} autoComplete="new-password" required placeholder="비밀번호를 다시 입력해주세요" />
          </div>

          <div className="input-group">
            <label>전화번호</label>
            <input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handlePhoneChange} autoComplete="tel" placeholder="010-0000-0000" maxLength={13} required />
          </div>

          <button type="submit" className="btn-submit">회원가입 완료</button>
        </form>

        <div className="register-footer">
          이미 계정이 있으신가요? <span className="login-link" onClick={() => navigate('/')}>로그인하기</span>
        </div>
      </div>
    </div>
  );
};

export default Register;