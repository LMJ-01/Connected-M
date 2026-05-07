import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
// 재생 아이콘(FaPlayCircle)은 더 이상 안 쓰므로 제거했습니다.
import { FaClock, FaChevronLeft, FaChevronRight } from 'react-icons/fa';
import './RecentPage.css';
import axios from 'axios';

interface RecentViewItem {
  id: number;
  contentId: number;
  title: string;
  posterPath: string;
  viewedAt: string;
  detailPath: string;
}

const RecentPage: React.FC = () => {
  const [recentList, setRecentList] = useState<RecentViewItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 3;
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRecentViews = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login');
        return;
      }

      try {
        setLoading(true);
        const response = await axios.get(`${import.meta.env.VITE_API_URL}/api/users/recent`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        if (response.data) {
          setRecentList(response.data);
        }
      } catch (error) {
        console.error("최근 본 목록 로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchRecentViews();
  }, [navigate]);

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = recentList.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(recentList.length / itemsPerPage);

  if (loading) return <div className="loading-msg">목록을 불러오는 중...</div>;

  return (
    <div className="recent-view-container">
      <div className="recent-card-box">
        <header className="recent-header">
          <button className="back-btn" onClick={() => navigate(-1)}><FaChevronLeft /></button>
          <h2 className="title-text">최근 본 영화 <span className="count-num">{recentList.length}</span></h2>
        </header>

        {recentList.length > 0 ? (
          <>
            <div className="recent-list-wrapper">
              {currentItems.map((item) => (
                <div 
                  key={item.id} 
                  className="recent-item-card"
                  onClick={() => navigate(`/movie/${item.contentId}`)}
                >
                  <div className="poster-wrapper">
                    <img 
                      src={item.posterPath && item.posterPath.startsWith('http') ? item.posterPath : `https://image.tmdb.org/t/p/w200${item.posterPath}`} 
                      alt={item.title} 
                      className="small-poster"
                    />
                    {/* ✨ 1. 포스터 위에 있던 재생 버튼(<div className="play-overlay">...</div>) 삭제 완료 ✨ */}
                  </div>
                  
                  <div className="recent-main-info">
                    <h3 className="movie-name">{item.title}</h3>
                    <div className="viewed-time-row">
                      <FaClock className="clock-icon" />
                      {/* ✨ 2. '시청함' 텍스트 삭제 완료 ✨ */}
                      <span className="viewed-at-text">{item.viewedAt}</span>
                    </div>
                    {/* ✨ 3. '다시보기' -> '상세정보 보기'로 텍스트 변경 완료 ✨ */}
                    <button className="re-watch-btn">상세정보 보기</button>
                  </div>
                </div>
              ))}
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
          <div className="empty-recent">
            <p>최근에 본 영화가 없습니다.</p>
            <button className="go-home-btn" onClick={() => navigate('/')}>영화 보러가기</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default RecentPage;