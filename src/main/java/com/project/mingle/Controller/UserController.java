package com.project.mingle.Controller;

import com.project.mingle.Config.JwtTokenProvider;
import com.project.mingle.Dto.UserDto;
import com.project.mingle.Entity.UserDetail;
import com.project.mingle.Entity.Users;
import com.project.mingle.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 클라이언트가 보낸 JWT 토큰을 검증하고, 유효하다면 해당 사용자의 정보를 반환합니다.
     * @param request HTTP 요청 객체 (Authorization 헤더를 읽기 위해 사용)
     * @return 유효한 토큰일 경우 사용자 정보, 아닐 경우 401 Unauthorized 에러
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        System.out.println("Hello!!!!!");
        // 1. 요청 헤더에서 "Authorization" 값을 가져옵니다.
        String header = request.getHeader("Authorization");

        // 2. 헤더가 없거나 "Bearer "로 시작하지 않으면 유효하지 않은 요청입니다.
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "토큰이 없거나 형식이 올바르지 않습니다."));
        }

        // 3. "Bearer " 부분을 제외한 순수 토큰 문자열만 추출합니다.
        String token = header.substring(7);

        try {
            // 4. JwtTokenProvider를 사용해 토큰이 유효한지 검증합니다.
            if (jwtTokenProvider.validateToken(token)) {
                // 5. 토큰에서 사용자 ID를 추출합니다.
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                // 6. 사용자 ID로 DB에서 전체 사용자 정보를 조회합니다.
                Optional<Users> userOptional = service.findByUserId(userId);

                if (userOptional.isPresent()) {
                    // 7. 사용자 정보가 있다면 DTO로 변환하여 반환합니다.
                    Users user = userOptional.get();
                    // User 엔티티에서 UserDetail 정보를 가져옵니다.
                    UserDetail detail = user.getUserDetail();
                    UserDto userDto = new UserDto(
                            user.getUserId(),
                            null,
                            user.getUsername(),
                            user.getUserPhoneNumber()
                    );
                    // UserDetail 정보가 있다면 DTO에 추가합니다.
                    if (detail != null) {
                        userDto.setAddress(detail.getAddress());
                        userDto.setProfileImage(detail.getProfileImage());
                    }
                    return ResponseEntity.ok(userDto);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자를 찾을 수 없습니다."));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "토큰이 유효하지 않습니다."));
            }
        } catch (Exception e) {
            // 8. 토큰 파싱 중 예외(만료, 변조 등)가 발생하면 401 에러를 반환합니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "토큰이 만료되었거나 유효하지 않습니다."));
        }
    }

    // 아이디 중복 체크
    @PostMapping("/check-id")
    public boolean checkUserId(@RequestBody UserDto dto) {
        System.out.println("check id :"+dto.getUserId());
        boolean isDuplicated = service.isUserIdDuplicated(dto.getUserId());
        System.out.println(isDuplicated ? "======id not duplicated!!!======" : "======id duplicated!!!======");
        return isDuplicated;
    }

    // 이름 중복 체크
    @PostMapping("/check-name")
    public boolean checkUserName(@RequestBody UserDto dto) {
        System.out.println("check name :"+dto.getUserName());
        boolean isDuplicated = service.isUserNameDuplicated(dto.getUserName());
        System.out.println(isDuplicated ? "======name not duplicated!!!======" : "======name duplicated!!!======");
        return isDuplicated;
    }

    @PostMapping("/sign-up")
    public Users postUser(@RequestBody UserDto dto){
        System.out.println("signup user :"+dto);
        return service.saveUser(dto);
    }

    /**
     * 기존 로그인 메서드를 JWT 토큰 발급 로직으로 변경합니다.
     * @param dto 로그인 정보 (userId, userPassword)
     * @return 성공 시 JWT 토큰, 실패 시 401 Unauthorized 응답
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDto dto){
        System.out.println("Hello login");
        boolean isAuthenticated = service.login(dto);

        if (isAuthenticated) {
            String token = jwtTokenProvider.generateToken(dto.getUserId());
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "로그인 실패: 아이디 또는 비밀번호가 일치하지 않습니다."));
        }
    }

    @PostMapping("/findPassword")
    public boolean findPassword(@RequestBody UserDto dto){
        System.out.println("find-password"+dto);
        boolean isDuplicated = service.isUserIdDuplicated(dto.getUserId());
        System.out.println(isDuplicated ? "======id exist!!!======" : "======id not exist!!!======");
        return isDuplicated;
    }

    @PostMapping("/resetPassword")
    public Integer resetPassword(@RequestBody UserDto dto){
        System.out.println("reset-password"+dto);
        return service.resetPassword(dto);
    }

    @PostMapping("/findId")
    public ResponseEntity<String> findUserId(@RequestBody UserDto dto) {
        System.out.println("find user ID with phone number: " + dto.getUserPhoneNumber());
        Optional<String> userId = service.findUserIdByPhoneNumber(dto.getUserPhoneNumber());

        if (userId.isPresent()) {
            return ResponseEntity.ok(userId.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateUserProfile(
            Principal principal, // 현재 로그인된 사용자 정보를 가져옴
            @RequestPart("userDto") UserDto userDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            // Principal.getName()은 UserDetails의 getUsername()을 호출 (우리의 경우 userId)
            String userId = principal.getName();

            Users updatedUser = service.updateUser(userId, userDto, profileImage);

            // 업데이트된 최신 사용자 정보를 다시 DTO로 변환하여 프론트엔드에 반환
            UserDetail detail = updatedUser.getUserDetail();
            UserDto responseDto = new UserDto(
                    updatedUser.getUserId(),
                    null, // 보안을 위해 비밀번호는 반환하지 않음
                    updatedUser.getUsername(),
                    updatedUser.getUserPhoneNumber()
            );
            if (detail != null) {
                responseDto.setAddress(detail.getAddress());
                responseDto.setProfileImage(detail.getProfileImage());
            }

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "정보 수정에 실패했습니다: " + e.getMessage()));
        }
    }
}

