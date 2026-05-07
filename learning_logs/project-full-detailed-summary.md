# Connected_M 전체 프로젝트 상세 정리

## 요약
- React/TypeScript 프론트엔드와 Spring Boot 백엔드, Python AI 챗봇/데이터 모듈이 통합된 영화 추천 서비스입니다.
- 검색, 상세 정보, 찜/리뷰/최근 본 목록, 소셜 로그인, AI 챗봇 상담 기능을 제공합니다.
- `App.tsx` 기반 SPA 라우팅과 공통 레이아웃, JWT 인증/토큰 관리, TMDB API 연동이 핵심입니다.

## 1. 전체 개요
Connected_M은 React/TypeScript 기반의 프론트엔드와 Spring Boot 기반의 메인 백엔드, 그리고 Python 기반의 AI 챗봇 및 데이터 수집 모듈이 결합된 복합 프로젝트입니다.

- 프론트엔드: `c:\team\Connected_M\frontend`
- 백엔드: `c:\team\Connected_M\backend`
- 학습 로그: `c:\team\Connected_M\learning_logs`

## 2. 프론트엔드 상세

### 2.1 핵심 진입점
- `src/main.tsx`
  - React DOM 렌더링 진입점
  - `App` 컴포넌트를 `React.StrictMode`로 마운트
- `src/App.tsx`
  - 전체 앱 구조와 라우팅을 담당
  - `BrowserRouter`로 SPA 라우팅을 감싸고 `Routes` 에서 페이지 컴포넌트 연결
  - `Header`, `Footer`, `Chatbot`, `LoginModal`을 공통 레이아웃으로 포함
  - `axios.interceptors.response`에서 서버 403 응답을 감지하여 계정 정지 시 로컬스토리지 삭제 및 메인 페이지 강제 이동 처리
  - `AppContent` 컴포넌트: 로그인 모달 열기/닫기 상태 관리, `useAuthCheck` 호출로 OAuth2 리다이렉트 검증

#### App.tsx 연결 구조
- `Header` → 검색, 카테고리, OTT, 마이페이지, 로그인 모달 제어
- `Footer` → 공통 하단 레이아웃
- `Chatbot` → 하단 챗봇 UI, 대화 입력 및 서버 호출
- `LoginModal` → 로그인/비밀번호 찾기/소셜 로그인 UI
- `useAuthCheck` → `/oauth2/redirect` 경로에서 토큰 파싱, 정지 유저 감지, 추가 정보 입력 페이지 이동 처리

### 2.2 주요 라우트
App.tsx는 다음 경로를 연결합니다:
- `/` → `Home`
- `/search` → `SearchResult`
- `/register` → `Register`
- `/extra-info` → `ExtraInfo`
- `/reset-password` → `ResetPassword`
- `/oauth2/redirect` → `OAuth2RedirectHandler`
- `/mypage` → `MyPage`
- `/movie/:id` → `MovieDetail`
- `/terms` → `Terms`
- `/privacy` → `Privacy`
- `/edit-profile` → `EditProfile`
- `/wishlist` → `WishlistPage`
- `/my-reviews` → `MyReviewsPage`
- `/recent` → `RecentPage`
- `/genre/:genreName` → `GenrePage`
- `/ott/:providerName` → `OttPage`
- `/keyword` → `KeywordPage`
- `/admin` → `AdminPage`

### 2.2.1 라우트/컴포넌트 매핑과 파일 관계
- `App.tsx`는 `Header`, `Footer`, `Chatbot`, `LoginModal`을 포함하여 공통 레이아웃을 구성하고, `Routes`로 페이지 컴포넌트를 연결합니다.
- `Header.tsx`는 검색 입력과 카테고리/OTT 네비게이션을 관리하며, `navigate()`로 `/search`, `/genre/:genreName`, `/ott/:providerName`, `/mypage` 등으로 이동합니다.
- `LoginModal.tsx`는 로그인/소셜 로그인/비밀번호 재설정 기능을 제공하며, 인증 성공 시 `localStorage`에 `token`과 `nickname`을 저장하고 `App.tsx`에서 상태를 갱신합니다.
- `/oauth2/redirect` 경로는 `OAuth2RedirectHandler.tsx`가 처리하며, `useAuthCheck.tsx`에서 쿼리 파라미터를 파싱하고 필요 시 `ExtraInfo.tsx`로 이동합니다.
- `Home.tsx`는 메인 화면으로, 추천 영화 슬라이더와 장르별 콘텐츠 탭을 렌더링하고 `searchHistory`를 `localStorage`에 저장합니다.
- `SearchResult.tsx`는 `q` 쿼리 파라미터 기반 검색 결과를 요청하고, 검색 결과 항목 클릭 시 `navigate('/movie/' + id)`로 `MovieDetail`로 연결합니다.
- `MovieDetail.tsx`는 `/movie/:id` 경로에서 상세 정보를 로드하고, 찜(`WishlistPage` 연결), 리뷰, 최근 본 목록 및 OTT 링크를 관리합니다.
- `MyPage.tsx`는 하위 페이지(`EditProfile`, `WishlistPage`, `MyReviewsPage`, `RecentPage`)를 렌더링하며, 로그인된 사용자 전용으로 동작합니다.
- `GenrePage.tsx`와 `OttPage.tsx`는 각각 장르 및 OTT 서비스 필터를 처리하며, `Header`와 `Home`의 네비게이션 흐름과 연결됩니다.
- `KeywordPage.tsx`는 키워드 기반 탐색을 제공하며 `/keyword` 경로로 이동 시 검색 UI를 표시합니다.
- `Terms.tsx`, `Privacy.tsx`는 약관/개인정보처리방침 페이지로 별도 라우트만 담당합니다.
- `AdminPage`는 관리자 전용 UI로 `/admin` 경로에서 접근됩니다.
- API 호출은 페이지/컴포넌트별로 백엔드 엔드포인트에 매핑되며, `Chatbot.tsx`는 `/api/ai/recommend`, `LoginModal.tsx`는 `/api/auth/login`, `MovieDetail.tsx`는 `/api/contents/*`, `WishlistPage`는 `/api/members/wishlist/*` 등을 호출합니다.

### 2.3 인증/토큰 처리
- `src/hooks/useAuth.tsx`
  - `localStorage`에서 `token`, `nickname`을 읽어 로그인 상태를 판단
  - `'undefined'`, `'null'` 문자열을 비로그인 처리하여 잘못된 토큰 저장 사례를 방지
  - `isLoggedIn`, `userNickname` 상태를 반환
- `src/hooks/useAuthCheck.tsx`
  - `/oauth2/redirect` 경로에서 OAuth2 로그인 콜백 쿼리 파라미터를 확인
  - `error=BANNED`일 때 토큰 제거, 메인 페이지 이동, 정지 메시지 표시
  - `token`과 `nickname`을 로컬스토리지에 저장
  - `needInfo=true`일 경우 `/extra-info`로 이동하여 추가 정보 입력 유도

### 2.4 공통 레이아웃과 네비게이션
- `src/components/layout/Header.tsx`
  - 로컬스토리지 토큰 확인으로 로그인 상태 표시
  - 입력 검색어 `searchTerm` 상태 관리, Enter 키로 검색 경로 이동
  - 로그인 시 닉네임 표시, 로그아웃 시 localStorage 삭제 및 페이지 리로드
  - 카테고리 드롭다운과 OTT 드롭다운을 렌더링하며 클릭 시 `navigate()`로 이동
  - `마이페이지` 버튼은 로그인 상태이면 `/mypage`로 이동, 아니면 로그인 모달 오픈
