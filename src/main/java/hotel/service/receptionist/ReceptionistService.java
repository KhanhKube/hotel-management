package hotel.service.receptionist;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.util.MessageResponse;

import java.util.List;

public interface ReceptionistService {

    List<User> getListReceptionist();

    User getReceptionist(Integer id);

    MessageResponse createReceptionist(UserRegisterDto user);
}
