package com.project.mingle.Service;

import com.project.mingle.Dto.UserDto;
import com.project.mingle.Entity.UserDetail;
import com.project.mingle.Entity.Users;
import com.project.mingle.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public boolean isUserIdDuplicated(String userId) {
        return repository.findByUserId(userId).isPresent();
    }

    public boolean isUserNameDuplicated(String user_name) {
        return repository.findByUserName(user_name).isPresent();
    }

    @Transactional
    public Users saveUser(UserDto dto){
        if (isUserIdDuplicated(dto.getUserId())) {
            throw new IllegalArgumentException("아이디가 이미 존재합니다.");
        }
        if (isUserNameDuplicated(dto.getUserId())) {
            throw new IllegalArgumentException("이름이 이미 존재합니다.");
        }

        Users users = new Users();
        users.setUserId(dto.getUserId());
        users.setUserName(dto.getUserName());
        users.setUserPassword(passwordEncoder.encode(dto.getUserPassword()));
        users.setUserPhoneNumber(dto.getUserPhoneNumber());

        // 2. UserDetail 엔티티 생성
        // 프로필 이미지가 없으면 기본 이미지 경로를, 주소가 없으면 빈 문자열을 저장
        String profileImage = (dto.getProfileImage() == null || dto.getProfileImage().isEmpty())
                ? "/images/default_profile.png"
                : dto.getProfileImage();
        String address = (dto.getAddress() == null) ? "" : dto.getAddress();

        UserDetail userDetail = new UserDetail(address, profileImage, users);

        // 3. 연관관계 편의 메서드를 사용하여 Users와 UserDetail을 연결
        users.setUserDetail(userDetail);

        return repository.save(users);
    }

    public boolean login(UserDto dto){
        // 1. 사용자의 아이디로 데이터베이스에서 사용자 정보를 조회
        Optional<Users> user = repository.findByUserId(dto.getUserId());
        System.out.println(user);

        //사용자 존재 확인
        if(user == null){
            return false;
        }

        //평문 비밀번호와 암호화된 비밀번호를 비교
        if(passwordEncoder.matches(dto.getUserPassword(), user.get().getUserPassword())){
            return true;
        }else{
            return false;
        }
    }

    @Transactional
    public Integer resetPassword(UserDto dto) {
        if (!isUserIdDuplicated(dto.getUserId())) {
            throw new IllegalArgumentException("아이디가 존재하지 않습니다.");
        }

        // 1. DTO에서 사용자 ID와 새로운 비밀번호를 가져옵니다.
        String userId = dto.getUserId();
        String newPassword = dto.getUserPassword(); // UserDto에 새로운 비밀번호 필드가 있다고 가정

        // 2. 새로운 비밀번호를 암호화합니다.
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 3. 암호화된 비밀번호와 사용자 ID를 Repository 메서드에 전달합니다.
        return repository.resetPassword(userId, encodedPassword);
    }
    /**
     * 전화번호로 사용자 아이디를 찾습니다.
     * @param phoneNumber 찾고자 하는 사용자의 전화번호
     * @return 아이디가 존재하면 Optional<String>에 담아 반환, 없으면 Optional.empty() 반환
     */
    public Optional<String> findUserIdByPhoneNumber(String phoneNumber) {
        // 1. 전화번호로 사용자를 조회합니다.
        Optional<Users> userOptional = repository.findByUserPhoneNumber(phoneNumber);

        // 2. 사용자가 존재하면 아이디를 반환하고, 아니면 Optional.empty()를 반환합니다.
        return userOptional.map(Users::getUserId);
    }
    public Optional<Users> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }
    @Transactional
    public Users updateUser(String userId, UserDto userDto, MultipartFile profileImageFile) {
        // 1. 현재 사용자 정보 조회
        Users user = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 2. 연락처 정보 업데이트 (값이 있을 경우에만)
        if (userDto.getUserPhoneNumber() != null && !userDto.getUserPhoneNumber().isEmpty()) {
            user.setUserPhoneNumber(userDto.getUserPhoneNumber());
        }

        // 3. 비밀번호 업데이트 (새 비밀번호가 입력된 경우에만)
        if (userDto.getUserPassword() != null && !userDto.getUserPassword().isEmpty()) {
            user.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));
        }

        // 4. 프로필 이미지 업데이트 (새 이미지가 업로드된 경우에만)
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            UserDetail userDetail = user.getUserDetail();
            if (userDetail == null) {
                userDetail = new UserDetail();
                user.setUserDetail(userDetail);
            }

            // 기존 이미지 경로를 변수에 저장
            String oldImagePath = userDetail.getProfileImage();

            // 새 파일을 저장하고 경로를 업데이트
            String newImagePath = fileStorageService.storeFile(profileImageFile);
            userDetail.setProfileImage(newImagePath);

            // 변경사항을 먼저 DB에 저장
            Users savedUser = repository.save(user);

            // DB 저장이 성공하면, 기존 이미지 파일을 서버에서 삭제
            if (oldImagePath != null && !oldImagePath.contains("default_profile.png")) {
                fileStorageService.deleteFile(oldImagePath);
            }

            return savedUser;
        }

        // 이미지 변경이 없으면, 다른 정보만 저장
        return repository.save(user);
    }
}