- `src/components/layout/Footer.tsx`
  - 공통 하단 푸터 렌더링

### 2.5 로그인/소셜 로그인/비밀번호 찾기
- `src/components/common/LoginModal.tsx`
  - 이메일/비밀번호 로그인 폼과 비밀번호 찾기 전환 UI 제공
  - `handleSubmit()`으로 백엔드 `/api/auth/login` 호출, 토큰과 닉네임을 로컬스토리지에 저장
  - `handleSocialLogin()`로 카카오/구글 OAuth2 인증 URL로 리다이렉트
  - `handleFindPassword()`로 비밀번호 재설정 이메일 발송 요청
  - 로그인 성공 후 모달 닫기 및 페이지 새로고침 처리

### 2.6 챗봇
- `src/components/chatbot/Chatbot.tsx`
  - 우측 하단 토글 버튼으로 채팅 모달 열기/닫기
  - 사용자 입력과 봇 메시지 상태를 `messages` 배열로 관리
  - `handleSendMessage()`로 백엔드 `/api/ai/recommend` 호출
  - JWT 토큰을 헤더에 `Bearer` 인증으로 추가
  - 서버 응답을 bot 메시지로 표시, 실패 시 에러 메시지 출력
  - 채팅창 외부 클릭 시 닫기, 자동 스크롤, Enter 키 전송 지원

### 2.7 메인 페이지(Home)
- `src/pages/Home/Home.tsx`
  - 검색창 및 검색 기록 관리
    - 로컬스토리지 `searchHistory`에서 최대 5개까지 저장
    - 검색 결과로 이동, 검색 기록 리스트 클릭 이동, 기록 삭제 기능
  - 추천 영화 Hero 슬라이더
    - 백엔드 `/api/contents/random` 호출로 랜덤 영화 가져오기
    - 3D 스타일 슬라이더 애니메이션과 좌우 이동 버튼
    - 터치 스와이프 및 마우스 호버 일시정지 처리
  - 장르별 콘텐츠 섹션
    - `activeGenre` 상태로 장르 탭 선택
    - 백엔드 `/api/contents/category?genreId=` 호출로 영화 목록 로드
    - 장르 버튼으로 다른 장르 콘텐츠 바로 조회

### 2.8 검색 결과 페이지
- `src/pages/SearchResult/SearchResult.tsx`
  - URL 쿼리 `q`를 `useSearchParams()`로 읽음
  - 백엔드 `/api/contents/search?query=` 호출로 검색 결과 요청
  - 색인된 검색 결과를 `results` 상태로 관리
  - 동적 장르 필터링: 검색 결과에 포함된 장르만 자동 생성
  - 페이지네이션 구성: 한 페이지에 6개 항목 표시
  - 영화 카드 클릭 시 `/movie/:id`로 이동

### 2.9 영화 상세 페이지(MovieDetail)
- `src/pages/MovieDetail/Moviedetail.tsx`
  - URL 파라미터 `id`로 콘텐츠 상세 정보 로드
  - 백엔드 `/api/contents/${id}` 호출로 영화 정보, AI 요약, 전문가 리뷰, 사용자 리뷰 등 수신
  - `useAuth()`를 이용해 로그인 여부와 닉네임을 확인
  - 찜 토글: `/api/members/wishlist/${id}` 호출
  - 최근 본 목록 저장: 로그인 상태에서 `/api/users/recent/${id}` POST
  - 리뷰 작성/수정: `/api/contents/user-reviews` POST/PUT 호출
  - 리뷰 신고 모달 UI와 신고 제출 처리
  - OTT 로고를 기반으로 시청 페이지 링크 매핑
  - `hasReviewed`로 중복 리뷰 방지, `isWishlisted`로 찜 상태 표시

### 2.10 공통 API helper
- `src/api/tmdb.ts`
  - `VITE_TMDB_API_KEY`, `VITE_TMDB_BASE_URL` 환경 변수 사용
  - `fetchPopularMovies()`로 TMDB 인기 영화 호출
  - HTTP 상태 검사 및 JSON 파싱 예외 처리 포함

### 2.11 페이지 구성
#### Home
- `src/pages/Home/Home.tsx`
- 스타일: `Home.css`
- 문서: `home.md`

#### MyPage
- `src/pages/MyPage/MyPage.tsx`
- 하위 페이지
  - `EditProfile.tsx` / `EditProfile.css`
  - `WishlistPage.tsx` / `WishlistPage.css`
  - `MyReviewsPage.tsx` / `MyReviewsPage.css`
  - `RecentPage.tsx` / `RecentPage.css`
- 스타일: `MyPage.css`

#### Register
- `src/pages/Register/Register.tsx`
- 스타일: `Register.css`
- 문서: `register.md`

#### Auth
- `OAuth2RedirectHandler.tsx` → 소셜 로그인 리다이렉트 응답 처리
- `ExtraInfo.tsx` → 추가 정보 입력 화면
- `ResetPassword.tsx` → 비밀번호 재설정 화면

#### MovieDetail
- `Moviedetail.tsx` → 영화 상세 정보 페이지
- 스타일: `Moviedetail.css`
- 문서: `moviedetail.md`
- 중복 파일: `Moviedetail copy.tsx`, `Moviedetail copy.css`

#### SearchResult
- `SearchResult.tsx`, `SearchResult.css`
- 키워드/제목 검색 결과 표시

#### Genre
- `GenrePage.tsx`, `GenrePage.css`
- 장르별 영화 리스트

#### Ott
- `OttPage.tsx`, `OttPage.css`
- OTT 서비스별 콘텐츠 리스트

#### Keyword
- `KeywordPage.tsx`, `KeywordPage.css`
- 문서: `keyword.md`
- 키워드 기반 검색 페이지

#### Terms / Privacy
- `Terms.tsx`, `Terms.css`, `Terms.md`
- `Privacy.tsx`, `Privacy.css`, `Privacy.md`

#### Admin
- `adminpage.tsx`, `adminpage.css`
- 관리자 전용 화면

### 2.12 공통 컴포넌트
- `src/components/common/LoginModal.tsx`
  - 로그인/비밀번호 찾기/소셜 로그인 UI
  - API 호출과 에러 분기 처리 포함
- `src/components/chatbot/Chatbot.tsx`
  - 챗봇 UI, 메시지 전송, 토큰 인증, 자동 스크롤
- 레이아웃
  - `Footer.tsx` / `Footer.css`
  - `Header.tsx` / `Header.css`

### 2.13 정적 자원과 문서
- `src/assets/img/` → 로고 및 이미지 자산
- `.md` 파일들 → 각 폴더 설명/문서
  - `components/commponets.md`
  - `hooks/hooks.md`
  - `pages/page.md`
  - `services/services.md`
  - `store/store.md`
  - `types/types.md`
  - `utils/utils.md`

### 2.14 Frontend 실행 명령
- `npm install`
- `npm run dev`
- `npm run build`

## 3. 백엔드 상세

### 3.1 Gradle 및 실행 환경
- `build.gradle`
  - Java 17 사용
  - Spring Boot Starter Web, WebFlux, Security, JPA, Validation, Mail
  - MariaDB JDBC 드라이버
  - OAuth2, JWT, Swagger(OpenAPI)
- `settings.gradle`
  - 프로젝트 이름: `backend`
- `gradlew`, `gradlew.bat`
  - 로컬 Gradle 실행 스크립트

### 3.2 서버 진입점
- `src/main/java/com/Connectedm/backend/BackendApplication.java`
  - Spring Boot 애플리케이션 시작

