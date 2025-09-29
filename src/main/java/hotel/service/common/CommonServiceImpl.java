package hotel.service.common;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import hotel.util.MessageResponse;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;
import static hotel.config.Constants.*;


@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
// Check required fields
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
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return new MessageResponse(false, PASSWORDNOTMATCH);
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            return new MessageResponse(false, USERNAMEDUPLICATE);
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return new MessageResponse(false, EMAILDUPLICATE);
        }

        // Handle gender mapping
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

}

