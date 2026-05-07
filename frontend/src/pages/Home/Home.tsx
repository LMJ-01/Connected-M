import React, { useState, useEffect, useRef } from 'react';
import './Home.css';
import { useNavigate } from 'react-router-dom';
import { IoIosTimer } from "react-icons/io";

const Home = () => {
  const navigate = useNavigate();
  
  const [activeGenre, setActiveGenre] = useState<number>(1); 
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchHistory, setSearchHistory] = useState<string[]>([]);
  const searchContainerRef = useRef<HTMLDivElement>(null);

  const [currentMovies, setCurrentMovies] = useState<any[]>([]);
  const [slideItems, setSlideItems] = useState<any[]>([]);
  const [currentSlide, setCurrentSlide] = useState(0); 
  const [isSliderPaused, setIsSliderPaused] = useState(false);
  const [touchStart, setTouchStart] = useState<number | null>(null);
  const [touchEnd, setTouchEnd] = useState<number | null>(null);
  const minSwipeDistance = 50;

  useEffect(() => {
    const saved = localStorage.getItem('searchHistory');
    if (saved) setSearchHistory(JSON.parse(saved));

    const handleClickOutside = (e: MouseEvent) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(e.target as Node)) {
        setIsSearchOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSearch = () => {
    if (!searchQuery.trim()) return;
    const updatedHistory = [searchQuery, ...searchHistory.filter(h => h !== searchQuery)].slice(0,5) ;
    setSearchHistory(updatedHistory);
    localStorage.setItem('searchHistory', JSON.stringify(updatedHistory));
    setIsSearchOpen(false);
    navigate(`/search?q=${searchQuery}`);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  useEffect(() => {
    const loadHeroMovies = async () => {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_URL}/api/contents/random`);
        const result = await response.json();
        if (result.data && result.data.length > 0) {
          const mapped = result.data.map((m: any) => ({
            ...m,
            poster_path: m.posterPath.replace('https://image.tmdb.org/t/p/w500', ''),
            id: m.id
          }));
          setSlideItems(mapped.slice(0, 5));
        }
      } catch (error) {
        console.error("추천작 로드 실패:", error);
      }
    };
    loadHeroMovies();
  }, []);

  useEffect(() => {
    const loadGenreMovies = async () => {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_URL}/api/contents/category?genreId=${activeGenre}&limit=10`);
        const result = await response.json();
        if (result.data) {
          const mapped = result.data.map((m: any) => ({
            ...m,
            poster_path: m.posterPath.replace('https://image.tmdb.org/t/p/w500', ''),
            id: m.id
          }));
          setCurrentMovies(mapped);
        }
      } catch (error) {
        console.error("장르 영화 로드 실패:", error);
      }
    };
    loadGenreMovies();
  }, [activeGenre]);

  useEffect(() => {
    if (isSliderPaused || slideItems.length === 0) return;
    const interval = setInterval(() => {
      setCurrentSlide(prev => (prev + 1) % slideItems.length);
    }, 3200);
    return () => clearInterval(interval);
  }, [isSliderPaused, slideItems.length]);

  const moveSlider = (direction: number) => {
    if (slideItems.length === 0) return;
    setCurrentSlide((prev) => (prev + direction + slideItems.length) % slideItems.length);
  };

  const getSlideStyle = (index: number) => {
    if (slideItems.length === 0) return { display: 'none' };
    const diff = index - currentSlide;
    let offset = diff;
    if (diff > 2) offset = diff - slideItems.length;
    if (diff < -2) offset = diff + slideItems.length;
    const absOffset = Math.abs(offset);
    const isActive = offset === 0;

    return {
      transform: `translateX(${offset * 220}px) scale(${1 - absOffset * 0.2})`,
      zIndex: 10 - absOffset,
      opacity: isActive ? 1 : absOffset === 1 ? 0.6 : 0,
      transition: 'all 0.5s ease-in-out',
      position: 'absolute' as const,
      visibility: absOffset > 1 ? 'hidden' as const : 'visible' as const,
    };
  };

  const onTouchStart = (e: any) => { const clientX = e.touches ? e.touches[0].clientX : e.clientX; setTouchStart(clientX); };
  const onTouchMove = (e: any) => { const clientX = e.touches ? e.touches[0].clientX : e.clientX; setTouchEnd(clientX); };
  const onTouchEnd = () => { if (touchStart === null || touchEnd === null) return; const distance = touchStart - touchEnd; if (distance > minSwipeDistance) moveSlider(1); else if (distance < -minSwipeDistance) moveSlider(-1); setTouchStart(null); setTouchEnd(null); };

  const removeHistory = (e: React.MouseEvent, textToRemove: string) => {
  e.stopPropagation(); // 중요: 부모의 클릭(검색 이동) 이벤트가 실행되지 않게 막음
  const updatedHistory = searchHistory.filter(item => item !== textToRemove);
  setSearchHistory(updatedHistory);
  localStorage.setItem('searchHistory', JSON.stringify(updatedHistory));
  };
  return (
    <main className="home-container">
      <section className="hero-section">
        <h1 className="hero-title">환영합니다. 인생작품을 찾아드립니다</h1>
        <div className="hero-search-wrapper" ref={searchContainerRef}>
          <div className={`google-search-container ${isSearchOpen && searchHistory.length > 0 ? 'active' : ''}`}>
            <div className="search-input-row">
              <span className="search-icon">🔍</span>
              <input 
                type="text" 
                className="hero-search" 
                placeholder="어떤 작품을 찾고 계신가요?" 
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onFocus={() => setIsSearchOpen(true)}
                onKeyPress={handleKeyPress}
              />
              <button className="hero-search-btn" onClick={handleSearch}>검색</button>
            </div>
            {isSearchOpen && searchHistory.length > 0 && (
              <ul className="search-list">
                {searchHistory.map((item, index) => (
                  <li key={index} className="search-item" onClick={() => {
                    setSearchQuery(item);
                    setIsSearchOpen(false);
                    navigate(`/search?q=${item}`);
                  }}>
                    <IoIosTimer className="history-icon" />
                    <span className="search-text">{item}</span>
                    <button 
          className="delete-history-btn" 
          onClick={(e) => removeHistory(e, item)}
        >
          ✕
        </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </section>

      <section className="slider-section">
        <div className="slider-header-wrapper">
          <h2 className="section-title">오늘의 추천작</h2>
          <div className="slider-3d-wrapper" onMouseEnter={() => setIsSliderPaused(true)} onMouseLeave={() => setIsSliderPaused(false)} onMouseDown={onTouchStart} onMouseMove={(e) => touchStart && onTouchMove(e)} onMouseUp={onTouchEnd}>
            <button className="nav-btn-3d left" onClick={() => moveSlider(-1)}>◀</button>
            <div className="slider-3d-container">
              {slideItems.length > 0 ? slideItems.map((item, index) => (
                <div key={item.id} className="slide-card-3d" style={getSlideStyle(index)} onClick={() => index === currentSlide ? navigate(`/movie/${item.id}`) : setCurrentSlide(index)}>
                  <img src={`https://image.tmdb.org/t/p/w500${item.poster_path}`} alt={item.title} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '12px' }} />
                </div>
              )) : <div className="loading">추천 영화를 불러오는 중...</div>}
            </div>
            <button className="nav-btn-3d right" onClick={() => moveSlider(1)}>▶</button>
          </div>
        </div>
      </section>

      <section className="content-section">
        <div className="category-header">
          <h2 className="section-title">카테고리별 영화</h2>
          <div className="genre-tabs">
            {[
              { id: 1, label: '액션' },
              { id: 2, label: '코미디' },
              { id: 3, label: '범죄' },
              { id: 5, label: '드라마' },
              { id: 7, label: '애니' }
            ].map(genre => (
              <button 
                key={genre.id}
                onClick={() => setActiveGenre(genre.id)}
                className={`genre-btn ${activeGenre === genre.id ? 'active' : ''}`}
                style={{
                  padding: '8px 20px', borderRadius: '20px', cursor: 'pointer', fontWeight: 'bold',
                  background: activeGenre === genre.id ? '#4b0082' : 'white',
                  color: activeGenre === genre.id ? 'white' : '#6b7280',
                  border: '1px solid #d1d5db', marginRight: '8px'
                }}
              >
                {genre.label}
              </button>
            ))}
          </div>
        </div>
        <div className="movie-grid">
          {currentMovies.length > 0 ? currentMovies.map((movie) => (
            <div key={movie.id} className="movie-item" onClick={() => navigate(`/movie/${movie.id}`)}>
              <div className="movie-poster">
                <img src={`https://image.tmdb.org/t/p/w500${movie.poster_path}`} alt={movie.title} style={{width: '100%', height: '100%', objectFit: 'cover', borderRadius: '8px'}} />
              </div>
              <p className="movie-title_main">{movie.title}</p>
              <p className="movie-info">상세 보기</p>
            </div>
          )) : <div className="loading">영화를 불러오는 중입니다...</div>}
        </div>
      </section>
    </main>
  );
};

export default Home;