### 3.3 설정 폴더
- `src/main/java/com/Connectedm/backend/config/`
  - `Appconfig.java` : 전역 설정
  - `DataInitializer.java` : 초기 데이터 적재 가능성
  - `RestTemplateConfig.java` : WebClient/RestTemplate 설정
  - `SecurityConfig.java` : Spring Security와 OAuth2/JWT 설정

### 3.4 리소스 설정
- `src/main/resources/application.properties`
  - MariaDB 연결 정보
  - JPA/Hibernate 설정
  - Swagger/OpenAPI 경로
  - Kakao/Google OAuth2 설정
  - JWT 비밀키 및 만료 시간
  - 이메일 SMTP 설정
  - Gemini API 키 및 Python AI 서버 URL `external-api.python-ai-url`
- `src/main/resources/api-keys.properties`
  - 민감 정보 분리용
- `src/main/resources/data.sql`
  - DB 초기 데이터

### 3.5 도메인 구조
#### 3.5.1 `domain/auth`
- 컨트롤러
  - `AuthController.java` : 인증 관련 API
    - `login()`: 로컬 로그인 처리
    - `signup()`: 회원가입 처리
    - `oauth2Login()`: 소셜 로그인 리다이렉트
    - `oauth2Callback()`: 소셜 로그인 콜백 처리
    - `refreshToken()`: 토큰 갱신
    - `logout()`: 로그아웃 처리
    - `resetPassword()`: 비밀번호 재설정
    - `verifyResetToken()`: 재설정 토큰 검증
- 서비스
  - 없음 (컨트롤러에서 직접 처리)
- 엔티티
  - 없음 (User 엔티티 재사용)
- DTO
  - `LoginRequest.java`, `SignupRequest.java`, `TokenResponse.java`, `PasswordResetRequest.java`
- 레포지토리
  - 없음 (UserRepository 재사용)

#### 3.5.2 `domain/user`
- 컨트롤러
  - `UserController.java` : 사용자 정보 조회/수정, 회원 관련 API
    - `getMyPage()`: 마이페이지 정보 조회
    - `updateExtraInfo()`: 추가 정보 업데이트 (이메일, 전화번호, 비밀번호, 닉네임)
    - `checkNickname()`: 닉네임 중복 확인
    - `withdraw()`: 회원 탈퇴 (본인)
    - `adminWithdrawUser()`: 관리자용 회원 탈퇴
  - `WishlistController.java` : 찜 목록 API
  - `RecentViewController.java` : 최근 본 목록 API
- 서비스
  - `UserService.java` : 사용자 회원가입, 로그인, 프로필 관리, 비밀번호 재설정, 상태 변경 기능
    - `signUp()`: 이메일/닉네임/전화번호 중복 체크, 비밀번호 암호화, 사용자 생성
    - `verifyAndSendResetLink()`: 비밀번호 재설정 토큰 생성 및 이메일 발송
    - `resetPassword()`: 토큰 검증 후 새 비밀번호 설정
    - `login()`: 이메일/비밀번호 검증, 로그인 로그 기록, 사용자 정보 반환
    - `getUserInfo()`: 마이페이지용 사용자 정보 조회
    - `updateProfile()`: 닉네임/전화번호 중복 체크 후 업데이트
    - `updateUserStatus()`: 관리자용 사용자 상태 변경 (BANNED 등)
  - `MyPageService.java` : 마이페이지 정보 조회 서비스
    - `getMyPageInfo()`: 사용자 정보, 리뷰 수, 찜 수 조회하여 MyPageResponseDto 반환
  - `WishlistService.java` : 찜 목록 관리 서비스
    - `toggleWishlist()`: 찜 추가/해제 토글 기능
    - `getMyWishlist()`: 사용자의 찜 목록 조회
  - `RecentViewService.java` : 최근 본 콘텐츠 관리 서비스
    - `saveOrUpdateRecentView()`: 최근 본 콘텐츠 저장/업데이트 (UPSERT)
    - `cleanUpOldRecords()`: 최대 10개 제한으로 오래된 기록 삭제
    - `getRecentViews()`: 사용자의 최근 본 목록 조회
  - `MailService.java` : 이메일 발송 처리
- 엔티티
  - `User.java` : 사용자 정보 저장 엔티티
    - 필드: id, email, password, realName, phoneNumber, passwordResetToken, nickname, createdAt 등
    - 관계: reviews, loginLogs, reports, wishlists, recentViews (cascade 삭제)
    - enum: AuthProvider (LOCAL, KAKAO, GOOGLE), UserRole, UserStatus
    - 메서드: formatPhoneNumber(), increaseReportedCount(), updateLastLoginAt()
  - `Wishlist.java` : 찜 목록 엔티티
    - 필드: id, user, content
    - User와 Content의 다대다 관계를 위한 중간 테이블
  - `RecentView.java` : 최근 본 콘텐츠 엔티티
    - 필드: id, user, content, viewedAt
    - viewedAt: 최근 열람 시간 자동 기록
    - 메서드: updateViewedAt()
  - `LoginLog.java` : 로그인 로그 엔티티
    - 필드: id, user, loginAt, ipAddress, deviceInfo
    - 로그인 시 자동 기록
  - `ReviewReport.java` : 리뷰 신고 엔티티
    - 필드: id, reporter, review, reason, detailReason, createdAt
    - 신고자, 신고된 리뷰, 사유 저장
  - `ReportReason.java` : 신고 사유 enum
    - SPAM, INAPPROPRIATE_CONTENT, ABUSIVE_LANGUAGE, SPOILER, OTHER
  - `UserRole.java` : 사용자 역할 enum
    - ROLE_USER, ROLE_ADMIN
  - `UserStatus.java` : 사용자 상태 enum
    - ACTIVE, PENDING, BANNED, WITHDRAWN
- DTO
  - `UserResponse.java` : 사용자 응답 DTO
    - 필드: id, email, nickname, realName, phoneNumber, role
    - `from()`: User 엔티티에서 DTO 생성
  - `LoginResponse.java` : 로그인 응답 DTO
    - 필드: token, user (UserResponse)
  - `MyPageResponseDto.java` : 마이페이지 응답 DTO
    - 필드: id, email, realName, nickname, phoneNumber, reviewCount, wishlistCount
    - `from()`: User 엔티티와 카운트 값으로 생성
  - `UserLoginRequest.java` : 로그인 요청 DTO
    - 필드: email, password
  - `UserSignupRequest.java` : 회원가입 요청 DTO
    - 필드: email, password, nickname, realName, phoneNumber
  - `WishlistResponse.java` : 찜 목록 응답 DTO
    - 필드: wishlistId, contentId, title, posterPath
  - `RecentViewResponseDto.java` : 최근 본 목록 응답 DTO
    - 필드: id, contentId, title, posterPath, viewedAt, detailPath
    - 생성자: RecentView 엔티티에서 변환
  - `PasswordResetConfirmRequest.java` : 비밀번호 재설정 확인 요청 DTO
    - 필드: token, newPassword
  - `ContentCreateRequestDto.java` : 콘텐츠 생성 요청 DTO
    - 필드: tmdbId, cine21Id, title, overview, posterPath, ottLogos, cine21MovieId, genres, summary, positiveRatio
    - `toEntity()`: DTO에서 Content 엔티티로 변환
