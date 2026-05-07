import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './OttPage.css';

interface ContentSummaryDto {
  id: number;
  title: string;
  posterPath: string;
}

const OttPage: React.FC = () => {
  const { providerName } = useParams<{ providerName: string }>();
  const [movies, setMovies] = useState<ContentSummaryDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate();

  // ✨ OTT별 정보를 결정하는 사전 (명준님 요청사항 반영)
  const getProviderInfo = (name: string) => {
  switch (name?.toLowerCase()) {
    case 'netflix': return { title: 'NETFLIX', color: '#E50914', logo: 'https://image.tmdb.org/t/p/original/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg' };
    case 'disney': return { title: 'DISNEY+', color: '#00D6FE', logo: 'https://image.tmdb.org/t/p/original/97yvRBw1GzX7fXprcF80er19ot.jpg' };
    case 'tving': return { title: 'TVING', color: '#FF153C', logo: 'https://image.tmdb.org/t/p/original/qHThQdkJuROK0k5QTCrknaNukWe.jpg' };
    case 'wavve': return { title: 'WAVVE', color: '#1B1464', logo: 'https://image.tmdb.org/t/p/original/hPcjSaWfMwEqXaCMu7Fkb529Dkc.jpg' };
    
    case 'watcha': return { 
  title: 'WATCHA', 
  color: '#FF0558', 
  // 🚀 [리얼 최종] 오른쪽 끝이 더 높이 솟아오르는 왓챠 특유의 기울기를 완벽 재현한 로고입니다.
  logo: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCI+PHJlY3Qgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiByeD0iMTIiIGZpbGw9IiMwMDAiLz48cGF0aCBmaWxsPSIjRkYwNTU4IiBkPSJNMTAgMTYuNUgxNi41TDE5LjUgMzBMMjMuNSAxNC41SDI5TDMyLjUgMzBMNDAgNkg0NkwzNyAzNkgyOUwyNiAyMkwyMiAzNkgxM1oiLz48L3N2Zz4='
};
    default: return { title: 'OTT', color: '#ffffff', logo: '' };
  }
};

  const info = getProviderInfo(providerName || '');

  useEffect(() => {
    const fetchMovies = async () => {
      if (!providerName) return;
      try {
        setLoading(true);
        const response = await axios.get(`http://localhost:8080/api/contents/ott/${providerName}`);
        if (response.data && response.data.data) {
          setMovies(response.data.data);
        } else {
          setMovies(response.data); 
        }
      } catch (error) {
        console.error("OTT 로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchMovies();
    window.scrollTo(0, 0); 
  }, [providerName]);

  if (loading) return <div className="ott-loading">콘텐츠를 불러오는 중입니다...</div>;

  return (
    <div className="ott-page-container">
      {/* 왼쪽 포인트 바 컬러 적용 */}
      <div className="ott-header" style={{ borderLeft: `5px solid ${info.color}` }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '15px', margin: 0 }}>
          {/* 로고 이미지가 있을 때만 출력 (alt를 비워 중복 텍스트 방지) */}
          {info.logo && (
            <img 
              src={info.logo} 
              alt="" 
              className="ott-title-logo" 
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
            />
          )}
          {/* 서비스명(WATCHA, NETFLIX 등) 출력 */}
          <span style={{ color: '#000000', fontSize: '28px', fontWeight: 'bold' }}>
            {info.title}
          </span>
        </h2>
        <span className="ott-count">총 {movies.length}개의 작품</span>
      </div>

      {movies.length > 0 ? (
        <div className="ott-movie-grid">
          {movies.map((movie) => (
            <div 
              key={movie.id} 
              className="ott-movie-card"
              onClick={() => navigate(`/movie/${movie.id}`)} 
            >
              <img 
                src={movie.posterPath?.startsWith('http') ? movie.posterPath : `https://image.tmdb.org/t/p/w500${movie.posterPath}`} 
                alt={movie.title} 
                className="ott-movie-poster"
              />
              <div className="ott-movie-info">
                <p className="ott-movie-title">{movie.title}</p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="ott-empty">해당 플랫폼에 등록된 영화가 없습니다.</div>
      )}
    </div>
  );
};

export default OttPage;