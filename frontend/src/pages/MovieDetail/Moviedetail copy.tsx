import React from 'react';
import './MovieDetail.css';

// 크롤링 데이터 구조 정의 (타입스크립트)
interface MovieDetailData {
  title: string;
  genre: string;
  synopsis: string;
  platforms: { name: string; url: string; logo: string }[];
  cast: { name: string; work: string; image: string }[];
  expertReviews: string[];
  aiRecommendations: string[];
  userReviews: string[];
}

const MovieDetail: React.FC = () => {
  


  // 임시 데이터 (나중에 크롤링 데이터로 교체)
  const movie: MovieDetailData = {
    title: "영화제목",
    genre: "장르",
    synopsis: "인간들과의 전쟁으로 첫째 아들을 잃은 후, '제이크'와 '네이티리'는 깊은 슬픔에 빠진다. 상심에 빠진 이들 앞에 '키리'가 이끄는 새로운 부족이 등장하면서 판도라는 더욱 큰 위협에 처하게 되는데...",
    platforms: [
      { name: "Netflix", url: "https://netflix.com", logo: "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg" },
      { name: "Watcha", url: "https://watcha.com", logo: "https://upload.wikimedia.org/wikipedia/commons/b/b8/Watcha_logo.png" }
    ],
    cast: Array(4).fill({ name: "출연진 이름", work: "출연진 작품의 이름", image: "" }),
    expertReviews: Array(4).fill("전문가의 평가"),
    aiRecommendations: [
      "AI 가 추천하는 이유 1번",
      "AI 가 추천하는 이유 2번",
      "AI 가 추천하는 이유 3번"
    ],
    userReviews: Array(6).fill("리뷰")
  };

  return (
    <div className="detail-container">
      <main className="main-content">
        {/* 2. 상단 배너 섹션 */}
        <section className="banner-section">
          <div className="info-overlay">
            {/* 현재 상영중인 플랫폼 (장르 위쪽) */}
            <div className="platform-row">
              {movie.platforms.map((p, i) => (
                <a key={i} href={p.url} target="_blank" rel="noreferrer" className="platform-link">
                  <img src={p.logo} alt={p.name} className="platform-icon" />
                </a>
              ))}
            </div>
            <span className="genre-label">{movie.genre}</span>
            <h1 className="movie-title">{movie.title}</h1>
            <div className="action-buttons">
              <button className="btn-trailer">트레일러 재생</button>
              <button className="btn-rating">평점</button>
            </div>
          </div>
        </section>

        {/* 3. 메인 그리드 레이아웃 */}
        <div className="content-grid">
          {/* 왼쪽 컬럼 */}
          <div className="left-column">
            <section className="detail-section">
              <h2 className="section-title">주요 출연진</h2>
              <div className="cast-grid">
                {movie.cast.map((person, i) => (
                  <div key={i} className="cast-card">
                    <div className="photo-placeholder" />
                    <p className="name">{person.name}</p>
                    <p className="work">{person.work}</p>
                  </div>
                ))}
              </div>
            </section>

            <section className="detail-section">
              <h2 className="section-title">줄거리</h2>
              <p className="synopsis-text">{movie.synopsis}</p>
            </section>

            <section className="detail-section">
              <h2 className="section-title">전문가의 평가</h2>
              <div className="expert-grid">
                {movie.expertReviews.map((review, i) => (
                  <div key={i} className="review-box expert-box">{review}</div>
                ))}
              </div>
            </section>
          </div>

          {/* 오른쪽 컬럼 */}
          <div className="right-column">
            <section className="detail-section">
              <h2 className="section-title">AI가 추천하는 PICK MOVIE</h2>
              <ul className="ai-pick-list">
                {movie.aiRecommendations.map((reason, i) => (
                  <li key={i}>{reason}</li>
                ))}
              </ul>
            </section>

            <section className="detail-section">
              <h2 className="section-title">리뷰 목록</h2>
              <div className="user-review-grid">
                {movie.userReviews.map((review, i) => (
                  <div key={i} className="review-box user-box">{review}</div>
                ))}
              </div>
            </section>
          </div>
        </div>
      </main>
    </div>
  );
};

export default MovieDetail;