- 레포지토리
  - `UserRepository.java` : 사용자 데이터 접근
    - `findByEmail()`: 이메일로 사용자 찾기
    - `existsByEmail()`, `existsByNickname()`, `existsByPhoneNumber()`: 중복 확인
    - `findByEmailAndRealNameAndPhoneNumber()`: 비밀번호 찾기용 3중 검증
    - `findByPasswordResetToken()`: 토큰으로 사용자 찾기
    - `findByProviderAndProviderId()`: 소셜 로그인용
    - `findAllByReportedCountGreaterThanOrderByReportedCountDesc()`: 신고 수 많은 사용자 조회
    - `findAllUserStats()`: 관리자용 사용자 통계 조회 (찜, 리뷰 수 포함)
  - `WishlistRepository.java` : 찜 목록 데이터 접근
    - `deleteByUser()`: 사용자별 찜 목록 삭제
    - `countByUserId()`: 사용자별 찜 수 카운트
    - `findByUserAndContent()`: 특정 사용자와 콘텐츠의 찜 기록 조회
    - `findAllByUserId()`: 사용자별 전체 찜 목록 조회
  - `RecentViewRepository.java` : 최근 본 콘텐츠 데이터 접근
    - `findByUserOrderByViewedAtDesc()`: 사용자별 최근 본 목록 최신순 조회
    - `findTop1ByUserAndContentOrderByViewedAtDesc()`: 특정 콘텐츠의 최신 열람 기록 조회
    - `countByUser()`: 사용자별 기록 수 카운트
    - `findFirstByUserOrderByViewedAtAsc()`: 가장 오래된 기록 조회
  - `LoginLogRepository.java` : 로그인 로그 데이터 접근
    - `deleteByUser()`: 사용자별 로그인 로그 삭제
    - `findByUserOrderByLoginAtDesc()`: 사용자별 로그인 로그 최신순 조회
    - `findAllByOrderByLoginAt()`: 전체 로그인 로그 조회
  - `ReviewReportRepository.java` : 리뷰 신고 데이터 접근
    - `findAllByReviewIdWithReporter()`: 특정 리뷰의 모든 신고 내역 조회 (신고자 정보 포함)
    - `existsByReporterIdAndReviewId()`: 중복 신고 방지용 존재 확인

#### 3.5.3 `domain/content`
- 컨트롤러
  - `ContentController.java` : 콘텐츠 CRUD, 검색, 상세보기 API
    - `getMainPage()`: 메인페이지 데이터 조회
    - `getContentDetail()`: 상세페이지 데이터 조회, TMDB 동기화
    - `getAllExpertReviews()`: 전문가 리뷰 전체 조회
    - `saveExpertReview()`: 전문가 리뷰 저장
    - `searchSemantic()`: 의미 기반 검색 (Python 서버 호출)
    - `getRandomMovies()`: 랜덤 영화 조회
    - `getCategoryMovies()`: 장르 ID로 영화 조회
    - `getMoviesByGenreName()`: 장르 이름으로 영화 리스트
    - `getMoviesByOtt()`: OTT별 영화 리스트
    - `searchMovies()`: 하이브리드 검색
    - `getAllContents()`: 전체 영화 데이터 조회
  - `MainController.java` : 메인 화면용 콘텐츠 제공
  - `UserReviewController.java` : 사용자 리뷰 생성/삭제/조회
- 서비스
  - `ContentService.java` : 콘텐츠 관리, TMDB 연동, 상세 조회 기능
    - `updateContentWithTmdb()`: TMDB API로 콘텐츠 정보 업데이트 및 장르 동기화
    - `getContentDetail()`: 콘텐츠 상세 조회, TMDB 데이터 보완, 리뷰/추천 포함
    - `saveCrawledContent()`: 크롤링된 콘텐츠 DB 저장
  - `ReviewService.java` : 리뷰 관리 비즈니스 로직
    - `saveExpertReview()`: 전문가 리뷰 저장 (크롤링 데이터)
    - `getExpertReviews()`: 특정 콘텐츠의 전문가 리뷰 조회
    - `getUserReviews()`: 특정 콘텐츠의 사용자 리뷰 조회
    - `getMyReviews()`: 마이페이지용 내 리뷰 조회
    - `saveUserReview()`: 사용자 리뷰 저장 (중복 방지)
    - `getAllExpertReviews()`: 모든 전문가 리뷰 조회
    - `updateUserReview()`: 사용자 리뷰 수정 (본인 검증)
    - `deleteUserReview()`: 사용자 리뷰 삭제 (본인 또는 관리자 권한)
    - `deleteAllUserReviews()`: 특정 사용자의 모든 리뷰 삭제
    - `getReviewStats()`: 리뷰 통계 조회 (평균 평점, 전문가 리뷰 수)
    - `reportReview()`: 리뷰 신고 처리 (중복 방지, 카운트 증가)
  - `SearchService.java` : 하이브리드 검색 기능
    - `searchHybrid()`: 제목 검색 + AI 의미 검색 통합
  - `SemanticSearchService.java` : 의미 기반 검색 기능
    - `searchBySemantic()`: 코사인 유사도 기반 검색, 키워드/장르 매칭 가중치 적용
  - `TmdbService.java` : TMDB 외부 API 연동
    - `searchMovieIdByTitle()`: 영화 제목으로 TMDB ID 검색
    - `getMovieDetail()`: 영화 상세 정보 및 한국 심의 등급 가져오기
- 엔티티
  - `Content.java` : 콘텐츠 정보 저장 엔티티
    - 필드: id, tmdbId, cine21Id, title, overview, posterPath, ottLogos, backdropPath, runtime, ageRating, viewCount, wishCount
    - 관계: contentGenres, analysisCache, reviews
    - 메서드: incrementViewCount(), addGenre(), updateTmdbInfo(), updateFullInfo(), addReview()
  - `Genre.java` : 장르 엔티티
    - 필드: id, name
  - `ContentGenre.java` : 콘텐츠-장르 관계 엔티티
    - 필드: id, content, genre
    - Content와 Genre의 다대다 관계를 위한 중간 테이블
  - `ExpertReview.java` : 전문가 리뷰 엔티티
    - 필드: id, content, analysisCache, cine21Source, movieTitle, criticName, rating, comment, source, createdAt
    - 크롤링된 전문가 리뷰 데이터 저장
  - `UserReview.java` : 사용자 리뷰 엔티티
    - 필드: id, user, content, rating, comment, createdAt, updatedAt, reportCount, status, reports
    - 사용자 작성 리뷰, 신고 기능 포함
    - 메서드: increaseReportCount(), changeStatusByAdmin(), update(), setMapping()
  - `ReviewStatus.java` : 리뷰 상태 enum
    - NORMAL, HIDDEN
  - `AnalysisCache.java` : AI 분석 캐시 엔티티
    - 필드: id, summary, positiveRatio, topKeywords, content, embeddingVector, searchKeywords, expertReviews
    - AI 분석 결과와 벡터 데이터 저장
  - `Cine21Source.java` : 씨네21 크롤링 소스 엔티티
    - 필드: id, externalMovieId, rawTitle, crawlDate, expertReviews
    - 크롤링된 영화 소스 정보 저장
