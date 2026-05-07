import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

export const useAuthCheck = () => {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    // oauth2 리다이렉트 경로가 아닐 때는 무시
    if (!location.pathname.includes('/oauth2/redirect')) {
      return; 
    }

    const params = new URLSearchParams(location.search);
    
    // ✨ 1. 정지 유저 체크 (가장 먼저 수행!)
    const error = params.get("error");
    if (error === "BANNED") {
      alert("운영 원칙 위반으로 해당 계정은 정지되었습니다. 관리자에게 문의하세요.");
      localStorage.removeItem("token"); // 만약 남아있을지 모를 토큰 제거
      navigate("/", { replace: true }); // 메인으로 튕겨내기
      return; // 로직 종료
    }

    const token = params.get("token") || params.get("accessToken") || params.get("jwt"); 
    const nickname = params.get("nickname");
    const needInfo = params.get("needInfo"); 

    // 2. 정상 로그인 처리
    if (token && token !== "undefined" && token !== "null") {
      localStorage.setItem("token", token);
      if (nickname) {
        localStorage.setItem("nickname", decodeURIComponent(nickname));
      }

      if (needInfo === "true") {
        console.log("추가 정보 입력 필요: /extra-info로 이동");
        navigate("/extra-info", { replace: true });
        return;
      }

      console.log("인증 완료: 메인으로 이동");
      navigate("/", { replace: true });
      window.location.reload();
    }
  }, [location, navigate]);
};