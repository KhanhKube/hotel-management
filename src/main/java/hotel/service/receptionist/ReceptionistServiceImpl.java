package hotel.service.receptionist;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import hotel.util.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;
import static hotel.db.enums.Constants.*;

@Service
@RequiredArgsConstructor
public class ReceptionistServiceImpl implements ReceptionistService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getListReceptionist(){
        List<User> users = userRepository.findByRole(RECEPTIONIST);
        if(users.isEmpty()){
            return null;
        }
        return users;
    }

    @Override
    public User getReceptionist(Integer id){
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public MessageResponse createReceptionist(UserRegisterDto user){
        MessageResponse result = createUserCommon(user, RECEPTIONIST);
        if(!result.isSuccess()){
            return new MessageResponse(false, result.getMessage());
        }
        return new MessageResponse(true, result.getMessage());
    }

    public MessageResponse createUserCommon(UserRegisterDto user, String role){
        if (user == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (isNullOrEmpty(user.getUsername()) ||
                isNullOrEmpty(user.getEmail()) ||
                isNullOrEmpty(user.getPassword()) ||
                isNullOrEmpty(user.getConfirmPassword()) ||
                isNullOrEmpty(user.getFirstName()) ||
                isNullOrEmpty(user.getLastName()) ||
                isNullOrEmpty(user.getPhone()) ||
                user.getDob() == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new MessageResponse(false, EMAILINVALID);
        }
        if (!user.getPhone().matches("^0\\d{9}$")) {
            return new MessageResponse(false, PHONEINVALID);
        }
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            return new MessageResponse(false, PASSWORDNOTMATCH);
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            return new MessageResponse(false, USERNAMEDUPLICATE);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return new MessageResponse(false, EMAILDUPLICATE);
        }
        LocalDate today = LocalDate.now();
        if (Period.between(user.getDob(), today).getYears() < 12) {
            return new MessageResponse(false, DOBINVALID);
        }
        User.Gender gender = Boolean.TRUE.equals(user.getGender()) ? User.Gender.MALE : User.Gender.FEMALE;

        User userResult = new User();
        userResult.setUsername(user.getUsername());
        userResult.setEmail(user.getEmail());
        userResult.setRole(role);
        userResult.setPassword(passwordEncoder.encode(user.getPassword()));
        userResult.setPhone(user.getPhone());
        userResult.setFirstName(user.getFirstName());
        userResult.setLastName(user.getLastName());
        userResult.setGender(gender);
        userResult.setDob(user.getDob());
        userResult.setAddress(user.getAddress());
        userRepository.save(userResult);
        return new MessageResponse(true, REGISTERSUCCESS);

    }
}
