import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './GenrePage.css';

interface ContentSummaryDto {
  id: number;
  title: string;
  posterPath: string;
  positiveRatio?: number;
}

const GenrePage: React.FC = () => {
  // 주소창에서 /genre/액션 -> '액션'을 꺼내옵니다.
  const { genreName } = useParams<{ genreName: string }>();
  const [movies, setMovies] = useState<ContentSummaryDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchMovies = async () => {
      if (!genreName) return;
      try {
        setLoading(true);
        // 방금 만든 백엔드 API 호출!
        const response = await axios.get(`http://localhost:8080/api/contents/genre/${genreName}`);
        
        // ApiResponse 구조 (data 안에 실제 배열이 있음)
        if (response.data && response.data.data) {
          setMovies(response.data.data);
        } else {
          setMovies(response.data); // 혹시 몰라 추가한 안전장치
        }
      } catch (error) {
        console.error("장르별 영화 로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
    window.scrollTo(0, 0); // 다른 장르 누르면 화면 맨 위로 이동
  }, [genreName]);

  if (loading) {
    return <div className="genre-loading">콘텐츠를 불러오는 중입니다...</div>;
  }

  return (
    <div className="genre-page-container">
      <div className="genre-header">
        <h2>{genreName} 영화</h2>
        <span className="genre-count">총 {movies.length}개의 작품</span>
      </div>

      {movies.length > 0 ? (
        <div className="genre-movie-grid">
          {movies.map((movie) => (
            <div 
              key={movie.id} 
              className="genre-movie-card"
              onClick={() => navigate(`/movie/${movie.id}`)} 
            >
              <img 
                src={movie.posterPath?.startsWith('http') ? movie.posterPath : `https://image.tmdb.org/t/p/w500${movie.posterPath}`} 
                alt={movie.title} 
                className="genre-movie-poster"
              />
              <div className="genre-movie-info">
                <p className="genre-movie-title">{movie.title}</p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="genre-empty">
          해당 장르의 영화가 아직 등록되지 않았습니다.
        </div>
      )}
    </div>
  );
};

export default GenrePage;