- DTO
  - `ContentDetailResponseDto.java` : 콘텐츠 상세 응답 DTO
    - 필드: id, title, overview, posterPath, ottLogos, genres, castList, backdropPath, runtime, ageRating, aiSummary, positiveRatio, topKeywords, expertReviews, userReviews, userRatingAvg, expertRatingAvg, expertReviewCount, trailerKey, recommendations
  - `ContentCreateRequestDto.java`
  - `ContentSearchRequest.java` : 콘텐츠 검색 요청 DTO
    - 필드: query, queryVector
  - `ContentSearchResponse.java` : 콘텐츠 검색 응답 DTO
    - 필드: contentId, title, posterPath, similaritySource, overview, genres, positiveRatio
  - `ContentSummaryDto.java` : 콘텐츠 요약 DTO
    - 필드: id, title, posterPath, positiveRatio, overview, genres
  - `AiAnalysisResponseDto.java` : AI 분석 응답 DTO
    - 필드: contentId, keywords, summary, positiveRatio, posterPath
  - `KeywordPageResponseDto.java` : 키워드 페이지 응답 DTO
    - 필드: id, title, posterPath, summary, topKeywords
  - `MainPageResponseDto.java` : 메인 페이지 응답 DTO
    - 필드: backgroundImage, todayRecommendations, genreContents
  - `MovieMainDto.java` : 메인 영화 DTO
    - 필드: id, title, posterPath, averageRating
  - `MyPageReviewResponseDto.java` : 마이페이지 리뷰 응답 DTO
    - 필드: reviewId, contentId, movieTitle, posterPath, rating, comment, createdAt, updatedAt
    - `from()`: UserReview 엔티티에서 DTO 생성
  - `ReviewCreateRequestDto.java` : 리뷰 생성 요청 DTO
    - 필드: contentId, externalMovieId, rawTitle, criticName, rating, comment
  - `ReviewResponseDto.java` : 리뷰 응답 DTO
    - 필드: id, criticName, rating, comment, sourceName, movieTitle
  - `ReviewReportRequestDto.java` : 리뷰 신고 요청 DTO
    - 필드: reason, detailReason
  - `ReviewStatsResponseDto.java` : 리뷰 통계 응답 DTO
    - 필드: userRatingAvg, expertRatingAvg, expertReviewCount
  - `TmdbMovieResponseDto.java` : TMDB 영화 응답 DTO
    - 필드: id, title, overview, poster_path, genres, credits, watchProviders, backdrop_path, runtime, ageRating, releaseDates, videos, trailerKey
    - 내부 클래스: TmdbGenre, TmdbCreditsResponse, TmdbCastItem, WatchProviders, CountryResult, ProviderDetail, TmdbReleaseDates, ReleaseDateResult, ReleaseDateDetail, TmdbVideoResponse, TmdbVideoItem
    - `getOttLogos()`: 한국 OTT 로고 경로들 반환
  - `UserReviewRequestDto.java` : 사용자 리뷰 요청 DTO
    - 필드: rating, comment
  - `UserReviewResponseDto.java` : 사용자 리뷰 응답 DTO
    - 필드: id, nickname, rating, comment, createdAt, updatedAt
    - `from()`: UserReview 엔티티에서 DTO 생성
- 레포지토리
  - `ContentRepository.java` : 콘텐츠 데이터 접근
    - `findByTmdbId()`: TMDB ID로 콘텐츠 찾기
    - `findWithCacheById()`: AI 분석 캐시와 함께 콘텐츠 조회
    - `findTop10ByOrderByIdDesc()`: 최신 등록순 10개
    - `findByGenreId()`: 장르 ID로 콘텐츠 필터링
    - `findRandomContents()`: 랜덤 콘텐츠 조회
    - `findRandomByGenreId()`: 장르 내 랜덤 콘텐츠
    - `findByOttLogosContaining()`: OTT 로고로 콘텐츠 찾기
    - `findByTitleContaining()`: 제목으로 검색
    - `findBySemanticSearch()`: AI 벡터 유사도 검색
    - `findSimilarContentByVector()`: 유사 콘텐츠 추천
  - `GenreRepository.java` : 장르 데이터 접근
    - `findByName()`: 장르 이름으로 조회
  - `ContentGenreRepository.java` : 콘텐츠-장르 관계 데이터 접근
    - `findByGenre()`: 장르로 콘텐츠-장르 관계 조회
    - `findAllByContentIdIn()`: 여러 콘텐츠 ID로 관계 조회
  - `ExpertReviewRepository.java` : 전문가 리뷰 데이터 접근
    - `findByContentId()`: 콘텐츠별 전문가 리뷰 조회
    - `getAverageRatingByContentId()`: 콘텐츠별 평균 평점 계산
    - `countByContentId()`: 콘텐츠별 리뷰 수 카운트
  - `UserReviewRepository.java` : 사용자 리뷰 데이터 접근
    - `findByContentId()`: 콘텐츠별 사용자 리뷰 조회
    - `findByUserId()`: 사용자별 리뷰 조회
    - `countByUserId()`: 사용자별 리뷰 수 카운트
    - `existsByUserIdAndContentId()`: 중복 리뷰 방지용 존재 확인
    - `deleteAllByUserId()`: 사용자별 리뷰 전체 삭제
    - `deleteByUser()`: 사용자별 리뷰 삭제
    - `findAllByContentIdAndStatus()`: 상태별 리뷰 조회
    - `getAverageRatingByContentId()`: 콘텐츠별 평균 평점 계산
    - `findAllByReportCountGreaterThanOrderByReportCountDesc()`: 신고 수 많은 리뷰 조회
  - `AnalysisCacheRepository.java` : AI 분석 캐시 데이터 접근
    - `findAll()`: 모든 분석 캐시 조회 (Content 조인)
    - `findByContentId()`: 콘텐츠별 분석 캐시 조회
    - `findAllByContentIdIn()`: 여러 콘텐츠별 분석 캐시 조회
  - `Cine21SourceRepository.java` : 씨네21 소스 데이터 접근
    - `findByExternalMovieId()`: 외부 영화 ID로 소스 조회
    - `findByRawTitle()`: 원본 제목으로 소스 조회

#### 3.5.4 `domain/ai`
- 컨트롤러
  - `AiController.java` : AI 챗봇 관련 API
    - `chat()`: 챗봇 채팅 요청 처리
    - `getAllAnalysisData()`: 모든 영화 분석 데이터 조회
    - `getMovieAnalysis()`: 특정 영화 분석 데이터 조회
- 서비스
  - `AiService.java` : AI 요청 및 답변 관리, 분석 데이터 제공 기능
    - `getAllAnalysisData()`: 모든 영화 분석 데이터 리스트 반환
    - `getMovieAnalysis()`: 특정 영화 분석 데이터 조회
    - `processChat()`: 챗봇 채팅 처리, Python AI 서버 호출
    - `callPythonAiServer()`: Python AI 서버에 요청 전송
    - `getOrCreateSession()`: 채팅 세션 생성/조회
    - `saveChatMessage()`: 채팅 메시지 DB 저장
- 엔티티
  - `ChatSession.java` : 채팅 세션 정보 저장
    - 필드: id, sessionId, createdAt, updatedAt
    - 관계: chatMessages
  - `ChatMessage.java` : 채팅 메시지 저장
    - 필드: id, sessionId, role, content, createdAt
- DTO
  - `ChatRequest.java` : 챗봇 요청 DTO
    - 필드: prompt, sessionId
  - `ChatResponse.java` : 챗봇 응답 DTO
- 레포지토리
  - `ChatSessionRepository.java` : 채팅 세션 데이터 접근
  - `ChatMessageRepository.java` : 채팅 메시지 데이터 접근

#### 3.5.5 `domain/admin`
- 컨트롤러
  - `AdminController.java` : 관리자용 API 엔드포인트
    - `getAllUsers()`: 전체 사용자 목록 조회 (페이징, 최신순)
    - `getReportedUsers()`: 상습 신고 사용자 조회
    - `updateUsersStatus()`: 사용자 상태 변경 (ACTIVE, BANNED 등)
    - `deleteUser()`: 사용자 영구 삭제
    - `adminWithdrawUser()`: 관리자용 사용자 탈퇴 처리
    - `getReportedReviews()`: 신고된 리뷰 조회
    - `updateReviewStatus()`: 리뷰 상태 변경 (APPROVED, REJECTED 등)
    - `deleteReview()`: 리뷰 삭제
    - `getContentStats()`: 콘텐츠 통계 조회
    - `getReviewReportDetails()`: 리뷰 신고 상세 조회
    - `getLoginLogs()`: 로그인 로그 조회
