package hotel.service.common;

import hotel.db.entity.User;
import hotel.dto.request.UserRegisterDto;
import hotel.dto.response.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CommonService {
    Optional<User> login(String username, String password);

    MessageResponse registerUser(UserRegisterDto userRegisterDto);
}
