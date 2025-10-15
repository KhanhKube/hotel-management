package hotel.service.common;

import hotel.db.dto.user.UserProfileDto;
import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import hotel.util.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;
import static hotel.db.enums.Constants.*;


@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public Optional<User> login(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    @Override
    public MessageResponse registerUser(UserRegisterDto dto) {
        if (dto == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (isNullOrEmpty(dto.getUsername()) ||
                isNullOrEmpty(dto.getEmail()) ||
                isNullOrEmpty(dto.getPassword()) ||
                isNullOrEmpty(dto.getConfirmPassword()) ||
                isNullOrEmpty(dto.getFirstName()) ||
                isNullOrEmpty(dto.getLastName()) ||
                isNullOrEmpty(dto.getPhone()) ||
                dto.getDob() == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new MessageResponse(false, EMAILINVALID);
        }
        if (!dto.getPhone().matches("^0\\d{9}$")) {
            return new MessageResponse(false, PHONEINVALID);
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return new MessageResponse(false, PASSWORDNOTMATCH);
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            return new MessageResponse(false, USERNAMEDUPLICATE);
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return new MessageResponse(false, EMAILDUPLICATE);
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            return new MessageResponse(false, PHONEDUPLICATE);
        }
        LocalDate today = LocalDate.now();
        if (Period.between(dto.getDob(), today).getYears() < 12) {
            return new MessageResponse(false, DOBINVALID);
        }
        User.Gender gender = Boolean.TRUE.equals(dto.getGender()) ? User.Gender.MALE : User.Gender.FEMALE;

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(USER);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(gender);
        user.setDob(dto.getDob());
        user.setAddress(dto.getAddress());
        userRepository.save(user);
        return new MessageResponse(true, REGISTERSUCCESS);
    }

    @Override
    public MessageResponse editUserProfile(UserProfileDto dto) {
        if (dto == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (isNullOrEmpty(dto.getUsername()) ||
                isNullOrEmpty(dto.getEmail()) ||
                isNullOrEmpty(dto.getFirstName()) ||
                isNullOrEmpty(dto.getLastName()) ||
                isNullOrEmpty(dto.getPhone()) ||
                dto.getDob() == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new MessageResponse(false, EMAILINVALID);
        }
        if (userRepository.existsByEmailAndUsernameNot(dto.getEmail(), dto.getUsername())) {
            return new MessageResponse(false, EMAILEXIST);
        }
        if (!dto.getPhone().matches("^0\\d{9}$")) {
            return new MessageResponse(false, PHONEINVALID);
        }
        if (userRepository.existsByPhoneAndUsernameNot(dto.getPhone(), dto.getUsername())) {
            return new MessageResponse(false, PHONEEXIST);
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            return new MessageResponse(false, PHONEDUPLICATE);
        }
        LocalDate today = LocalDate.now();
        if (Period.between(dto.getDob(), today).getYears() < 12) {
            return new MessageResponse(false, DOBINVALID);
        }
        User.Gender gender = Boolean.TRUE.equals(dto.getGender()) ? User.Gender.MALE : User.Gender.FEMALE;

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException(USERNOTEXIST));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDob(dto.getDob());
        user.setGender(gender);
        user.setAddress(dto.getAddress());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new MessageResponse(true, UPDATESUCCESS);
    }


    @Override
    public UserProfileDto userToUserProfile(User user) {
        if (user == null) {
            return null;
        }

        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPhone(user.getPhone());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        if (user.getGender() != null) {
            switch (user.getGender()) {
                case MALE:
                    dto.setGender(true);
                    break;
                case FEMALE:
                    dto.setGender(false);
                    break;
                case OTHER:
                    dto.setGender(null);
                    break;
            }
        }
        dto.setDob(user.getDob());
        dto.setAddress(user.getAddress());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    @Override
    public MessageResponse updateAvatar(String userName, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return new MessageResponse(false, CHOOSEPICTURE);
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return new MessageResponse(false, FILEINVALID);
        }

        // Upload directory outside resources
        String uploadDir = "src/main/resources/static/images/avatar";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Clean file name: remove spaces and special chars
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return new MessageResponse(false, FILEINVALID);
        }

        // Replace spaces with underscore, remove anything that's not letter/number/dot/underscore/dash
        String safeFileName = originalFileName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");

        String fileName = UUID.randomUUID() + "_" + safeFileName;
        Path filePath = Paths.get(uploadDir, fileName);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update user profile
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException(USERNOTEXIST));
        user.setAvatarUrl("/images/avatar/" + fileName);
        userRepository.save(user);

        return new MessageResponse(true, UPDATESUCCESS);
    }

}