- 서비스
  - `AdminService.java` : 관리자 비즈니스 로직
    - `getReportedReviews()`: 신고된 리뷰 조회 (신고 수 기준 정렬)
    - `getReviewReportDetails()`: 리뷰 신고 상세 조회 (신고자 정보 포함)
    - `getReportedUsers()`: 상습 신고 사용자 조회
    - `getAllUsers()`: 전체 사용자 목록 조회 (페이징)
    - `getLoginLogs()`: 로그인 로그 조회
    - `getContentStats()`: 콘텐츠 통계 조회 (조회수, 찜 수)
    - `updateReviewStatus()`: 리뷰 상태 변경 (관리자)
    - `deleteReview()`: 리뷰 삭제 (관리자)
    - `updateUserStatus()`: 사용자 상태 변경 (관리자)
    - `deleteUser()`: 사용자 영구 삭제
    - `withdrawUser()`: 사용자 탈퇴 처리 (관리자)
- 엔티티
  - 없음 (다른 도메인 엔티티 재사용)
- DTO
  - `AdminUserResponseDto.java` : 관리자용 사용자 응답 DTO
  - `AdminReviewResponseDto.java` : 관리자용 리뷰 응답 DTO
  - `AdminContentStateResponseDto.java` : 콘텐츠 통계 응답 DTO
  - `AdminReviewReportResponseDto.java` : 리뷰 신고 상세 응답 DTO
  - `LoginLogResponseDto.java` : 로그인 로그 응답 DTO
- 레포지토리
  - 없음 (다른 도메인 레포지토리 재사용)

#### 3.5.6 `domain/common`
- Validator
  - `StatusValidator.java` : 상태 검증 유틸

### 3.6 글로벌 보안 및 예외
- `src/main/java/com/Connectedm/backend/global/security/CustomUserDetails.java`
- `src/main/java/com/Connectedm/backend/global/security/CustomUserDetailsService.java`
- `global/auth`, `global/common`, `global/config`, `global/error`, `global/utils`
  - 공통 응답/예외 처리, 인증/인가 설정, 전역 유틸리티

### 3.7 인프라
- `src/main/java/com/Connectedm/backend/infra/ai/`
  - AI 외부 연동, Gemini 또는 기타 외부 API 호출 로직

### 3.8 Python AI 챗봇 서버
#### ChatBot 디렉터리
- `ChatBot/main.py`
  - FastAPI 서버 진입점
  - `GET /` : 정상 작동 확인
  - `POST /api/chat/session` : 채팅 세션 생성
  - `POST /api/chat` : 챗봇 질의응답 처리
  - CORS를 `allow_origins=['*']`로 설정
- `ChatBot/database.py`
  - SQLAlchemy DB 연결과 세션 제공
- `ChatBot/models.py`
  - DB 모델 및 Pydantic 스키마
- `ChatBot/chatbot_logic.py`
  - Gemini API 연동 및 AI 답변 생성
- `ChatBot/check_models.py`
  - 모델 검증 및 점검 스크립트
- `ChatBot/requirements.txt`
  - Python 패키지 목록

#### ChatBot 동작 흐름
1. 프론트엔드/백엔드에서 챗봇 요청을 받으면 `main.py`가 FastAPI로 처리
2. `chatbot_logic.get_gemini_response()`를 호출하여 Gemini AI 응답 생성
3. 채팅 이력과 질문을 DB에 저장
4. `ChatBot`은 `backend` 메인 서버가 호출하는 내부 AI 서비스로 동작

### 3.9 데이터 크롤링/전처리
#### data_crawling 디렉터리
- `main.py`
  - 전체 워크플로우: 크롤링 → DB 저장 → AI 분석
  - `save_data_and_crawling()` : 사이트 리뷰 수집, TMDB 장르 조회, DB 적재
  - `run_all_analysis()` : 저장 데이터에 Gemini 분석 수행
- `scraper.py`
  - 영화 리뷰/데이터 수집용 웹 크롤러
- `movie_data.py`
  - 수집 대상 영화 목록 및 메타데이터 정의
- `expert_saver.py`
  - 수집 결과를 MariaDB에 저장하는 클래스
- `embedding_engine.py`
  - 리뷰 텍스트를 벡터로 변환하는 임베딩 엔진
- `gemini_analysis.py`
  - AI 분석 호출 로직
- `db_filler.py`, `db_fillter.py`
  - DB 채우기 및 데이터 관리 도구
- `.env`, `.env.example`
  - 환경 변수 설정 파일
- `requirements.txt`
  - 파이썬 의존성 목록

#### data_crawling 동작 흐름
1. `MovieScraper`로 웹에서 리뷰 수집
2. 수집한 텍스트를 `MeaningVectorEngine`으로 벡터 변환
3. `ExpertSaver`가 MariaDB에 영화, 리뷰, 장르, 벡터 저장
4. `gemini_analysis.py`가 저장된 영화 데이터를 AI로 분석

## 4. 프론트엔드 ↔ 백엔드 연결 관계

### 4.1 인증/로그인
- `frontend/src/components/common/LoginModal.tsx`는 이메일/비밀번호 로그인, 회원가입, 비밀번호 재설정, 소셜 로그인 버튼을 처리합니다.
- 로그인 요청은 `/api/auth/login`에 `UserLoginRequest` 형태로 POST되어 `backend/src/main/java/com/Connectedm/backend/domain/auth/AuthController.login()`에 도달합니다.
- `AuthController.login()`은 `UserService.login()`을 호출하여 `UserRepository.findByEmail()`로 사용자 조회, `PasswordEncoder`로 비밀번호 검증, JWT를 생성하고 `LoginResponse`를 반환합니다.
- 회원가입은 `/api/auth/signup`에 `UserSignupRequest`로 요청되어 `UserService.signUp()`이 `UserRepository.existsByEmail()`/`existsByNickname()`/`existsByPhoneNumber()` 중복 검사를 하고 `User` 엔티티를 저장합니다.
- 비밀번호 재설정은 `/api/auth/reset-password`에 `PasswordResetRequest`를 전달하고 `UserService.verifyAndSendResetLink()`가 이메일 토큰 생성 및 `MailService` 발송을 수행합니다.
- 소셜 로그인은 `LoginModal.tsx`의 `handleSocialLogin()`이 `AuthController.oauth2Login()` URL로 리디렉션하고, 콜백은 `/oauth2/redirect`에서 `frontend/src/pages/Auth/OAuth2RedirectHandler.tsx`가 처리합니다.
- `frontend/src/hooks/useAuthCheck.tsx`는 OAuth2 콜백 쿼리를 파싱하고 `token`, `nickname`, `needInfo` 값을 로컬스토리지에 저장하거나 `/extra-info`로 이동시킵니다.
- 백엔드 `backend/src/main/java/com/Connectedm/backend/config/SecurityConfig.java`는 JWT 필터와 OAuth2 로그인 설정을 정의하며 `CustomUserDetailsService`에서 DB 사용자 조회를 수행합니다.

