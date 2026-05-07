import os
from google import genai
from dotenv import load_dotenv

load_dotenv()

api_key = os.getenv("GEMINI_API_KEY")
client = genai.Client(api_key=api_key)

print("=== [ Connected M : 모델 이름 찾기 ] ===")

try:
    # 모든 모델을 다 가져온 뒤 이름만 출력합니다.
    model_list = client.models.list()

    for model in model_list:
        # 모델의 이름(name)만 출력해서 404를 방지할 '진짜 이름'을 찾습니다.
        if 'embed' in model.name:
            print(f"✅ 임베딩 모델: {model.name}")
        elif 'gemini' in model.name:
            print(f"🤖 제미나이 모델: {model.name}")

    print("========================================")
    print("💡 위 목록에서 임베딩 모델 이름을 복사해서")
    print("   chatbot_logic.py의 EMBEDDING_MODEL에 넣으세요.")

except Exception as e:
    # 혹시라도 리스트 자체가 안 불릴 경우 에러 출력
    print(f"❌ 에러 발생: {e}")