package hotel.service.common;

import hotel.db.entity.User;
import hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                // 🔑 In production: use BCryptPasswordEncoder, not plain equals
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

//    @Override
//    public Optional<User> login(String username, String rawPassword) {
//        return userRepository.findByUsername(username)
//                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
//    }
}

