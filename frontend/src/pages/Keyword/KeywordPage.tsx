import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './KeywordPage.css';

// 🚀 백엔드 DTO와 일치하도록 인터페이스 유지
interface MovieData {
  id: number;
  title: string;
  posterPath: string;
  summary: string;
  keywords: string[];
}

const KeywordPage: React.FC = () => {
  const [movies, setMovies] = useState<MovieData[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('전체');
  const navigate = useNavigate();
  
  // 🚀 그리드 배치를 위해 페이지당 아이템을 6개로 조정
  const itemsPerPage = 6; 

  // 필터링할 장르 목록
  const genres = ['전체', '사극', '애니메이션', '로맨스', '액션', '스릴러', 'SF', '공포', '범죄'];

  useEffect(() => {
    const fetchAndCombineData = async () => {
      try {
        setIsLoading(true);

        // 🚀 기존 fetch 로직 유지
        const [mainRes, aiRes] = await Promise.all([
          fetch('http://localhost:8080/api/contents/all'),
          fetch('http://localhost:8080/api/ai/keywords')
        ]);

        const mainJson = await mainRes.json();
        const aiData = await aiRes.json();

        // 🚀 211개 데이터를 모두 가져오기 위해 리스트 참조 (데이터 구조 유지)
        const mainList = mainJson.data;

        const combined = mainList.map((movie: any) => {
          // 🚀 기존 content_id 매칭 로직 100% 보존
          const aiMatch = aiData.find((ai: any) => 
            Number(ai.content_id) === Number(movie.id)
          );
          
          return {
            id: movie.id,
            title: movie.title,
            posterPath: movie.posterPath,
            summary: aiMatch ? aiMatch.summary : "줄거리 정보를 불러올 수 없습니다.",
            keywords: aiMatch ? aiMatch.keywords : ["분석중"]
          };
        });

        setMovies(combined);
      } catch (error) {
        console.error("데이터 매칭 실패:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAndCombineData();
  }, []);

  // 🚀 핵심: 선택된 탭에 따라 영화 필터링 (기존 데이터 가공)
  const filteredMovies = movies.filter(movie => 
    activeTab === '전체' || movie.keywords.includes(activeTab)
  );

  // 필터링된 결과로 페이지네이션 계산
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredMovies.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredMovies.length / itemsPerPage);

  if (isLoading) return <div className="loading">DB 매칭 중...</div>;

  return (
    <div className="keyword-container">
      {/* 🚀 상단 장르 탭 바 추가 */}
      <div className="filter-tab-bar">
        {genres.map(genre => (
          <button 
            key={genre} 
            className={`tab-btn ${activeTab === genre ? 'active' : ''}`}
            onClick={() => { setActiveTab(genre); setCurrentPage(1); }}
          >
            {genre}
          </button>
        ))}
      </div>

      <div className="purple-frame">
        {currentItems.length > 0 ? (
          currentItems.map((movie) => {
            const displayKeywords = activeTab !== '전체'
              ? [activeTab, ...movie.keywords.filter(k => k !== activeTab)].slice(0, 5)
              : movie.keywords.slice(0, 5);

            return (
              <div 
                key={movie.id} 
                className="movie-card"
                onClick={() => navigate(`/movie/${movie.id}`)}
                style={{ cursor: 'pointer' }}
              >
                <div className="poster-area_keyword">
                  {movie.posterPath ? (
                    <img src={movie.posterPath} alt={movie.title} className="poster-image" />
                  ) : (
                    <div className="poster-fallback">이미지 없음</div>
                  )}
                </div>
                
                <div className="info-area">
                  <div className="summary-box">
                    <h3 className="movie-title_keyword">{movie.title}</h3>
                    {/* 🚀 CSS의 line-clamp 설정을 위해 substring 없이 원문 그대로 전달합니다. */}
                    <p className="summary-text">{movie.summary}</p>
                  </div>
                  
                  <div className="keyword-row">
                    {/* 🚀 movie.keywords 대신 지독하게 가공된 displayKeywords를 사용합니다! */}
                    {displayKeywords.map((tag: string, index: number) => (
                      <span key={index} className="keyword-tag"># {tag}</span>
                    ))}
                  </div>
                </div>
              </div>
            );
          })
        ) : (
          <div className="no-result">해당 장르의 분석 데이터가 아직 없습니다!</div>
        )}
      </div>

      <div className="pagination-bar">
        <button className="page-btn" onClick={() => setCurrentPage(p => Math.max(p - 1, 1))} disabled={currentPage === 1}>&lt;</button>
        <span className="page-info">{currentPage} / {totalPages || 1} page</span>
        <button className="page-btn" onClick={() => setCurrentPage(p => Math.min(p + 1, totalPages))} disabled={currentPage === totalPages || totalPages === 0}>&gt;</button>
      </div>
    </div>
  );
};

export default KeywordPage;