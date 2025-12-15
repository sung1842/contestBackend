package com.project.mingle.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    // DB의 auto_increment user_no와 매핑될 필드 추가
    private Long userNo;
    private String userId;
    private String userPassword;
    private String userName;
    private String userPhoneNumber;

    private String address;
    private String profileImage;

    // UserController의 /profile 경로에서 사용하기 위한 생성자 추가
    public UserDto(String userId, String userPassword, String userName, String userPhoneNumber) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userPhoneNumber = userPhoneNumber;
    }
}