### 4.2 콘텐츠/검색
- `frontend/src/pages/Home/Home.tsx`, `GenrePage.tsx`, `OttPage.tsx`, `SearchResult.tsx`, `KeywordPage.tsx`, `MovieDetail/Moviedetail.tsx`는 모두 `backend`의 `domain/content` API를 호출합니다.
- `Home.tsx`는 `GET /api/contents/random`과 `GET /api/contents/category?genreId=`를 호출하여 `ContentController.getRandomMovies()`와 `ContentController.getCategoryMovies()`를 사용합니다.
- `SearchResult.tsx`는 검색어 `q`로 `/api/contents/search?query=`를 호출하고 `ContentController.searchMovies()`가 `SearchService.searchHybrid()`를 통해 title 검색 결과와 AI 의미 검색을 통합합니다.
- 의미 검색이 활성화되면 `ContentController.searchSemantic()`이 `SemanticSearchService.searchBySemantic()`를 호출하여 `AnalysisCacheRepository.findByContentId()`와 `ContentRepository.findBySemanticSearch()`를 사용합니다.
- `GenrePage.tsx`는 `GET /api/contents/genre/{genreName}` 또는 `GET /api/contents/genre?name=` 형태로 `ContentController.getMoviesByGenreName()`를 사용하여 `GenreRepository.findByName()`과 `ContentGenreRepository.findByGenre()`를 조회합니다.
- `OttPage.tsx`는 `GET /api/contents/ott/{providerName}`로 `ContentController.getMoviesByOtt()`를 호출하고 `ContentRepository.findByOttLogosContaining()`을 통해 OTT별 영화 리스트를 가져옵니다.
- `MovieDetail/Moviedetail.tsx`는 `GET /api/contents/{id}`를 호출하여 `ContentController.getContentDetail()`과 `ContentService.getContentDetail()`이 `ContentRepository.findWithCacheById()`, `AnalysisCacheRepository.findByContentId()`, `ExpertReviewRepository.findByContentId()`, `UserReviewRepository.findByContentId()`를 사용합니다.
- `ContentService.updateContentWithTmdb()`는 필요 시 `TmdbService.getMovieDetail()`을 호출하여 TMDB 외부 데이터를 보강하고 `Content.updateTmdbInfo()`를 저장합니다.

### 4.3 주요 백엔드 엔드포인트 매핑
- `frontend/src/components/common/LoginModal.tsx`
  - 로그인: POST `/api/auth/login` → `domain/auth/AuthController.login()` → `UserService.login()` → `UserRepository.findByEmail()` → `LoginResponse`
  - 회원가입: POST `/api/auth/signup` → `AuthController.signup()` → `UserService.signUp()` → `UserRepository.save()`
  - 비밀번호 재설정: POST `/api/auth/reset-password` → `AuthController.resetPassword()` → `UserService.verifyAndSendResetLink()` → `MailService.sendResetEmail()`
  - 소셜 로그인: GET `/api/auth/oauth2/authorize/{provider}` → `AuthController.oauth2Login()`, 콜백 `AuthController.oauth2Callback()` → OAuth2 provider 토큰 처리 및 JWT 발급
- `frontend/src/pages/Auth/OAuth2RedirectHandler.tsx`
  - OAuth2 콜백 결과 처리 후 `frontend/src/hooks/useAuthCheck.tsx`가 `token`, `nickname`, `needInfo`를 localStorage에 저장하고 `navigate('/extra-info')` 또는 `/` 이동을 결정합니다.
- `frontend/src/components/layout/Header.tsx`
  - 검색어 제출 시 `navigate('/search?q=' + searchTerm)`
  - 장르 버튼 클릭 시 `navigate('/genre/' + genreName)` → `GenrePage.tsx`
  - OTT 버튼 클릭 시 `navigate('/ott/' + providerName)` → `OttPage.tsx`
  - 로그인 버튼/마이페이지 버튼은 `localStorage.token`을 확인하여 모달 표시 또는 `/mypage` 이동
- `frontend/src/pages/Home/Home.tsx`
  - 랜덤 추천: `GET /api/contents/random` → `ContentController.getRandomMovies()` → `ContentService.getRandomMovies()` → `ContentRepository.findRandomContents()`
  - 장르별 섹션: `GET /api/contents/category?genreId=` → `ContentController.getCategoryMovies()` → `ContentRepository.findByGenreId()`
- `frontend/src/pages/SearchResult/SearchResult.tsx`
  - 키워드 검색: `GET /api/contents/search?query=` → `ContentController.searchMovies()` → `SearchService.searchHybrid()`
  - 페이지네이션과 필터링은 프론트엔드에서 `results` 상태를 관리하고 `ContentSearchResponse` DTO 목록을 렌더링
- `frontend/src/pages/MovieDetail/Moviedetail.tsx`
  - 상세 페이지: `GET /api/contents/{id}` → `ContentController.getContentDetail()` → `ContentService.getContentDetail()` → `ContentDetailResponseDto`
  - 찜: POST/DELETE `/api/members/wishlist/{id}` → `WishlistController.toggleWishlist()` → `WishlistService.toggleWishlist()` → `WishlistRepository.findByUserAndContent()` / `save()` / `delete()`
  - 최근 본: POST `/api/users/recent/{id}` → `RecentViewController.saveOrUpdateRecentView()` → `RecentViewService.saveOrUpdateRecentView()` → `RecentViewRepository.findTop1ByUserAndContentOrderByViewedAtDesc()`
  - 리뷰: POST/PUT/DELETE `/api/contents/user-reviews` → `UserReviewController` → `ReviewService.saveUserReview()` / `updateUserReview()` / `deleteUserReview()`
- `frontend/src/components/chatbot/Chatbot.tsx`
  - 챗봇 요청: POST `/api/ai/recommend` 또는 `/api/ai/chat` → `AiController.chat()` → `AiService.processChat()` → `AiService.callPythonAiServer()` → `ChatBot/main.py`
  - 서버 응답은 `ChatResponse` 형태로 받아 프론트엔드 `messages` 리스트에 추가
- `frontend/src/pages/MyPage/MyPage.tsx` 및 하위 페이지
  - `GET /api/users/mypage` → `UserController.getMyPage()` → `MyPageService.getMyPageInfo()` → `MyPageResponseDto`
  - 프로필 업데이트: PUT `/api/users/extra-info` → `UserController.updateExtraInfo()` → `UserService.updateProfile()`
  - 찜 목록 조회: GET `/api/members/wishlist` → `WishlistController.getMyWishlist()` → `WishlistService.getMyWishlist()` → `WishlistResponse`
  - 최근 본 조회: GET `/api/users/recent` → `RecentViewController.getRecentViews()` → `RecentViewService.getRecentViews()` → `RecentViewResponseDto`
  - 내 리뷰 조회: GET `/api/contents/user-reviews/my` → `UserReviewController.getMyReviews()` → `ReviewService.getMyReviews()`
- `frontend/src/pages/admin/adminpage.tsx`
  - 사용자 목록: GET `/api/admin/users` → `AdminController.getAllUsers()` → `AdminService.getAllUsers()`
  - 신고 사용자: GET `/api/admin/reported-users` → `AdminController.getReportedUsers()` → `AdminService.getReportedUsers()`
  - 리뷰 신고 목록: GET `/api/admin/reported-reviews` → `AdminController.getReportedReviews()` → `AdminService.getReportedReviews()`
  - 상태 변경: PUT `/api/admin/users/status` 또는 `/api/admin/reviews/status` → `AdminController.updateUsersStatus()` / `updateReviewStatus()`

### 4.4 백엔드 도메인 연결
- 인증 도메인
  - `backend/src/main/java/com/Connectedm/backend/domain/auth/AuthController.java`는 `/api/auth/**` 경로를 처리합니다.
  - `AuthController`는 `UserService`, `MailService`, `JwtTokenProvider`를 사용합니다.
  - `UserService`는 `UserRepository`, `PasswordEncoder`, `MailService`, `JwtTokenProvider`를 함께 호출하여 사용자 생성/인증/토큰 발급/비밀번호 재설정 로직을 완성합니다.
  - `backend/src/main/java/com/Connectedm/backend/global/security/CustomUserDetailsService.java`는 JWT기반 사용자 인증을 위해 `UserRepository.findByEmail()`을 사용합니다.
