package hotel.service.common;

import hotel.db.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface CommonService {
    Optional<User> login(String username, String password);
}
