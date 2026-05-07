import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  FaStar, FaRegStar, FaStarHalfAlt, FaTrashAlt, FaEdit, 
  FaChevronLeft, FaChevronRight, FaCheck, FaTimes 
} from 'react-icons/fa'; 
import './MyReviewsPage.css';
import axios from 'axios';

interface MyReviewItem {
  reviewId: number;
  contentId: number;
  movieTitle: string;
  posterPath: string;
  rating: number;
  comment: string;
  createdAt: string;
  updatedAt?: string;
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

const MyReviewsPage: React.FC = () => {
  const [reviews, setReviews] = useState<MyReviewItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [expandedReviews, setExpandedReviews] = useState<number[]>([]);
  const reviewsPerPage = 3;
  const navigate = useNavigate();

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editContent, setEditContent] = useState("");

  const [editRating, setEditRating] = useState(0);
  const [editHoverRating, setEditHoverRating] = useState<number | null>(null);

  useEffect(() => {
    const fetchMyReviews = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }
      try {
        setLoading(true);
        const response = await axios.get('http://localhost:8080/api/user-reviews/me', {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (response.data && response.data.success) {
          const sortedData = [...response.data.data].sort((a, b) => {
            const timeA = new Date(a.updatedAt || a.createdAt).getTime();
            const timeB = new Date(b.updatedAt || b.createdAt).getTime();
            return timeB - timeA;
          });
          setReviews(sortedData);
        }
      } catch (error) {
        console.error("로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchMyReviews();
  }, [navigate]);

  const startEdit = (e: React.MouseEvent, review: MyReviewItem) => {
    e.stopPropagation();
    setEditingId(review.reviewId);
    setEditContent(review.comment);
    setEditRating(review.rating); 
  };

  const cancelEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingId(null);
    setEditContent("");
    setEditRating(0);
    setEditHoverRating(null);
  };

  const handleSave = async (e: React.MouseEvent, reviewId: number) => { 
    e.stopPropagation();
    if (!editContent.trim()) return;

    try {
      const token = localStorage.getItem('token');
      const response = await axios.put(`http://localhost:8080/api/contents/user-reviews/${reviewId}`, 
        { comment: editContent, rating: editRating.toString() },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (response.data.success) {
        const updatedTime = new Date().toISOString();
        setReviews(prev => prev.map(r => 
          r.reviewId === reviewId 
            ? { ...r, comment: editContent, rating: editRating, updatedAt: updatedTime }
            : r
        ));
        setEditingId(null);
        alert("수정되었습니다!");
      }
    } catch (error) {
      alert("수정 권한이 없거나 실패했습니다.");
    }
  };

  const handleDelete = async (e: React.MouseEvent, reviewId: number) => {
    e.stopPropagation();
    if (!window.confirm("리뷰를 삭제하시겠습니까?")) return;
    try {
      const token = localStorage.getItem('token');
      const response = await axios.delete(`http://localhost:8080/api/contents/user-reviews/${reviewId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.data && response.data.success) {
        setReviews(reviews.filter(r => r.reviewId !== reviewId));
        if (currentReviews.length === 1 && currentPage > 1) {
          setCurrentPage(currentPage - 1);
        }
      }
    } catch (error) {
      alert("삭제 실패!");
    }
  };

  const handleDeleteAll = async () => {
    if (!window.confirm('정말로 모든 리뷰를 삭제하시겠습니까?')) return;
    const token = localStorage.getItem('token');
    try {
      const response = await axios.delete(`http://localhost:8080/api/user-reviews/me`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.data.success) {
        setReviews([]);
        alert('모든 리뷰가 삭제되었습니다.');
      }
    } catch (error) {
      alert('전체 삭제에 실패했습니다.');
    }
  };

  const toggleReview = (e: React.MouseEvent, reviewId: number) => {
    e.stopPropagation();
    setExpandedReviews(prev => 
      prev.includes(reviewId) ? prev.filter(id => id !== reviewId) : [...prev, reviewId]
    );
  };

  const renderStars = (rating: number) => {
    const num = rating / 2;
    return (
      <div className="review-stars-row">
        {[...Array(5)].map((_, i) => {
          const isFull = num >= i + 1;
          const isHalf = num >= i + 0.5 && num < i + 1;
          return isFull ? (
            <FaStar key={i} className="star-icon filled" />
          ) : isHalf ? (
            <FaStarHalfAlt key={i} className="star-icon filled" />
          ) : (
            <FaRegStar key={i} className="star-icon" />
          );
        })}
        <span className="rating-text">{rating}점</span>
      </div>
    );
  };

  const indexOfLastReview = currentPage * reviewsPerPage;
  const indexOfFirstReview = indexOfLastReview - reviewsPerPage;
  const currentReviews = reviews.slice(indexOfFirstReview, indexOfLastReview);
  const totalPages = Math.ceil(reviews.length / reviewsPerPage);

  if (loading) return <div className="loading-msg">리뷰 불러오는 중...</div>;

  return (
    <div className="my-reviews-container">
      <div className="reviews-card-box">
        <header className="reviews-header">
          <button className="back-btn" onClick={() => navigate(-1)}>
            <FaChevronLeft />
          </button>
          <h2 className="title-text">
            내가 작성한 리뷰 <span className="count-num">{reviews.length}</span>
          </h2>
          {reviews.length > 0 && (
            <button className="delete-all-btn" onClick={handleDeleteAll}>전체 삭제</button>
          )}
        </header>

        {reviews.length > 0 ? (
          <>
            <div className="reviews-list-wrapper">
              {currentReviews.map((review) => {
                const isEditing = editingId === review.reviewId;
                const isExpanded = expandedReviews.includes(review.reviewId);
                const isLongText = review.comment.length > 50;

                return (
                  <div 
                    key={review.reviewId} 
                    className="review-item-card" 
                    onClick={() => !isEditing && navigate(`/movie/${review.contentId}`)}
                  >
                    <div className="poster-wrapper">
                      <img 
                        src={review.posterPath && review.posterPath.startsWith('http') 
                          ? review.posterPath 
                          : `https://image.tmdb.org/t/p/w200${review.posterPath}`} 
                        alt={review.movieTitle} 
                        className="small-poster"
                      />
                    </div>
                    
                    <div className="review-main-info">
                      <div className="info-top-row">
                        <h3 className="movie-name">{review.movieTitle}</h3>
                        <div className="action-btns">
                          {isEditing ? (
                            <>
                              <button 
                                className="save-icon-btn" 
                                onClick={(e) => handleSave(e, review.reviewId)}
                              >
                                <FaCheck />
                              </button>
                              <button className="cancel-icon-btn" onClick={cancelEdit}>
                                <FaTimes />
                              </button>
                            </>
                          ) : (
                            <>
                              <button className="edit-icon-btn" onClick={(e) => startEdit(e, review)}>
                                <FaEdit />
                              </button>
                              <button className="delete-icon-btn" onClick={(e) => handleDelete(e, review.reviewId)}>
                                <FaTrashAlt />
                              </button>
                            </>
                          )}
                        </div>
                      </div>
                      
                      {isEditing ? (
                        <div className="interactive-stars-row" onMouseLeave={() => setEditHoverRating(null)}>
                          {/* 0점 조절용 투명 영역 */}
                          <div 
                            className="rating-zero-zone"
                            onMouseEnter={() => setEditHoverRating(0)}
                            onClick={(e) => { e.stopPropagation(); setEditRating(0); }}
                          />

                          {[...Array(5)].map((_, i) => {
                            const displayRating = editHoverRating !== null ? editHoverRating : editRating;
                            const isFull = displayRating >= (i + 1) * 2;
                            const isHalf = displayRating === (i * 2) + 1;
                            return (
                              <div key={i} className="interactive-star-unit">
                                {isFull ? <FaStar className="star-icon filled" /> : isHalf ? <FaStarHalfAlt className="star-icon filled" /> : <FaRegStar className="star-icon" />}
                                <div 
                                  className="star-click-zone left"
                                  onMouseEnter={() => setEditHoverRating(i * 2 + 1)} 
                                  onClick={(e) => { e.stopPropagation(); setEditRating(i * 2 + 1); }} 
                                />
                                <div 
                                  className="star-click-zone right"
                                  onMouseEnter={() => setEditHoverRating(i * 2 + 2)} 
                                  onClick={(e) => { e.stopPropagation(); setEditRating(i * 2 + 2); }} 
                                />
                              </div>
                            );
                          })}
                          <span className="edit-rating-num">
                            {editHoverRating !== null ? editHoverRating : editRating}점
                          </span>
                        </div>
                      ) : (
                        renderStars(review.rating)
                      )}
                      
                      {isEditing ? (
                        <div className="edit-input-wrapper">
                          <textarea 
                            className="edit-textarea"
                            value={editContent}
                            onChange={(e) => setEditContent(e.target.value)}
                            onClick={(e) => e.stopPropagation()} 
                            maxLength={200}
                            autoFocus
                          />
                          <div className="edit-char-count">
                            {editContent.length} / 200
                          </div>
                        </div>
                      ) : (
                        <p className={`user-comment-text ${isExpanded ? 'expanded' : 'clamped'}`}>
                          {review.comment}
                        </p>
                      )}

                      {!isEditing && isLongText && (
                        <button className="btn-more-detail" onClick={(e) => toggleReview(e, review.reviewId)}>
                          {isExpanded ? '접기' : '자세히 보기'}
                        </button>
                      )}
                      
                      <span className="created-date-label">
                        {formatDate(review.updatedAt ? review.updatedAt : review.createdAt)}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>

            {totalPages > 1 && (
              <div className="pagination-wrapper">
                <button 
                  className="page-btn" 
                  disabled={currentPage === 1} 
                  onClick={() => setCurrentPage(currentPage - 1)}
                >
                  <FaChevronLeft />
                </button>
                <span className="page-info">{currentPage} / {totalPages}</span>
                <button 
                  className="page-btn" 
                  disabled={currentPage === totalPages} 
                  onClick={() => setCurrentPage(currentPage + 1)}
                >
                  <FaChevronRight />
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="empty-reviews">
            <p>작성한 리뷰가 없습니다.</p>
            <button className="go-home-btn" onClick={() => navigate('/')}>영화 보러가기</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyReviewsPage;