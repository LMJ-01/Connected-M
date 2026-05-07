import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './EditProfile.css';

interface UserInfo {
  id: number;
  email: string;
  nickname: string;
  realName: string;
  phoneNumber: string;
}

const EditProfile = () => {
  const navigate = useNavigate();
  const [storedUser, setStoredUser] = useState<UserInfo | null>(null);
  const [formData, setFormData] = useState({
    nickname: '',
    phoneNumber: ''
  });

  // 1. 기존 정보 불러오기
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      setStoredUser(user);
      setFormData({
        nickname: user.nickname || '',
        phoneNumber: user.phoneNumber || ''
      });
    } else {
      alert('로그인 정보가 없습니다.');
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  // 2. 전화번호 하이픈 자동 생성 (ExtraInfo.tsx와 동일)
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

  // 3. 수정 요청 처리
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const phoneRegex = /^01(?:0|1|[6-9])-(?:\d{3,4})-\d{4}$/;
    if (!phoneRegex.test(formData.phoneNumber)) {
      alert("올바른 전화번호 형식이 아닙니다.");
      return;
    }

    try {
      const token = localStorage.getItem('token');
      
      if (!storedUser) return;

      interface UpdateRequest {
        id: number;
        email: string;
        phoneNumber: string;
        password: null;
        nickname: string;
      }

      const requestData: UpdateRequest = {
        id: storedUser.id,
        email: storedUser.email,
        phoneNumber: formData.phoneNumber,
        password: null,
        nickname: formData.nickname.trim()
      };

      const response = await axios.put('http://localhost:8080/api/user/update-extra-info', requestData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data && response.data.success === true) {
        // 로컬 스토리지 정보 동기화
        const updatedUser = { ...storedUser, ...requestData };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        localStorage.setItem('nickname', requestData.nickname);

        window.dispatchEvent(new Event('nicknameUpdate'));
        
        alert("정보 수정이 완료되었습니다.");
        navigate('/mypage', { replace: true }); // 완료 후 마이페이지로 이동
      } else {
        alert(response.data?.message || "이미 사용 중인 정보가 있습니다.");
      }
    } catch (error) {
      const err = error as { response?: { data?: { message?: string } } };
      alert(err?.response?.data?.message || "정보 수정 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="edit-profile-container">
      <h2>내 정보 수정</h2>
      <form onSubmit={handleSubmit} className="edit-profile-form">
        <div className="form-group">
          <label>닉네임</label>
          <input 
            type="text" 
            value={formData.nickname} 
            onChange={(e) => setFormData(prev => ({ ...prev, nickname: e.target.value }))} 
            required 
          />
        </div>
        <div className="form-group">
          <label>휴대폰 번호</label>
          <input 
            type="text" 
            value={formData.phoneNumber} 
            onChange={handlePhoneChange} 
            maxLength={13}
            required 
          />
        </div>
        <button type="submit" className="submit-btn">수정 완료</button>
      </form>
    </div>
  );
};

export default EditProfile;