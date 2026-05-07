import { useState, useEffect } from 'react';

export const useAuth = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(() => {
    const token = localStorage.getItem("token");
    return token && token !== "undefined" && token !== "null";
  });
  const [userNickname, setUserNickname] = useState(() => {
    const nickname = localStorage.getItem("nickname");
    const token = localStorage.getItem("token");
    return (token && token !== "undefined" && token !== "null") ? nickname : null;
  });

  useEffect(() => {
    const token = localStorage.getItem("token");
    const nickname = localStorage.getItem("nickname");

    console.log("useAuth - token:", token);
    console.log("useAuth - nickname:", nickname);
    console.log("useAuth - token exists:", !!token);
    console.log("useAuth - token not undefined:", token !== "undefined");
    console.log("useAuth - token not null:", token !== "null");

    // 단순 존재 여부뿐만 아니라 'undefined' 문자열이 저장되는 경우까지 방지
    if (token && token !== "undefined" && token !== "null") {
      setIsLoggedIn(true);
      setUserNickname(nickname);
      console.log("useAuth - set isLoggedIn to true");
    } else {
      setIsLoggedIn(false);
      setUserNickname(null);
      console.log("useAuth - set isLoggedIn to false");
    }
  }, []);

  return { isLoggedIn, userNickname };
};