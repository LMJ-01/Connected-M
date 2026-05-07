package com.Connectedm.backend.config;

import com.Connectedm.backend.domain.user.entity.User;
import com.Connectedm.backend.domain.user.entity.UserRole;
import com.Connectedm.backend.domain.user.entity.UserStatus;
import com.Connectedm.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//@Component // 💡 스프링이 이 클래스를 자동으로 찾아 실행하게 합니다.
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 [서버 시작] 영화 데이터 자동 조회를 시작합니다...");

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 1; i <= 211; i++) {
            try {
                String url = "http://localhost:8080/api/contents/" + i;
                // 실제 브라우저에서 들어가는 것과 똑같이 GET 요청을 보냅니다.
                restTemplate.getForObject(url, String.class);
                System.out.println("✅ ID " + i + " 호출 완료");
            } catch (Exception e) {
                // 에러가 나도 멈추지 않고 다음 ID로 넘어갑니다.
                System.err.println("❌ ID " + i + " 호출 실패: " + e.getMessage());
            }
        }

        System.out.println("✨ 모든 데이터 자동 조회가 완료되었습니다!");

        String adminEmail = "admin@connectedm.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin1234"))
                    .nickname("최고관리자")
                    .realName("관리자")
                    .role(UserRole.ROLE_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .reportedCount(0)
                    .provider(User.AuthProvider.LOCAL)
                    .build();

            userRepository.save(admin);
            System.out.println(">> [System] 어드민 계정 생성 완료");
        }
    }
}

