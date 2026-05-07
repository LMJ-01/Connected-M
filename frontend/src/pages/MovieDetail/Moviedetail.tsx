import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { FaStar, FaRegStar, FaStarHalfAlt, FaBookmark, FaRegBookmark, FaChevronLeft, FaChevronRight, FaEdit, FaTrashAlt, FaCheck, FaTimes } from 'react-icons/fa';
import './MovieDetail.css';
import { useAuth } from '../../hooks/useAuth';
import axios from 'axios';
import { PiSirenFill } from "react-icons/pi";

interface ExpertReviewData {
  id: number;
  criticName: string;
  rating: string;
  comment: string;
  source: string;
}

interface UserReviewData {
  id: number;
  nickname: string;
  rating: string;
  comment: string;
}

interface CastMember {
  name: string;
  character: string;
  profile_path: string;
}

interface WatchProvider {
  provider_id: number;
  provider_name: string;
  logo_path: string;
}

interface MovieDetailData {
  id: number;
  title: string;
  overview: string;
  posterPath: string;
  backdropPath: string;
  genres: string[];
  castList: CastMember[];
  runtime: number;
  ageRating: string;
  topKeywords: string[];
  expertReviews: ExpertReviewData[];
  userReviews: UserReviewData[];
  providers: WatchProvider[]; 
  ottLogos?: string;
  userRatingAvg: number;
  expertRatingAvg: number;
  expertReviewCount: number
  trailerKey?: string;
  recommendations?: {
    id: number;
    title: string;
    posterPath: string;
    positiveRatio: number;
  }[];
}

