import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom'; // ✨ useNavigate 추가

const OAuth2RedirectHandler = () => {
    const hasProcessed = useRef(false);
    const navigate = useNavigate(); // ✨ 훅 생성
    
    useEffect(() => {
        if (hasProcessed.current) return;

        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        const id = params.get('id');
        const nickname = params.get('nickname');
        const realName = params.get('realName');
        const email = params.get('email');
        const needInfo = params.get('needInfo') === 'true'; 

        if (token) {
            hasProcessed.current = true;

            localStorage.setItem('token', token);
            localStorage.setItem('nickname', nickname || "사용자");

            const userObject = {
                id: id,
                nickname: nickname,
                realName: realName,
                email: email
            };
            localStorage.setItem('user', JSON.stringify(userObject));

            const needNickname = params.get('needNickname') === 'true';
            if (needNickname) {
                alert("닉네임 중복이 확인되어 직접 닉네임 입력이 필요합니다.");
                navigate("/extra-info?needNickname=true", { replace: true });
            } else if (needInfo) {
                alert("정상적인 서비스 이용을 위해 전화번호 등록이 필요합니다.");
                // 🚨 href 대신 navigate를 쓰면 리액트 내부에서 안전하게 이동합니다.
                navigate("/extra-info", { replace: true }); 
            } else {
                navigate("/", { replace: true });
            }
            // 🚨 새로고침은 이동 후에 한 번만!
            window.location.reload(); 
        } else {
            // 에러 파라미터가 있으면 알림 띄워주기
            if (params.get('error')) {
                alert("로그인 중 오류가 발생했습니다.");
            }
            navigate("/", { replace: true });
        }
    }, [navigate]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f3f4f6' }}>
            <p style={{ fontWeight: 'bold', color: '#4b0082' }}>로그인 인증 중...</p>
        </div>
    );
};

export default OAuth2RedirectHandler;