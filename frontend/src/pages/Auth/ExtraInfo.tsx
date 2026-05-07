import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './ExtraInfo.css';

const ExtraInfo = () => {
  const [formData, setFormData] = useState({
    email: '',
    phoneNumber: '',
    nickname: ''
  });
  const [nicknameError, setNicknameError] = useState('');
  const [storedUser] = useState(() => {
    const storedUserStr = localStorage.getItem('user');
    return storedUserStr ? JSON.parse(storedUserStr) : null;
  });
  const isTempEmail = storedUser?.email?.includes('@connectedm.temp');
  const params = new URLSearchParams(window.location.search);
  const needNickname = params.get('needNickname') === 'true' || storedUser?.nickname?.startsWith('tmp_social_');

  useEffect(() => {
    const nextForm: Partial<typeof formData> = {};
    if (storedUser && !isTempEmail) {
      nextForm.email = storedUser.email;
    }
    // ✨ [수정] 닉네임이 tmp_social_ 로 시작하는 임시 닉네임이 아닐 때만 폼에 미리 채워넣음
    if (storedUser?.nickname && !storedUser.nickname.startsWith('tmp_social_')) {
      nextForm.nickname = storedUser.nickname;
    }
    if (storedUser?.phoneNumber) {
      nextForm.phoneNumber = storedUser.phoneNumber;
    }
    if (Object.keys(nextForm).length > 0) {
      setFormData(prev => ({ ...prev, ...nextForm }));
    }
  }, [isTempEmail, storedUser]);

  const checkNicknameAvailability = async (nickname: string) => {
    if (!nickname || nickname.trim().length < 2) {
      setNicknameError('닉네임은 최소 2자 이상이어야 합니다.');
      return false;
    }
    try {
      const response = await axios.get(`${import.meta.env.VITE_API_URL}/api/user/check-nickname`, {
        params: { nickname: nickname.trim() }
      });
      const exists = response.data.data;
      if (exists && nickname.trim() !== storedUser?.nickname) {
        const duplicateMessage = '이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해 주세요.';
        setNicknameError(duplicateMessage);
        alert(duplicateMessage);
        return false;
      }
      setNicknameError('');
      return true;
    } catch (error) {
      setNicknameError('닉네임 확인 중 오류가 발생했습니다.');
      return false;
    }
  };

  // ✨ [추가] 실시간 전화번호 하이픈 자동 생성 핸들러
  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^0-9]/g, ''); // 숫자만 남기기
    
    let formattedValue = '';
    if (value.length <= 3) {
      formattedValue = value;
    } else if (value.length <= 7) {
      formattedValue = `${value.slice(0, 3)}-${value.slice(3)}`;
    } else {
      formattedValue = `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7, 11)}`;
    }

    setFormData({ ...formData, phoneNumber: formattedValue });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // ✨ [추가] 전화번호 유효성 검사 (010-XXXX-XXXX 형식)
    const phoneRegex = /^01(?:0|1|[6-9])-(?:\d{3,4})-\d{4}$/;
    
    if (!phoneRegex.test(formData.phoneNumber)) {
      alert("올바른 전화번호 형식이 아닙니다.\n번호를 다시 확인해주세요! (예: 010-1234-5678)");
      return; // ❌ 여기서 중단! 서버에 요청을 보내지 않습니다.
    }

    try {
      const token = localStorage.getItem('token');
      if (needNickname) {
        const nicknameValid = await checkNicknameAvailability(formData.nickname);
        if (!nicknameValid) {
          return;
        }
      }

      interface UpdateRequest {
        id: number;
        email: string;
        phoneNumber: string;
        password: null;
        nickname?: string;
      }
      const requestData: UpdateRequest = {
        id: storedUser.id,
        email: formData.email,
        phoneNumber: formData.phoneNumber,
        password: null
      };

      if (needNickname) {
        requestData.nickname = formData.nickname.trim();
      }

      const response = await axios.put(`${import.meta.env.VITE_API_URL}/api/user/update-extra-info`, requestData, {
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.data?.success) {
        const updatedUser = { 
          ...storedUser, 
          email: formData.email, 
          phoneNumber: formData.phoneNumber,
          ...(needNickname ? { nickname: formData.nickname.trim() } : {})
        };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        if (needNickname) {
          localStorage.setItem('nickname', formData.nickname.trim());
        }
        alert("모든 정보가 등록되었습니다. 환영합니다!");
        window.location.href = "/";
      } else {
        const backendMessage = response.data?.message || '알 수 없는 오류가 발생했습니다.';
        if (backendMessage.includes('닉네임')) {
          setNicknameError(backendMessage);
          alert(backendMessage);
          return;
        }
        alert(backendMessage);
      }
  } catch (error) {
    const err = error as { response?: { data?: { message?: string; error?: string } } };
    const backendMessage = err?.response?.data?.message || err?.response?.data?.error;
      if (backendMessage) {
        if (backendMessage.includes('닉네임')) {
          setNicknameError(backendMessage);
          alert(backendMessage);
          return;
        }
        alert(backendMessage);
      } else {
        alert("정보 업데이트 중 오류가 발생했습니다. 번호가 너무 길거나 형식이 틀릴 수 있습니다.");
      }
    }
  };

  return (
    <div className="extra-info-container">
      <form onSubmit={handleSubmit} className="extra-info-form">
        <h2 className="extra-info-title">반가워요, {storedUser?.realName || storedUser?.nickname}님!</h2>
        <p className="extra-info-description">{needNickname ? '닉네임과 휴대폰 번호를 입력해 주세요.' : '마지막으로 연락처만 확인하면 가입이 완료됩니다.'}</p>

        {needNickname && (
          <div className="input-group">
            <label className="input-label">닉네임</label>
            <input
              className="extra-info-input"
              type="text"
              placeholder="사용하실 닉네임을 입력하세요"
              value={formData.nickname}
              onChange={(e) => {
                setFormData({ ...formData, nickname: e.target.value });
                setNicknameError('');
              }}
              onBlur={() => checkNicknameAvailability(formData.nickname)}
              required
            />
            {nicknameError && (
              <p className="input-error-message">{nicknameError}</p>
            )}
          </div>
        )}

        {isTempEmail ? (
          <div className="input-group">
            <label className="input-label">실제 이메일 주소</label>
            <input className="extra-info-input" type="email" placeholder="example@email.com" required 
                    onChange={(e) => setFormData({...formData, email: e.target.value})} />
          </div>
        ) : (
          <div className="extra-info-readonly-badge">
            인증된 이메일: <strong>{storedUser?.email}</strong>
          </div>
        )}

        <div className="input-group">
          <label className="input-label">휴대폰 번호</label>
          <input 
            className="extra-info-input" 
            type="text" 
            placeholder="010-0000-0000" 
            value={formData.phoneNumber} // ✨ 상태값 연결
            onChange={handlePhoneChange} // ✨ 하이픈 생성기 연결
            maxLength={13} // ✨ 하이픈 포함 최대 13자 제한
            required 
          />
        </div>

        <button className="extra-info-button" type="submit">지금 바로 시작하기</button>
      </form>
    </div>
  );
};

export default ExtraInfo;