package hotel.service.receptionist;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.util.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReceptionistService {

    List<User> getListReceptionist();

    User getReceptionist(Integer id);

    MessageResponse createReceptionist(UserRegisterDto user);

    Page<User> searchReceptionists(String keyword, String gender, String status, String sort, int page, int size);

    MessageResponse updateReceptionist(Integer receptionistId, User user);
}
