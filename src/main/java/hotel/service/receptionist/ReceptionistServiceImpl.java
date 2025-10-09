package hotel.service.receptionist;

import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static hotel.db.enums.Constants.RECEPTIONIST;

@Service
@RequiredArgsConstructor
public class ReceptionistServiceImpl implements ReceptionistService {

    private final UserRepository userRepository;

    @Override
    public List<User> getListReceptionist(){
        List<User> users = userRepository.findByRole(RECEPTIONIST);
        if(users.isEmpty()){
            return null;
        }
        return users;
    }

}
