package hotel.service.common;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;

import hotel.util.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CommonService {
    Optional<User> login(String username, String password);

    MessageResponse registerUser(UserRegisterDto userRegisterDto);
}
