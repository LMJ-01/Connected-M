# Connected_M - 영화 탐색 및 분석 플랫폼

Connected_M은 다채로운 영화 정보와 개인 맞춤형 추천을 제공하는 종합 플랫폼입니다. TMDB와 씨네21(Cine21)의 데이터를 통합하여 사용자에게 깊이 있는 통찰력과 AI 기반의 도움, 그리고 원활한 영화 탐색 경험을 제공합니다.

---

## 주요 기능

### 영화 탐색 및 상세 정보
- **데이터 통합:** TMDB의 대중적인 영화 데이터와 씨네21의 전문적인 분석 데이터를 결합하여 제공합니다.
- **상세 정보:** 줄거리, 출연진/제작진, 장르 및 전문가 분석 내용을 확인할 수 있습니다.
- **랭킹 및 키워드:** 주간 랭킹과 트렌드 키워드를 통해 영화를 발견할 수 있습니다.
- **검색:** 제목이나 관련 키워드로 영화를 빠르게 검색할 수 있습니다.

### 사용자 경험
- **스마트 인증:** 카카오, 구글 소셜 로그인(OAuth2) 또는 일반 이메일 인증(JWT 토큰 기반)을 지원합니다.
- **개인화 프로필:** 마이페이지를 통해 사용자 정보와 저장한 콘텐츠를 관리할 수 있습니다.
- **계정 관리:** 회원가입, 추가 정보 입력, 이메일을 통한 비밀번호 재설정 기능을 제공합니다.

### AI 기반 기능
- **AI 챗봇:** 영화 관련 문의 및 개인 맞춤형 추천을 돕는 통합 챗봇을 제공합니다.
- **콘텐츠 분석:** 사용자에게 더 나은 통찰력을 제공하기 위한 영화 데이터의 심층 분석을 수행합니다.

---

## 🛠 기술 스택

### [Backend]
- **Main Server:** Java 17, Spring Boot 3.2.4
- **Security:** Spring Security, OAuth2 (Kakao/Google), JWT
- **Database:** MariaDB (JPA/Hibernate)
- **API Documentation:** Swagger UI (SpringDoc 2.3.0)
- **AI Framework:** LangChain, Google Gemini API
- **AI API Server:** FastAPI (Python)
- **Data Scraping:** Selenium, BeautifulSoup4, Sentence Transformers (Embedding)

### [Frontend]
- **Core:** React 18, TypeScript, Vite
- **Routing:** React Router DOM (v7)
- **Styling:** Vanilla CSS (모듈형 설계)
- **Data Visualization:** Recharts
- **HTTP Client:** Axios

---

## 📂 프로젝트 구조

### 🔹 [Backend](backend/README.md)
```text
backend/
├── src/main/java/com/Connectedm/backend/
│   ├── config/      # 보안, OAuth2, JWT, Swagger 등 설정
│   ├── domain/      # 도메인별 엔티티, 리포지토리, 서비스, 컨트롤러
│   ├── global/      # 공통 예외 처리, 응답 포맷, 유틸리티
│   └── infra/       # 외부 API 연동 및 인프라 관련 설정
├── ChatBot/         # FastAPI 기반 AI 챗봇 서비스 (LangChain, Gemini 연동)
└── data_crawling/   # 데이터 수집 및 전처리 (Selenium, Embedding)
```

### 🔹 [Frontend](frontend/README.md)
```text
frontend/
├── src/
│   ├── api/        # TMDB 및 백엔드 서버 통신 로직
│   ├── components/ # UI 컴포넌트 (Chatbot, Layout, Common)
│   ├── hooks/      # 커스텀 훅 (인증 등)
│   ├── pages/      # 페이지 컴포넌트 (Home, MovieDetail, Admin 등)
│   └── types/      # TypeScript 타입 정의
```

---

## ⚙️ 설치 및 설정

### 사전 요구 사항
- JDK 17, Node.js (v18+), MariaDB, Python 3.x

### 1. 백엔드 설정 (Java & Python)
**Java 서버 실행:**
```bash
cd backend
# application.properties 설정 후
./gradlew bootRun
```
**AI 챗봇 실행:**
```bash
cd backend/ChatBot
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### 2. 프론트엔드 설정 (React)
```bash
cd frontend
npm install
npm run dev
```

---

## 👥 팀원 (Contributors)

| 이름 | 역할 |
| --- | --- |
| 이명준 | 총괄 |
| 이건용 | 백엔드, 프론트엔드 |
| 이승우 | 데이터크롤링 |
| 조우진 | UI 구현,프론트엔드 |
| 한규대 | 데이터베이스 |

---

## 📝 라이선스
본 프로젝트는 교육 및 포트폴리오 목적으로 제작되었습니다. 영화 데이터는 TMDB와 씨네21에서 제공받았습니다.
