import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import './adminpage.css';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const AdminPage = () => {
  const [mainTab, setMainTab] = useState('user'); 
  const [subTab, setSubTab] = useState('filter'); 
  const [users, setUsers] = useState<any[]>([]);
  const [reportedReviews, setReportedReviews] = useState<any[]>([]); 

  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  
  // ✨ 초기값을 'ACTIVE'(정상)로 설정[cite: 10]
  const [statusFilter, setStatusFilter] = useState('ACTIVE');
  
  const [summarySortTarget, setSummarySortTarget] = useState<'review' | 'wish'>('review');
  const [selectedMonth, setSelectedMonth] = useState('ALL');

  useEffect(() => {
    const init = async () => {
      const userData = localStorage.getItem('user'); 
      if (!userData) { window.location.href = "/login"; return; }
      
      try {
        const user = JSON.parse(userData);
        if (user.role !== 'ROLE_ADMIN') {
          alert("관리자 권한이 없습니다.");
          window.location.href = "/";
          return;
        }

        const token = localStorage.getItem('token'); 
        const response = await axios.get(`${import.meta.env.VITE_API_URL}/api/admin/users`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        const userDataList = response.data.content || []; 
        setUsers([...userDataList].sort((a, b) => b.userId - a.userId)); 
      } catch (error) {
        console.error("데이터 로딩 실패", error);
      }
    };
    init();
  }, []);

  useEffect(() => {
    if (subTab === 'reviews') {
      const fetchReportedReviews = async () => {
        try {
          const token = localStorage.getItem('token');
          const response = await axios.get(`${import.meta.env.VITE_API_URL}/api/admin/reviews/reported`, {
              headers: { Authorization: `Bearer ${token}` }
          });
          setReportedReviews(response.data || []);
        } catch (error) {
          console.error("신고 목록 조회 실패:", error);
        }
      };
      fetchReportedReviews();
    }
  }, [subTab]);

  const handleStatusChange = async (userId: number, newStatus: string) => {
    const serverStatus = newStatus === 'SUSPENDED' ? 'BANNED' : 'ACTIVE';
    const confirmMsg = newStatus === 'ACTIVE' ? "계정을 활성화하시겠습니까?" : "해당 유저를 정지 처리하시겠습니까?";
    
    if (!window.confirm(confirmMsg)) return;

    try {
      const token = localStorage.getItem('token');
      await axios.patch(`${import.meta.env.VITE_API_URL}/api/admin/users/${userId}/status?status=${serverStatus}`, 
          {}, 
          { headers: { Authorization: `Bearer ${token}` } }
      );

      setUsers(prev => prev.map(u => u.userId === userId ? { ...u, status: serverStatus } : u));
      alert(serverStatus === 'ACTIVE' ? "계정이 활성화되었습니다." : "정지 처리되었습니다.");
    } catch (error) {
      console.error("상태 변경 실패:", error);
      alert("상태 변경에 실패했습니다.");
    }
  };

  const handleExit = async (user: any) => {
    if (window.confirm(`${user.nickname}님을 정말 탈퇴 처리하시겠습니까?`)) {
      try {
        const token = localStorage.getItem('token');
        await axios.patch(`${import.meta.env.VITE_API_URL}/api/user/${user.userId}/withdraw`, 
            {}, 
            { headers: { Authorization: `Bearer ${token}` } }
        );

        setUsers(prev => prev.map(u => 
          u.userId === user.userId 
            ? { ...u, status: 'WITHDRAWN', nickname: '탈퇴유저_진행됨', email: 'masked@deleted.com' } 
            : u
        ));
        
        alert("성공적으로 탈퇴 처리되었습니다.");
      } catch (error: any) {
        console.error("탈퇴 처리 실패:", error);
        alert("탈퇴 처리에 실패했습니다.");
      }
    }
  };

  const filteredUsers = users.filter(user => {
    if (!user.createdAt) return false;
    const userDate = user.createdAt.split('T')[0];

    if (startDate && userDate < startDate) return false;
    if (endDate && userDate > endDate) return false;
    
    // ✨ 필터링 로직 업데이트[cite: 10]
    if (statusFilter !== 'ALL') {
        if (statusFilter === 'ACTIVE' && user.status !== 'ACTIVE') return false;
        if (statusFilter === 'SUSPENDED' && (user.status !== 'BANNED' && user.status !== 'SUSPENDED')) return false;
        if (statusFilter === 'WITHDRAWN' && user.status !== 'WITHDRAWN') return false;
    }

    const term = searchTerm.toLowerCase();
    return (
      String(user.userId).includes(term) ||
      (user.email || "").toLowerCase().includes(term) ||
      (user.nickname || "").toLowerCase().includes(term) ||
      (user.realName || "").toLowerCase().includes(term) ||
      (user.phoneNumber || "").includes(term)
    );
  });

  const processedChartData = useMemo(() => {
    const counts: { [key: string]: number } = {};
    users.forEach(user => {
      if (user.createdAt) {
        const fullDate = user.createdAt.split('T')[0];
        const month = fullDate.split('-')[1];

        if (selectedMonth === 'ALL') {
          const label = `${month}월`;
          counts[label] = (counts[label] || 0) + 1;
        } else {
          if (month === selectedMonth) {
            const label = fullDate.substring(5);
            counts[label] = (counts[label] || 0) + 1;
          }
        }
      }
    });

    return Object.keys(counts).sort().map(key => ({
      name: key,
      users: counts[key]
    }));
  }, [users, selectedMonth]);

  return (
    <div className="admin-container">
      <header className="admin-header">
        <h1>관리자 세션으로 접속되었습니다.</h1>
      </header>

      <div className="tab-group main-tabs">
        <button className={mainTab === 'user' ? 'active' : ''} onClick={() => {setMainTab('user'); setSubTab('filter');}}>유저관리</button>
        <button className={mainTab === 'stats' ? 'active' : ''} onClick={() => {setMainTab('stats'); setSubTab('newuser');}}>통계</button>
      </div>

      <div className="tab-group sub-tabs">
        {mainTab === 'user' && (
          <>
            <button className={subTab === 'filter' ? 'active' : ''} onClick={() => setSubTab('filter')}>전체유저</button>
            <button className={subTab === 'reviews' ? 'active' : ''} onClick={() => setSubTab('reviews')}>신고 목록 조회</button>
          </>
        )}
        {mainTab === 'stats' && (
          <>
            <button className={subTab === 'newuser' ? 'active' : ''} onClick={() => setSubTab('newuser')}>신규 가입자 추이</button>
            <button className={subTab === 'summary' ? 'active' : ''} onClick={() => setSubTab('summary')}>활동 요약</button>
          </>
        )}
      </div>

      <main className="admin-content">
        {mainTab === 'user' && subTab === 'filter' && (
          <>
            <div className="filter-control-bar">
              <div className="filter-group">
                <label>가입일자</label>
                <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
                <span className="date-separator">~</span>
                <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
              </div>
              
              <div className="filter-group">
                <label>상태</label>
                <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                  <option value="ALL">전체</option>
                  <option value="ACTIVE">정상</option>
                  <option value="SUSPENDED">정지됨</option>
                  <option value="WITHDRAWN">탈퇴완료</option> {/* ✨ 옵션 추가[cite: 10] */}
                </select>
              </div>

              <div className="filter-group search-group">
                <input type="text" placeholder="ID, 이메일, 이름, 닉네임 검색..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
              </div>
            </div>
            <div className="user-table-container">
              <table className="user-table">
                <thead>
                  <tr>
                    <th>ID</th><th>가입일</th><th>이메일</th><th>닉네임</th><th>전화번호</th><th>이름</th><th>상태</th><th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredUsers.map((user) => (
                    <tr key={user.userId}>
                      <td className="txt-id">{user.userId}</td>
                      <td>{user.createdAt?.replace('T', ' ').split('.')[0]}</td>
                      <td className="txt-email">{user.email}</td>
                      <td>{user.nickname}</td>
                      <td>{user.phoneNumber}</td>
                      <td>{user.realName}</td>
                      <td>
                        <span className={`status-badge ${user.status}`}>
                          {user.status === 'ACTIVE' ? '정상' : 
                            user.status === 'BANNED' ? '정지됨' : 
                            user.status === 'WITHDRAWN' ? '탈퇴완료' : user.status} 
                        </span>
                      </td>
                      <td className="action-cell">
                        <button className="btn-table active-btn" onClick={() => handleStatusChange(user.userId, 'ACTIVE')}>활성</button>
                        <button className="btn-table stop-btn" onClick={() => handleStatusChange(user.userId, 'SUSPENDED')}>정지</button>
                        <button className="btn-table exit-btn" onClick={() => handleExit(user)}>탈퇴</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
        
        {mainTab === 'user' && subTab === 'reviews' && (
          <div className="report-grid">
            {reportedReviews.length > 0 ? (
              reportedReviews.map((report) => (
                <div key={report.id} className="report-card">
                  <div className="report-card-header">
                    <div className="nickname-group">
                      <span className="target-user">신고당한 유저: {report.targetNickname}</span>
                      <span className="reporter-user">신고자: {report.reporterNickname}</span>
                    </div>
                  </div>
                  <div className="report-review-text">{report.reviewText}</div>
                  <div className="report-footer">
                    <span className="report-reason-badge">{report.reason}</span>
                    <small style={{color: '#999'}}>{report.createdAt?.split('T')[0]}</small>
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-view">신고된 내역이 없습니다.</div>
            )}
          </div>
        )}
        
        {mainTab === 'stats' && subTab === 'newuser' && (
          <>
            <div className="filter-control-bar">
              <div className="filter-group">
                <label>조회 범위</label>
                <select value={selectedMonth} onChange={(e) => setSelectedMonth(e.target.value)}>
                  <option value="ALL">전체 기간 (월별 합계)</option>
                  {['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'].map(m => (
                    <option key={m} value={m}>{m}월 (일별 상세)</option>
                  ))}
                </select>
              </div>
            </div>
            <div className="chart-container" style={{ width: '98%', height: 450, padding: '20px' }}>
              <ResponsiveContainer>
                <BarChart data={processedChartData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="name" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="users" fill="#8884d8" name="가입자 수" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </>
        )}

        {mainTab === 'stats' && subTab === 'summary' && (
          <>
            <div className="filter-control-bar">
              <div className="filter-group">
                <label>정렬 기준</label>
                <select value={summarySortTarget} onChange={(e) => setSummarySortTarget(e.target.value as 'review' | 'wish')}>
                  <option value="review">리뷰 작성이 많은 순</option>
                  <option value="wish">찜한 영화가 많은 순</option>
                </select>
              </div>
            </div>
            <div className="user-table-container">
              <table className="user-table">
                <thead>
                  <tr>
                    <th>순위</th><th>닉네임</th><th>작성 리뷰 수</th><th>찜한 영화 수</th>
                  </tr>
                </thead>
                <tbody>
                  {[...users]
                    .sort((a, b) => {
                      if (summarySortTarget === 'review') return (b.reviewCount || 0) - (a.reviewCount || 0);
                      return (b.wishCount || 0) - (a.wishCount || 0);
                    })
                    .map((user, index) => (
                      <tr key={user.userId}>
                        <td>{index + 1}</td>
                        <td>{user.nickname}</td>
                        <td style={{ fontWeight: summarySortTarget === 'review' ? 'bold' : 'normal' }}>{user.reviewCount || 0}개</td>
                        <td style={{ fontWeight: summarySortTarget === 'wish' ? 'bold' : 'normal' }}>{user.wishCount || 0}개</td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default AdminPage;