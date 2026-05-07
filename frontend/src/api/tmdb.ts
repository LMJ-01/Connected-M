// src/api/tmdb.ts
const API_KEY = import.meta.env.VITE_TMDB_API_KEY;
const BASE_URL = import.meta.env.VITE_TMDB_BASE_URL;

export const fetchPopularMovies = async () => {
  // 주소가 올바르게 합쳐졌는지 확인하기 위해 console.log 추가
  const url = `${BASE_URL}/movie/popular?api_key=${API_KEY}&language=ko-KR&page=1`;
  console.log("요청 주소 확인:", url); 

  try {
    const response = await fetch(url);
    
    // 응답이 JSON이 아닌 HTML(<!doctype)인 경우 여기서 걸러짐
    if (!response.ok) {
      throw new Error(`API 호출 실패: ${response.status}`);
    }

    const data = await response.json();
    return data.results;
  } catch (error) {
    console.error("데이터 파싱 에러:", error);
    return [];
  }
};