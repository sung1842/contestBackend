package com.project.mingle.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @Column(name = "user_phone_number")
    private String userPhoneNumber;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserDetail userDetail;

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
        if (userDetail != null) {
            userDetail.setUser(this);
        }
    }

    // --- UserDetails 인터페이스의 메서드 구현 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 역할(Role)을 사용하지 않으므로 빈 리스트를 반환합니다.
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.userPassword;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 'username'으로 사용할 필드를 지정합니다. (우리는 userId)
        return this.userId;
    }

    // 아래 4개 메서드는 계정 상태를 관리합니다. (만료, 잠김 등)
    // 현재는 모두 true로 설정하여 항상 활성화된 상태로 둡니다.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
