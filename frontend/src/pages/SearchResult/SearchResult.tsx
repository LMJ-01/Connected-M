import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import './SearchResult.css';

const SearchResult = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const query = searchParams.get('q') || "";
  
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedGenre, setSelectedGenre] = useState("모든 장르"); // 기본값을 "모든 장르"로 설정

  // 페이지네이션을 위한 상태
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;

  useEffect(() => {
    const fetchSearchResults = async () => {
      if (!query) return;
      setLoading(true);
      try {
        const response = await fetch(`${import.meta.env.VITE_API_URL}/api/contents/search?query=${encodeURIComponent(query)}`);
        const result = await response.json();
        console.log("🚀 백엔드에서 온 데이터:", result);
        if (result.data) {
          const mapped = result.data.map((m: any) => {
            const genreArray = Array.isArray(m.genres) ? m.genres : [];
            return {
              ...m,
              poster_path: m.poster_path || m.posterpath || m.posterPath || "",
              id: m.id,
              overview: m.overview || m.contents_overview || "줄거리 정보가 없습니다.",
              genreList: genreArray 
            };
          });
          setResults(mapped);
          setCurrentPage(1); // 검색어가 바뀌면 다시 1페이지로
        }
      } catch (error) {
        console.error("검색 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchSearchResults();
  }, [query]);

  // 1. 현재 결과물에 존재하는 장르만 뽑아내기 (동적 장르 목록)
  const availableGenres = Array.from(new Set(results.flatMap(movie => movie.genreList || [])));
  const displayGenres = ["모든 장르", ...availableGenres];

  // 2. 장르 필터링 적용
  const filteredResults = results.filter(movie => {
    if (selectedGenre === "모든 장르") return true; 
    return movie.genreList && movie.genreList.includes(selectedGenre);
  });

  // 3. 페이지네이션 계산 (필터링된 결과 기준)
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredResults.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredResults.length / itemsPerPage);

  return (
    <main className="search-result-container">
      <aside className="search-sidebar">
        <div className="sidebar-card">
          <h3 className="sidebar-title">검색 결과</h3>
          <ul className="category-list genre-list">
            {displayGenres.map((genre) => {
              const count = genre === "모든 장르" 
                ? results.length 
                : results.filter(movie => movie.genreList?.includes(genre)).length;

              return (
                <li 
                  key={genre}
                  className={selectedGenre === genre ? "active" : ""}
                  onClick={() => {
                    setSelectedGenre(genre);
                    setCurrentPage(1); // 장르 변경 시 1페이지로 이동
                  }}
                >
                  {genre} <span className="genre-count">{count}</span>
                </li>
              );
            })}
          </ul>
        </div>
      </aside>

      <section className="search-content-area">
        <div className="search-header-info">
          <h2 className="search-query-title">
            <span>"{query}"</span> 검색 결과 ({filteredResults.length}건)
          </h2>
        </div>

        {loading ? (
          <div className="loading-message">정보를 가져오는 중...</div>
        ) : currentItems.length > 0 ? (
          <>
            <div className="search-list-wrapper">
              {currentItems.map((movie) => (
                <div key={movie.id} className="search-result-card" onClick={() => navigate(`/movie/${movie.id}`)}>
                  <div className="search-poster-box">
                    <img 
                      src={movie.poster_path?.startsWith('http') 
                        ? movie.poster_path 
                        : `https://image.tmdb.org/t/p/w500${movie.poster_path}`} 
                      alt={movie.title} 
                    />
                  </div>
                  <div className="search-info-box">
                    <h3 className="search-movie-title">{movie.title}</h3>
                    <p className="search-movie-overview">{movie.overview}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* 페이지네이션 UI */}
            {totalPages > 1 && (
              <div className="pagination-wrapper">
                <button 
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage(prev => prev - 1)}
                >
                  &lt;
                </button>
                
                <span className="page-indicator">
                  <strong>{currentPage}</strong> / {totalPages}
                </span>

                <button 
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage(prev => prev + 1)}
                >
                  &gt;
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="no-result-message">
             <p>선택하신 <span>"{selectedGenre}"</span> 장르와 일치하는 결과가 없습니다.</p>
          </div>
        )}
      </section>
    </main>
  );
};

export default SearchResult;