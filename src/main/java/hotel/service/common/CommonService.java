package hotel.service.common;

import hotel.db.dto.user.UserProfileDto;
import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;

import hotel.util.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CommonService {

    User getUserByUsername(String username);

    Optional<User> login(String username, String password);

    MessageResponse registerUser(UserRegisterDto userRegisterDto);

    MessageResponse editUserProfile(UserProfileDto dto);

    UserProfileDto userToUserProfile(User user);
}