const MovieDetail: React.FC = () => {
  const maskName = (name: string) => {
    if (!name) return "";
    if (name.length <= 1) return name;
    if (name.length === 2) return name[0] + "*";
    return name[0] + "*".repeat(name.length - 2) + name.slice(-1);
  };
  
  const { id } = useParams<{ id: string }>();
  const [movie, setMovie] = useState<MovieDetailData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  
  const { isLoggedIn, userNickname } = useAuth();
  const [hasReviewed, setHasReviewed] = useState<boolean>(false);
  const [isWishlisted, setIsWishlisted] = useState<boolean>(false);

  // 신고 관련 상태
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [selectedReviewId, setSelectedReviewId] = useState<number | null>(null);
  const [reportReason, setReportReason] = useState<string>('');
  const reportOptions = [
    { value: 'SPAM', label: '스팸/홍보' },
    { value: 'INAPPROPRIATE_CONTENT', label: '부적절한 내용' },
    { value: 'ABUSIVE_LANGUAGE', label: '욕설 비하발언' },
    { value: 'SPOILER', label: '스포일러 포함' },
    { value: 'OTHER', label: '기타 사유' },
  ];

  const [selectedReviewData, setSelectedReviewData] = useState<{
    nickname: string;
    comment: string;
  } | null>(null);

  const handleOpenReport = (reviewId: number, targetNickname: string, targetComment: string) => {
    setSelectedReviewId(reviewId);
    setSelectedReviewData({ nickname: targetNickname, comment: targetComment }); 
    setIsReportModalOpen(true);
  };

  const handleReportSubmit = async () => {
    if (!reportReason) { alert("사유를 선택해주세요."); return; }

    const reportData = {
      reviewId: selectedReviewId,
      targetNickname: selectedReviewData?.nickname, 
      reason: reportReason,                         
      reviewText: selectedReviewData?.comment,      
      reporterNickname: userNickname                
    };

    try {
      const token = localStorage.getItem('token');
      await axios.post('http://localhost:8080/api/reports', reportData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert("신고가 정상적으로 접수되었습니다.");
      setIsReportModalOpen(false);
      setReportReason('');
    } catch (error) {
      alert("신고 접수 중 오류가 발생했습니다.");
    }
  };

  const [hasLoggedRecentView, setHasLoggedRecentView] = useState<boolean>(false);

  const [newComment, setNewComment] = useState("");
  const [newRating, setNewRating] = useState(0); 
  const [hoverRating, setHoverRating] = useState<number | null>(null);

  const [isEditingDetail, setIsEditingDetail] = useState(false);
  const [editDetailComment, setEditDetailComment] = useState("");
  const [editDetailRating, setEditDetailRating] = useState(0);
  const [detailHoverRating, setDetailHoverRating] = useState<number | null>(null);

  const [expertPage, setExpertPage] = useState(1);
  const [userPage, setUserPage] = useState(1);
  const itemsPerPage = 3;

  const [expandedReviews, setExpandedReviews] = useState<number[]>([]);

  const toggleReview = (reviewId: number) => {
    setExpandedReviews(prev => 
      prev.includes(reviewId) 
        ? prev.filter(id => id !== reviewId) 
        : [...prev, reviewId]
    );
  };

  const getOttLink = (logoPath: string, movieTitle: string) => {
    if (!logoPath) return `https://www.google.com/search?q=${encodeURIComponent(movieTitle)}+시청하기`;
    const path = logoPath.trim();
    if (path.includes("pbpMk2JmcoNnQwx5JGpXngfoWtp")) return "https://www.netflix.com";
    if (path.includes("dpR8r13zWDeUR0QkzWidrdMxa56")) return "https://www.netflix.com";
    if (path.includes("97yvRBw1GzX7fXprcF80er19ot")) return "https://www.disneyplus.com";
    if (path.includes("5gmEivxOGPdqQ0A09uXp9Gf1vSj")) return "https://www.coupangplay.com";
    if (path.includes("hPcjSaWfMwEqXaCMu7Fkb529Dkc")) return "https://www.wavve.com";
    if (path.includes("5gmEivxOGPdq4Afpq1f8ktLtEW1")) return "https://watcha.com";
    if (path.includes("qHThQdkJuROK0k5QTCrknaNukWe")) return "https://www.tving.com";
    return `https://www.google.com/search?q=${encodeURIComponent(movieTitle)}+시청하기`;
  };

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    const fetchMovieData = async () => {
      if (!id) return;
      try {
        setLoading(true);
        const token = localStorage.getItem('token'); 

        const response = await fetch(`http://localhost:8080/api/contents/${id}`);
        const result = await response.json();

        if (result && result.data) {
          const data = result.data;
          setMovie({
            ...data,
            posterPath: data.posterPath ? data.posterPath.replace('https://image.tmdb.org/t/p/w500', '') : "",
            backdropPath: data.backdropPath ? data.backdropPath.replace('https://image.tmdb.org/t/p/original', '') : "",
          });

          if (isLoggedIn && userNickname) {
            setHasReviewed(data.userReviews?.some((r: any) => r.nickname === userNickname) ?? false);
            
            if (token) {
              const wishRes = await axios.get('http://localhost:8080/api/members/wishlist', {
                headers: { Authorization: `Bearer ${token}` }
              });
              const isWished = wishRes.data?.some((w: any) => w.contentId === Number(id)) ?? false;
              setIsWishlisted(isWished);
            }
          }

          if (isLoggedIn && token && !hasLoggedRecentView) {
            setHasLoggedRecentView(true);
            axios.post(`http://localhost:8080/api/users/recent/${id}`, {}, {
              headers: { Authorization: `Bearer ${token}` }
            })
            .then(() => console.log("최근 본 목록 저장 성공"))
            .catch(err => {
              if (err.response?.status === 401) {
                console.error("인증 실패");
              }
              setHasLoggedRecentView(false);
            });
          }
        }
      } catch (error) {
        console.error("데이터 로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchMovieData();
  }, [id, isLoggedIn, userNickname, hasLoggedRecentView]); 

  const handleWishlistToggle = async () => { 
    if (!isLoggedIn) { alert("로그인이 필요합니다."); return; }
    try {
      const token = localStorage.getItem('token');
      await axios.post(`http://localhost:8080/api/members/wishlist/${id}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setIsWishlisted(!isWishlisted);
    } catch (error) { alert("찜하기 처리 중 오류가 발생했습니다."); }
  };

  const handleReviewSubmit = async () => {
    if (!isLoggedIn) { alert("로그인이 필요합니다."); return; }
    if (hasReviewed) { alert("이미 리뷰를 작성하셨습니다."); return; }
    if (!newComment.trim()) { alert("내용을 입력해주세요."); return; }

    try {
      const token = localStorage.getItem('token'); 
      const response = await axios.post(`http://localhost:8080/api/contents/user-reviews?contentId=${id}`, 
        { rating: newRating.toString(), comment: newComment },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.status === 200 || response.status === 201) {
        alert("리뷰가 등록되었습니다!");
        window.location.reload();
      }
    } catch (error) { alert("리뷰 등록 실패!"); }
  };

  const handleDetailUpdate = async (reviewId: number) => {
    if (!editDetailComment.trim()) { alert("내용을 입력해주세요."); return; }
    try {
      const token = localStorage.getItem('token');
      await axios.put(`http://localhost:8080/api/contents/user-reviews/${reviewId}`, 
        { rating: editDetailRating.toString(), comment: editDetailComment },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("리뷰가 수정되었습니다!");
      setIsEditingDetail(false);
      window.location.reload(); 
    } catch (error) { alert("수정 실패!"); }
  };

  const handleDetailDelete = async (reviewId: number) => {
    if (!window.confirm("리뷰를 삭제하시겠습니까?")) return;
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`http://localhost:8080/api/contents/user-reviews/${reviewId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert("리뷰가 삭제되었습니다.");
      window.location.reload();
    } catch (error) { alert("삭제 실패!"); }
  };

  const renderStars = (rating: string | number) => {
    const num = Number(rating) / 2;
    return (
      <div className="star-wrapper compact">
        {[...Array(5)].map((_, i) => {
          const isFull = num >= i + 1;
          const isHalf = num >= i + 0.5 && num < i + 1;
          const starClass = (isFull || isHalf) ? "star-icon filled" : "star-icon";
          return isFull ? <FaStar key={i} className={starClass} /> : isHalf ? <FaStarHalfAlt key={i} className={starClass} /> : <FaRegStar key={i} className={starClass} />;
        })}
        <span className="rating-num">{rating}점</span>
      </div>
    );
  };

  if (loading || !movie) return <div className="loading-container">로딩 중...</div>;

  const safeGenres = movie.genres || [];
  const safeCastList = movie.castList || [];
  const safeProviders = movie.providers || [];
  const safeExpertReviews = movie.expertReviews || [];
  const safeUserReviews = movie.userReviews || [];
  const safeKeywords = movie.topKeywords || [];

  const myReview = safeUserReviews.find(r => r.nickname === userNickname);
  const otherUserReviews = safeUserReviews.filter(r => r.nickname !== userNickname);

  const runtime = movie.runtime || 0;
  const hours = Math.floor(runtime / 60);
  const minutes = runtime % 60;
  const runtimeText = hours > 0 ? `${hours}시간 ${minutes}분` : `${minutes}분`;

  const expertReviewsSlice = safeExpertReviews.slice((expertPage - 1) * itemsPerPage, expertPage * itemsPerPage);
  const userReviewsSlice = otherUserReviews.slice((userPage - 1) * itemsPerPage, userPage * itemsPerPage); 
  const totalExpertPages = Math.ceil(safeExpertReviews.length / itemsPerPage);
  const totalUserPages = Math.ceil(otherUserReviews.length / itemsPerPage);

  return (
    <div className="detail-container">
      <main className="main-content">
        <section className="banner-section">
          <div className="banner-background-wrapper">
            <div className="banner-bg-image" style={{ backgroundImage: `url(https://image.tmdb.org/t/p/original${movie.backdropPath})` }}></div>
            <div className="banner-overlay"></div>
          </div>
          <div className="banner-content">
            <div className="poster-area">
              <img src={`https://image.tmdb.org/t/p/w500${movie.posterPath}`} alt={movie.title} className="main-poster" />
              <div className="detail-keyword-row">
                {safeKeywords.map((tag, index) => (
                  <span key={index} className="detail-tag"># {tag}</span>
                ))}
              </div>
            </div>
            <div className="info-area">
              <div className="meta-info-row">
                <span className={`age-badge ${
                  movie.ageRating === '19' ? 'age-19' : 
                  movie.ageRating === '15' ? 'age-15' : 
                  movie.ageRating === '12' ? 'age-12' : 'age-all'
                }`}>
                  {movie.ageRating}
                </span>
                <span className="runtime-label">{runtimeText}</span>
              </div>
              <h1 className="movie-title">{movie.title}</h1>
              <div className="movie-genres-row">
                {safeGenres.join(' · ')}
              </div>
              <div className="platform-buttons-container">
                <span className="platform-label">시청 가능한 플랫폼</span>
                <div className="platform-logos-row">
                  {safeProviders.length > 0 ? (
                    safeProviders.map((p) => (
                      <a key={p.provider_id} href={getOttLink(p.logo_path, movie.title)} target="_blank" rel="noopener noreferrer">
                        <img src={`https://image.tmdb.org/t/p/original${p.logo_path}`} alt={p.provider_name} className="ott-logo-simple" title={p.provider_name} />
                      </a>
                    ))
                  ) : movie.ottLogos ? (
                    movie.ottLogos.split(',').map((logo, idx) => {
                      const cleanLogo = logo.trim();
                      const fullUrl = cleanLogo.startsWith('http') ? cleanLogo : `https://image.tmdb.org/t/p/original${cleanLogo}`;
                      return (
                        <a key={idx} href={getOttLink(cleanLogo, movie.title)} target="_blank" rel="noopener noreferrer">
                           <img src={fullUrl} alt="OTT" className="ott-logo-simple" />
                        </a>
                      );
                    })
                  ) : <span className="no-provider">현재 제공 중인 OTT가 없습니다.</span>}
                </div>
                <button className={`wishlist-btn ${isWishlisted ? 'active' : ''}`} onClick={handleWishlistToggle}>
                  {isWishlisted ? <FaBookmark /> : <FaRegBookmark />}
                  <span>{isWishlisted ? '찜한 영화' : '찜하기'}</span>
                </button>
              </div>
            </div>
          </div>
        </section>

        <div className="content-grid">
          <div className="left-column">
            <section className="detail-section">
              <h2 className="section-title">주요 출연진</h2>
              <div className="cast-grid">
                {safeCastList.map((p, i) => (
                  <div key={i} className="cast-card">
                    <img src={p.profile_path ? `https://image.tmdb.org/t/p/w200${p.profile_path}` : ''} alt="" className="cast-photo" />
                    <p className="name">{p.name}</p>
                    <p className="character">{p.character}</p>
                  </div>
                ))}
              </div>
            </section>
            <section className="detail-section">
              <h2 className="section-title">줄거리</h2>
              <p className="synopsis-text">{movie.overview}</p>
            </section>
            
            {movie.trailerKey && (
              <section className="detail-section">
                <h2 className="section-title">메인 예고편</h2>
                <div className="video-container">
                  <iframe
                    width="100%"
                    height="450"
                    src={`https://www.youtube.com/embed/${movie.trailerKey}`}
                    title="YouTube video player"
                    frameBorder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                  ></iframe>
                </div>
              </section>
            )}

            {movie.recommendations && movie.recommendations.length > 0 && (
              <section className="detail-section compact-recommendation-section">
                <h2 className="section-title">
                  이 작품과 결이 비슷한 영화
                  <span className="ai-highlight-tag">AI 매칭</span>
                </h2>
                <div className="rec-three-grid">
                  {movie.recommendations.slice(0, 3).map((rec) => (
                    <div key={rec.id} className="rec-card-compact" onClick={() => window.location.href = `/movie/${rec.id}`}>
                      <div className="rec-poster-wrapper-compact">
                        <img src={`https://image.tmdb.org/t/p/w300${rec.posterPath}`} alt={rec.title} />
                      </div>
                      <p className="rec-title-compact">{rec.title}</p>
                    </div>
                  ))}
                </div>
              </section>
            )}

          </div>

          <div className="right-column">
            <section className="detail-section">
              <h2 className="section-title" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                전문가 평점 {movie.expertReviewCount || 0}건
                <span className="avg-score-badge">★ {movie.expertRatingAvg?.toFixed(1) || "0.0"}</span>
              </h2>
              <div className="compact-card-container">
                {expertReviewsSlice.length > 0 ? expertReviewsSlice.map((r) => (
                  <div key={r.id} className="compact-review-row">
                    <div className="compact-reviewer-meta">
                      <span className="compact-reviewer-name">{maskName(r.criticName)} <small>| {r.source}</small></span>
                      {renderStars(r.rating)}
                    </div>
                    <p className="compact-comment-text">"{r.comment}"</p>
                  </div>
                )) : <p className="placeholder-text">전문가 리뷰가 없습니다.</p>}
                
                {totalExpertPages > 1 && (
                  <div className="pagination-controls">
                    <button disabled={expertPage === 1} onClick={() => setExpertPage(p => p - 1)}><FaChevronLeft /></button>
                    <span>{expertPage} / {totalExpertPages}</span>
                    <button disabled={expertPage === totalExpertPages} onClick={() => setExpertPage(p => p + 1)}><FaChevronRight /></button>
                  </div>
                )}
              </div>
            </section>

            <section className="detail-section">
              <h2 className="section-title" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                관람평 {otherUserReviews.length}건 
                <span className="avg-score-badge user">★ {movie.userRatingAvg?.toFixed(1) || "0.0"}</span>
              </h2>
              <div className="compact-card-container">
                {userReviewsSlice.length > 0 ? userReviewsSlice.map((r) => {
                  const isExpanded = expandedReviews.includes(r.id);
                  const isLongText = (r.comment || "").length > 80;
                  // 🌟 이 부분을 추가하세요!
    // 로컬 스토리지에 저장된 유저 정보를 가져와서 관리자인지 확인합니다.
    const userData = localStorage.getItem('user');
    const user = userData ? JSON.parse(userData) : null;
    const isAdmin = user?.role === 'ROLE_ADMIN'; // 여기서 isAdmin이 정의됩니다!
                  return (
                    <div key={r.id} className="compact-review-row">
                      <div className="compact-reviewer-meta">
                        <span className="compact-reviewer-name">{r.nickname}</span>
                        {renderStars(r.rating)}
                        
                        {/* 🔥 수정 포인트: 관리자가 아닐 때만 신고 아이콘을 보여줌 */}
        {!isAdmin && (
          <PiSirenFill 
            className="report-siren-icon" 
            title="신고하기" 
            onClick={() => handleOpenReport(r.id, r.nickname, r.comment)}
          />
        )}
                      </div>
                      <p className={`compact-comment-text ${isExpanded ? 'expanded' : 'clamped'}`}>
                        {r.comment}
                      </p>
                      {isLongText && (
                        <button className="btn-more-detail" onClick={() => toggleReview(r.id)}>
                          {isExpanded ? '접기' : '자세히 보기'}
                        </button>
                      )}
                    </div>
                  );
                }) : <p className="placeholder-text">다른 관람객의 리뷰가 아직 없습니다.</p>}

                {totalUserPages > 1 && (
                  <div className="pagination-controls">
                    <button disabled={userPage === 1} onClick={() => setUserPage(p => p - 1)}><FaChevronLeft /></button>
                    <span>{userPage} / {totalUserPages}</span>
                    <button disabled={userPage === totalUserPages} onClick={() => setUserPage(p => p + 1)}><FaChevronRight /></button>
                  </div>
                )}
              </div>
            </section>

            <section className="review-write-section">
              <h2 className="section-title">리뷰 작성</h2>
              {!isLoggedIn ? (
                <div className="already-reviewed-msg">로그인이 필요합니다.</div>
              ) : hasReviewed && myReview ? (
                <div className="my-detail-review-card">
                  <div className="card-header-row">
                    <span className="my-label">내가 남긴 리뷰 </span>
                    <div className="my-review-btns">
                      {isEditingDetail ? (
                        <>
                          <button className="save-btn" onClick={() => handleDetailUpdate(myReview.id)}><FaCheck /> 저장</button>
                          <button className="cancel-btn" onClick={() => setIsEditingDetail(false)}><FaTimes /> 취소</button>
                        </>
                      ) : (
                        <>
                          <button className="edit-btn" onClick={() => {
                            setIsEditingDetail(true);
                            setEditDetailComment(myReview.comment);
                            setEditDetailRating(Number(myReview.rating));
                          }}><FaEdit /> 수정</button>
                          <button className="delete-btn" onClick={() => handleDetailDelete(myReview.id)}><FaTrashAlt /> 삭제</button>
                        </>
                      )}
                    </div>
                  </div>

                  {isEditingDetail ? (
                    <div className="write-form-container" style={{padding: 0, marginTop: '10px'}}>
                      <div className="interactive-rating-stars-wrapper" onMouseLeave={() => setDetailHoverRating(null)}>
                        <div 
                          className="rating-zero-zone"
                          style={{ width: '20px', minHeight: '30px', cursor: 'pointer' }}
                          onMouseEnter={() => setDetailHoverRating(0)}
                          onClick={() => setEditDetailRating(0)}
                        />
                        {[...Array(5)].map((_, i) => {
                          const displayRating = detailHoverRating !== null ? detailHoverRating : editDetailRating;
                          const isFull = displayRating >= (i + 1) * 2;
                          const isHalf = displayRating === (i * 2) + 1;
                          const starClass = (isFull || isHalf) ? "visible-star filled" : "visible-star";
                          return (
                            <div key={i} className="interactive-star-area">
                              {isFull ? <FaStar className={starClass} /> : isHalf ? <FaStarHalfAlt className={starClass} /> : <FaRegStar className={starClass} />}
                              <div className="click-zones-wrapper">
                                <div className="click-zone left-half" onMouseEnter={() => setDetailHoverRating(i * 2 + 1)} onClick={() => setEditDetailRating(i * 2 + 1)} />
                                <div className="click-zone right-half" onMouseEnter={() => setDetailHoverRating(i * 2 + 2)} onClick={() => setEditDetailRating(i * 2 + 2)} />
                              </div>
                            </div>
                          );
                        })}
                        <span className="rating-num-display">{detailHoverRating !== null ? detailHoverRating : editDetailRating}점</span>
                      </div>
                      <textarea 
                        className="write-textarea" 
                        value={editDetailComment} 
                        onChange={(e) => setEditDetailComment(e.target.value)}
                        maxLength={200} 
                        autoFocus
                      />
                      <div className="char-count" style={{ textAlign: 'right', fontSize: '12px', color: '#888', marginTop: '5px' }}>
                        {editDetailComment.length} / 200
                      </div>
                    </div>
                  ) : (
                    <div className="my-review-body">
                      {renderStars(myReview.rating)}
                      <p className="my-comment-text">{myReview.comment}</p>
                    </div>
                  )}
                </div>
              ) : (
                <div className="write-form-container">
                  <div className="interactive-rating-stars-wrapper" onMouseLeave={() => setHoverRating(null)}>
                    <div 
                      className="rating-zero-zone"
                      style={{ width: '20px', minHeight: '30px', cursor: 'pointer' }}
                      onMouseEnter={() => setHoverRating(0)}
                      onClick={() => setNewRating(0)}
                    />
                    {[...Array(5)].map((_, i) => {
                      const displayRating = hoverRating !== null ? hoverRating : newRating;
                      const isFull = displayRating >= (i + 1) * 2;
                      const isHalf = displayRating === (i * 2) + 1;
                      const starClass = (isFull || isHalf) ? "visible-star filled" : "visible-star";
                      return (
                        <div key={i} className="interactive-star-area">
                          {isFull ? <FaStar className={starClass} /> : isHalf ? <FaStarHalfAlt className={starClass} /> : <FaRegStar className={starClass} />}
                          <div className="click-zones-wrapper">
                            <div className="click-zone left-half" onMouseEnter={() => setHoverRating(i * 2 + 1)} onClick={() => setNewRating(i * 2 + 1)} />
                            <div className="click-zone right-half" onMouseEnter={() => setHoverRating(i * 2 + 2)} onClick={() => setNewRating(i * 2 + 2)} />
                          </div>
                        </div>
                      );
                    })}
                    <span className="rating-num-display">{hoverRating !== null ? hoverRating : newRating}점</span>
                  </div>
                  <textarea 
                    className="write-textarea" 
                    placeholder="감상을 남겨주세요. (최대 200자)" 
                    value={newComment} 
                    onChange={(e) => setNewComment(e.target.value)} 
                    maxLength={200}
                  />
                  <div className="char-count">{newComment.length} / 200</div>
                  <button className="btn-submit-review" onClick={handleReviewSubmit}>리뷰 등록</button>
                </div>
              )}
            </section>
          </div>
        </div>
      </main>
      
      {isReportModalOpen && (
        <div className="modal-overlay" onClick={() => setIsReportModalOpen(false)}>
          <div className="report-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>리뷰 신고하기</h3>
              <button className="close-btn" onClick={() => setIsReportModalOpen(false)}>
                <FaTimes />
              </button>
            </div>

            <div className="modal-body">
              <div className="report-title-row">
                <p className="report-guide">신고 사유를 선택해주세요.</p>
              </div>
              <div className="report-options-grid">
                {reportOptions.map((opt) => (
                  <label key={opt.value} className="report-radio-label">
                    <input 
                      type="radio" 
                      name="reportReason" 
                      value={opt.value} 
                      onChange={(e) => setReportReason(e.target.value)}
                    />
                    <span className="label-text">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn-cancel" onClick={() => setIsReportModalOpen(false)}>취소</button>
              <button className="btn-report-submit" onClick={handleReportSubmit}>제출하기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MovieDetail;