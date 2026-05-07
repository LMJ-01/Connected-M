import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './WishlistPage.css';
import { FaBookmark, FaChevronLeft, FaChevronRight } from 'react-icons/fa'; // 🚀 화살표 아이콘 추가
import axios from 'axios';

interface WishlistItem {
  wishlistId: number;
  contentId: number;
  title: string;
  posterPath: string;
}

const WishlistPage: React.FC = () => {
  const [wishlist, setWishlist] = useState<WishlistItem[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // 🚀 [추가] 기존 코드는 건들지 않고 상태만 추가합니다.
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 3;

  useEffect(() => {
    const fetchWishlist = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('token');
        
        const response = await axios.get('http://localhost:8080/api/members/wishlist', {
          headers: { Authorization: `Bearer ${token}` }
        });

        if (response.data) {
          const mappedData = response.data.map((item: any) => ({
            wishlistId: item.wishlistId,
            contentId: item.contentId,
            title: item.title,
            posterPath: item.posterPath 
          }));
          setWishlist(mappedData);
        }
      } catch (error) {
        console.error("찜 목록 로딩 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchWishlist();
  }, []);

  const handleRemove = async (e: React.MouseEvent, contentId: number) => {
    e.stopPropagation();
    if (!window.confirm("찜 목록에서 삭제하시겠습니까?")) return;

    try {
      const token = localStorage.getItem('token');
      
      await axios.post(`http://localhost:8080/api/members/wishlist/${contentId}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setWishlist(prev => prev.filter(item => item.contentId !== contentId));
      
      const savedWish = JSON.parse(localStorage.getItem('wishlist') || '[]');
      const filtered = savedWish.filter((item: any) => item.contentId !== contentId);
      localStorage.setItem('wishlist', JSON.stringify(filtered));

      // 🚀 삭제 후 현재 페이지에 데이터가 없으면 앞으로 이동하는 로직만 추가 ㅋ
      const totalAfterDelete = wishlist.length - 1;
      const maxPage = Math.ceil(totalAfterDelete / itemsPerPage);
      if (currentPage > maxPage && maxPage > 0) setCurrentPage(maxPage);

    } catch (error) {
      console.error("삭제 실패:", error);
    }
  };

  if (loading) return <div className="wishlist-loading">찜 목록을 불러오는 중...</div>;

  // 🚀 [추가] 3개씩 잘라내기 위한 계산 로직
  const totalPages = Math.ceil(wishlist.length / itemsPerPage);
  const currentItems = wishlist.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  return (
    <div className="wishlist-container-wrap">
      <div className="wishlist-card-box">
        <header className="wishlist-header-row">
          <button className="back-arrow" onClick={() => navigate(-1)}>❮</button>
          <h2 className="wishlist-main-title">찜 목록 <span>{wishlist.length}</span></h2>
        </header>

        {wishlist.length > 0 ? (
          <>
            <div className="wishlist-items-grid">
              {/* 🚀 wishlist 대신 3개로 잘린 currentItems를 맵핑합니다. ㅋ */}
              {currentItems.map((item) => (
                <div 
                  key={item.contentId} 
                  className="wish-movie-item"
                  onClick={() => navigate(`/movie/${item.contentId}`)} 
                >
                  <div className="wish-poster-frame">
                    {item.posterPath ? (
                      <img 
                        src={item.posterPath.startsWith('http') ? item.posterPath : `https://image.tmdb.org/t/p/w300${item.posterPath}`} 
                        alt={item.title} 
                      />
                    ) : (
                      <div className="no-image-placeholder">이미지 없음</div>
                    )}
                    <button 
                      className="wish-cancel-icon" 
                      onClick={(e) => handleRemove(e, item.contentId)}
                    >
                      <FaBookmark />
                    </button>
                  </div>
                  <div className="wish-movie-desc">
                    <h3 className="wish-movie-name">{item.title}</h3>
                  </div>
                </div>
              ))}
            </div>

            {/* 🚀 [추가] 최근 본 목록과 동일한 스타일의 페이지네이션 UI */}
            {totalPages > 1 && (
              <div className="wishlist-pagination-bar">
                <button 
                  className="page-nav-btn"
                  disabled={currentPage === 1} 
                  onClick={() => setCurrentPage(prev => prev - 1)}
                >
                  <FaChevronLeft />
                </button>
                <span className="page-indicator">{currentPage} / {totalPages}</span>
                <button 
                  className="page-nav-btn"
                  disabled={currentPage === totalPages} 
                  onClick={() => setCurrentPage(prev => prev + 1)}
                >
                  <FaChevronRight />
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="wishlist-empty-box">
            <p>찜한 영화가 없습니다.</p>
            <button className="go-home-link" onClick={() => navigate('/')}>영화 보러가기</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default WishlistPage;