package com.project.mingle.Repository;

import com.project.mingle.Entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUserId(String userId); // user_id로 조회
    Optional<Users> findByUserName(String userName); // user_name으로 조회
    Optional<Users> findByUserPhoneNumber(String userPhoneNumber); // 전화번호로 사용자 조회 메서드 추가

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.userPassword = :newPassword WHERE u.userId = :userId")
    int resetPassword(String userId, String newPassword);
}
