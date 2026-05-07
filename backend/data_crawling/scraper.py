'''
[ 웹 크롤링 및 데이터 추출 도구 세트 ]

1. 기본 유틸리티:
   - time: 브라우저가 로딩될 때까지 기다리거나, 너무 빠른 접속으로 차단당하지 않게 '잠깐 멈춤'을 줍니다.
   - re: '정규 표현식'입니다. 텍스트 속에서 숫자만 뽑거나 특정 패턴의 문자열을 찾을 때 사용합니다.

2. Selenium (브라우저 자동화 도구):
   - webdriver: 실제 크롬 창을 띄우고 조종하는 '조종사' 역할을 합니다.
   - Service: 크롬 드라이버 실행 파일의 경로와 시작을 관리합니다.
   - Options: 브라우저를 눈에 안 보이게 실행(Headless)하거나, 리소스를 최적화하는 '설정값'입니다.
   - By: 요소를 찾을 때 "ID로 찾을지, 클래스 이름으로 찾을지" 결정하는 '탐색 기준'입니다.

3. 동적 로딩 대응 (Wait):
   - WebDriverWait & expected_conditions (EC): 
     웹페이지는 인터넷 속도에 따라 뜨는 시간이 다릅니다. 
     에러 방지를 위해 특정 버튼이 '화면에 나타날 때까지' 똑똑하게 기다려주는 기능입니다.

4. 관리 및 분석 도구:
   - ChromeDriverManager: 내 컴퓨터의 크롬 버전에 맞는 드라이버를 인터넷에서 자동으로 찾아 설치해주는 '비서'입니다.
   - BeautifulSoup (BS): 셀레니움이 가져온 복잡한 HTML 덩어리를 예쁘게 분석(Parsing)해서 원하는 데이터만 쉽게 뽑아내는 '요리사'입니다.
'''

import time
import re
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
from bs4 import BeautifulSoup as BS


class MovieScraper:
    def __init__(self, headless=False):
        self.options = Options()
        if headless:
            self.options.add_argument("--headless")

        # 리소스 최적화
        self.options.add_argument("--disable-gpu")
        self.options.add_argument("--no-sandbox")
        self.options.add_argument("--disable-dev-shm-usage")
        self.options.add_argument("--blink-settings=imagesEnabled=false")  # 이미지 차단
        self.options.add_argument("--window-size=1200,1000")
        self.options.add_argument(
            "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

        self.driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=self.options
        )
        self.wait = WebDriverWait(self.driver, 10)

    def _clean_text(self, text):
        """
        [핵심] 토큰 절약을 위한 텍스트 정제 로직
        """
        if not text: return ""
        # 1. HTML 태그만 제거 (BeautifulSoup 활용)
        soup = BS(text, 'html.parser')
        text = soup.get_text()  # 텍스트만 남김

        # 2. 연속된 공백 통합
        text = re.sub(r'\s+', ' ', text)

        # 3. 양끝 공백 제거
        return text.strip()

    def get_expert_reviews(self, movie_id, limit=10):
        url = f"http://www.cine21.com/movie/info/?movie_id={movie_id}"
        reviews = []

        try:
            self.driver.get(url)

            # 전문가 리뷰 섹션 대기
            target_list_selector = "ul.expert_star_list"
            self.wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, target_list_selector)))

            # 부드러운 스크롤로 데이터 렌더링 유도
            target_element = self.driver.find_element(By.CSS_SELECTOR, target_list_selector)
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", target_element)
            time.sleep(1)

            items = self.driver.find_elements(By.CSS_SELECTOR, f"{target_list_selector} > li")

            for item in items[:limit]:
                try:
                    # 1. 평론가 이름 추출
                    try:
                        name = item.find_element(By.CSS_SELECTOR, "div.reviewer .name").text.strip()
                    except Exception:
                        name = "익명 평론가" # 이름이 없는 경우 대비

                    # 2. 평점(score) 추출 - [중요: 예외 처리 강화]
                    try:
                        score_element = item.find_element(By.CSS_SELECTOR, "div.reviewer .num")
                        score = score_element.text.strip()
                        if not score: # 요소는 있는데 텍스트가 비어있는 경우
                            score = "0" 
                    except Exception:
                        score = "평점 없음" # 평점 요소 자체가 없는 경우

                    # 3. 본문 추출 (우선순위: comment_open -> review)
                    content_raw = ""
                    try:
                        # 펼쳐진 댓글창 시도
                        content_raw = item.find_element(By.CSS_SELECTOR, "div.review div.comment_open").text
                    except Exception:
                        try:
                            # 일반 리뷰창 시도
                            content_raw = item.find_element(By.CSS_SELECTOR, "div.review").text
                        except Exception:
                            content_raw = ""

                    # 4. 텍스트 정제 적용
                    clean_content = self._clean_text(content_raw)
                    
                    # 이름과 별점도 정제함 (혹시 모를 특수문자 제거)
                    clean_name = self._clean_text(name)
                    clean_score = self._clean_text(score)

                    # 내용이 있는 경우에만 리스트에 추가
                    if clean_content:
                        reviews.append({
                            "critic": clean_name,
                            "score": clean_score, # 이제 "8.5" 혹은 "평점 없음" 등이 문자열로 들어감
                            "content": clean_content
                        })
                        
                except Exception as e:
                    # 한 개의 리뷰에서 에러가 나도 다음 리뷰로 넘어가도록 처리
                    print(f"      [!] 개별 리뷰 추출 중 오류 발생: {e}")
                    continue

        except Exception as e:
            print(f"   [!] 크롤링 오류: {e}")

        return reviews

    def close(self):
        if self.driver:
            self.driver.quit()