- 사용자 도메인
  - `backend/src/main/java/com/Connectedm/backend/domain/user/UserController.java`는 마이페이지 정보, 추가 정보 수정, 닉네임 확인, 회원 탈퇴 경로를 제공합니다.
  - `WishlistController.java`는 `WishlistRepository`와 `WishlistService`를 통해 `User`/`Content` 관계를 관리합니다.
  - `RecentViewController.java`는 `RecentViewRepository`를 사용하여 최근 본 콘텐츠 저장과 조회를 수행합니다.
  - `UserService`가 `UserRepository`를 통해 `findByEmail()`/`existsByNickname()`/`findByPasswordResetToken()`을 사용합니다.
- 콘텐츠 도메인
  - `ContentController.java`와 `MainController.java`는 `ContentService`와 `TmdbService`를 호출하여 콘텐츠 리스트와 상세 정보를 제공합니다.
  - `ContentService.getContentDetail()`은 `ContentRepository.findWithCacheById()`로 콘텐츠를 불러오고 `AnalysisCacheRepository.findByContentId()`로 AI 분석 데이터를 결합합니다.
  - `SearchService.searchHybrid()`는 `ContentRepository.findByTitleContaining()`과 `SemanticSearchService.searchBySemantic()` 결과를 병합합니다.
  - `SemanticSearchService.searchBySemantic()`은 `AnalysisCacheRepository.findAllByContentIdIn()` 또는 `ContentRepository.findBySemanticSearch()`를 사용하여 의미 유사도 기반 콘텐츠를 필터링합니다.
  - `TmdbService.getMovieDetail()`은 TMDB API에서 `poster_path`, `watchProviders`, `videos`, `releaseDates`를 받아 `TmdbMovieResponseDto`로 매핑하고 `Content.updateTmdbInfo()`를 호출합니다.
- 리뷰 도메인
  - `UserReviewController.java`는 사용자 리뷰 CRUD 요청을 받고 `ReviewService`로 위임합니다.
  - `ReviewService.saveUserReview()`는 `UserReviewRepository.existsByUserIdAndContentId()`로 중복 리뷰를 검사하고, 새로운 `UserReview`를 저장합니다.
  - `ReviewService.reportReview()`는 `ReviewReportRepository.existsByReporterIdAndReviewId()`로 신고 중복을 방지하고 `ReviewReport` 엔티티를 생성합니다.
- AI 도메인
  - `AiController.java`는 `/api/ai/recommend` 또는 `/api/ai/chat` 요청을 처리합니다.
  - `AiService.processChat()`은 `ChatSessionRepository`와 `ChatMessageRepository`를 사용하여 `ChatSession`과 `ChatMessage`를 생성/조회합니다.
  - `AiService.callPythonAiServer()`는 `backend/src/main/resources/application.properties`의 `external-api.python-ai-url`에 POST 요청을 보내 `ChatBot/main.py`의 `/api/chat`을 호출합니다.
- 관리자 도메인
  - `AdminController.java`는 `/api/admin/**` 환경을 관리합니다.
  - `AdminService`는 `UserRepository`, `UserReviewRepository`, `LoginLogRepository`, `ContentRepository`, `ReviewReportRepository`를 사용하여 통계와 신고 관리 기능을 구현합니다.
- 공통 도메인
  - `domain/common/StatusValidator.java`와 `global/error`는 요청 검증과 예외 처리 로직을 공유합니다.

### 4.5 마이페이지
- 프론트엔드 `frontend/src/pages/MyPage/MyPage.tsx`는 `UserController.getMyPage()`와 `WishlistController.getMyWishlist()`, `RecentViewController.getRecentViews()`를 호출합니다.
- `EditProfile.tsx`는 `/api/users/extra-info`로 PUT 요청을 보내 `UserService.updateProfile()`를 실행합니다.
- `WishlistPage.tsx`와 `RecentPage.tsx`는 각각 `WishlistResponse`와 `RecentViewResponseDto`를 받아 UI로 렌더링합니다.
- `MyReviewsPage.tsx`는 `UserReviewController.getMyReviews()`로 `MyPageReviewResponseDto` 배열을 받아 리뷰 목록을 표시합니다.

### 4.6 AI 챗봇
- `frontend/src/components/chatbot/Chatbot.tsx`는 사용자 입력을 `ChatRequest` 형식으로 변환하여 `POST /api/ai/recommend`에 전송합니다.
- `backend/domain/ai/AiController.chat()`는 `AiService.processChat()`을 호출합니다.
- `AiService.processChat()`은 세션이 없으면 `getOrCreateSession()`으로 `ChatSession`을 생성하고, `ChatMessage`를 저장한 뒤 `callPythonAiServer()`로 AI 응답을 가져옵니다.
- Python `backend/ChatBot/main.py`는 `/api/chat/session`에서 세션을 만들고 `/api/chat`에서 Gemini API 기반 답변을 생성합니다.
- 프론트엔드는 `ChatResponse`를 받아 챗봇 메시지를 `messages` 배열에 추가하고 `Chatbot.tsx`에서 화면에 표시합니다.

### 4.7 관리자 기능
- `frontend/src/pages/admin/adminpage.tsx`는 관리자 UI를 렌더링하고 `/api/admin/*`를 호출합니다.
- `AdminController.getAllUsers()`는 `AdminService.getAllUsers()`를 호출하여 사용자 목록과 통계 데이터를 제공합니다.
- `AdminController.getReportedUsers()`는 `AdminService.getReportedUsers()`로 상습 신고 사용자를 조회합니다.
- `AdminController.getReportedReviews()`는 `AdminService.getReportedReviews()`로 신고된 리뷰와 신고자 정보를 반환합니다.
- `AdminController.updateUsersStatus()` 및 `updateReviewStatus()`는 `AdminService.updateUserStatus()` / `updateReviewStatus()`를 통해 사용자 상태 변경과 리뷰 처리 상태를 저장합니다.
- `AdminService.deleteUser()`는 `UserRepository.deleteById()`를 호출하고, 필요 시 `UserReviewRepository.deleteAllByUserId()`와 `WishlistRepository.deleteByUser()`를 함께 실행합니다.

## 5. 개발/운영 참고
- 백엔드 메인 서버는 `application.properties`에 DB 및 OAuth 설정이 집중됨
- Python AI 서버와 데이터 수집 모듈은 각각 독립 실행 가능
- `backend/ChatBot`과 `backend/data_crawling`의 `.venv`는 Python 가상환경이며, 일반적으로 배포 시 제외할 수 있음
- 프론트엔드는 Vite를 사용하며 `npm run dev`로 빠른 개발 서버 실행 가능

## 6. 요약
- 프론트엔드: React + TypeScript + Vite, 라우팅과 UI 컴포넌트 중심
- 백엔드: Spring Boot + MariaDB, 도메인별 패키지로 사용자/콘텐츠/AI/관리자 기능 분리
- AI: FastAPI 챗봇 (`ChatBot`)과 데이터 수집/분석 (`data_crawling`) 두 모듈로 구성
- 핵심 연결: 프론트엔드 → 메인 백엔드 API → AI 챗봇/데이터 수집

---

이 파일은 `frontend`와 `backend`를 모두 포함하는 전체 프로젝트의 자세한 한글 설명입니다.
