package hotel.service.staff;

import hotel.db.dto.staff.StaffListDto;
import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.util.MessageResponse;
import org.springframework.data.domain.Page;

public interface StaffService {

    User getStaff(Integer id);

    Page<User> getUserListWithFiltersAndPagination(String search,
                                                   String gender,
                                                   String status,
                                                   String sortBy,
                                                   int page,
                                                   int pageSize);

    Page<StaffListDto> getStaffListForManagement(String search,
                                                  String role,
                                                  String gender,
                                                  String status,
                                                  String sortBy,
                                                  int page,
                                                  int pageSize);

    MessageResponse createStaff(UserRegisterDto user);

    String createStaffFromForm(User user);

}
