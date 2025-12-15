package com.project.mingle.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_detail")
@Getter
@Setter
@NoArgsConstructor
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유 ID

    private String address; // 주소

    private String profileImage; // 프로필 이미지 경로 (URL 또는 파일 경로)

    // Users 엔티티와의 1:1 관계 설정
    // 'user' 필드를 통해 Users 엔티티를 참조합니다.
    @OneToOne
    @JoinColumn(name = "user_no") // 외래 키 컬럼 이름 지정
    private Users user;

    public UserDetail(String address, String profileImage, Users user) {
        this.address = address;
        this.profileImage = profileImage;
        this.user = user;
    }
}