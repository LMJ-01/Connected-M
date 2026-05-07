# Connected_M Backend

Connected_M의 백엔드는 Spring Boot 기반의 메인 서버와 Python 기반의 AI 및 데이터 수집 모듈로 구성되어 있습니다.

## 🏗 프로젝트 구조

```text
backend/
├── src/main/java/com/Connectedm/backend/
│   ├── config/      # 보안, OAuth2, JWT, Swagger 등 설정
│   ├── domain/      # 도메인별 엔티티, 리포지토리, 서비스, 컨트롤러
│   ├── global/      # 공통 예외 처리, 응답 포맷, 유틸리티
│   └── infra/       # 외부 API 연동 및 인프라 관련 설정
├── ChatBot/         # FastAPI 기반 AI 챗봇 서비스
│   ├── main.py      # FastAPI 진입점
│   ├── chatbot_logic.py # LangChain 및 Gemini 연동 로직
│   └── requirements.txt # Python 의존성
└── data_crawling/   # 데이터 수집 및 전처리 스크립트
    ├── scraper.py   # Selenium/BS4 기반 크롤러
    ├── embedding_engine.py # Sentence Transformers 기반 데이터 임베딩
    └── requirements.txt # Python 의존성
```

## 🛠 기술 스택

### Main Server
- **Framework:** Spring Boot 3.2.4
- **Language:** Java 17
- **Database:** MariaDB (JPA/Hibernate)
- **Security:** Spring Security, OAuth2 (Google/Kakao), JWT
- **Documentation:** Swagger (SpringDoc 2.3.0)
- **Communication:** WebClient (WebFlux) for async API calls

### AI & Data
- **AI Framework:** LangChain, Google Gemini API
- **API Framework:** FastAPI (ChatBot)
- **Data Scraping:** Selenium, BeautifulSoup4
- **NLP:** Sentence Transformers (Embedding)

## ⚙️ 시작하기

### 1. Java 서버 실행
1. `backend/src/main/resources/application.properties` 파일에 DB 정보 및 API 키를 설정합니다.
2. 프로젝트 루트 또는 `backend/` 폴더에서 실행:
   ```bash
   ./gradlew bootRun
   ```

### 2. Python 챗봇 실행
1. `backend/ChatBot/` 폴더로 이동합니다.
2. 가상환경 생성 및 의존성 설치:
   ```bash
   python -m venv venv
   source venv/bin/activate  # Windows: venv\Scripts\activate
   pip install -r requirements.txt
   ```
3. 서버 실행:
   ```bash
   uvicorn main:app --reload --port 8000
   ```

### 3. 데이터 수집 스크립트 실행
1. `backend/data_crawling/` 폴더로 이동합니다.
2. 가상환경 설정 및 의존성 설치 (챗봇과 별도로 관리 권장).
3. 스크립트 실행:
   ```bash
   python main.py
   ```

## 🔗 주요 API 경로
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **ChatBot API:** `http://localhost:8000`
