package com.Connectedm.backend.domain.user.repository;

import com.Connectedm.backend.domain.user.entity.LoginLog;
import com.Connectedm.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    // 👈 이 부분을 추가해야 UserService의 빨간 줄이 사라집니다!
    void deleteByUser(User user);
    List<LoginLog> findByUserOrderByLoginAtDesc(User userId);
    List<LoginLog> findAllByOrderByLoginAt();

}
