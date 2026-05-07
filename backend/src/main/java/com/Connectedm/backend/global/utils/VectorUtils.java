package com.Connectedm.backend.global.utils;
import java.util.Arrays;

public class VectorUtils {
    /**
     * [코사인 유사도 엔진]
     * 두 벡터 사이의 각도를 계산하여 유사도를 측정합니다. ㅋㅋㅋㅋ
     * 결과값: 1.0에 가까울수록 "이건 거의 똑같은 영화다!" ㅋㅋㅋㅋ
     */
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * [데이터 변환기]
     * DB의 LONGTEXT 문자열을 자바가 계산할 수 있는 숫자 배열로 '딸깍' 바꿉니다! ㅋㅋㅋㅋ
     */
    public static double[] parseVectorString(String vectorStr) {
        if (vectorStr == null || vectorStr.isEmpty()) return new double[0];

        // 대괄호 [ ] 제거하고 쉼표로 분리 ㅋㅋㅋㅋ
        String cleanStr = vectorStr.replace("[", "").replace("]", "");
        return Arrays.stream(cleanStr.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
