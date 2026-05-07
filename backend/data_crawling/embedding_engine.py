from sentence_transformers import SentenceTransformer

class MeaningVectorEngine:
    def __init__(self):
        # 한국어 성능 끝판왕
        self.model = SentenceTransformer('jhgan/ko-sroberta-multitask')
    
    def generate_vector(self, text):
        if not text: return None
        # 문장을 숫자의 나열(의미 좌표)로 변환
        vector = self.model.encode(text)
        return str(vector.tolist()) # DB 저장을 위해 문자열로 변환