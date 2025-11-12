package hotel.service.staff;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import hotel.service.receptionist.ReceptionistService;
import hotel.service.receptionist.ReceptionistServiceImpl;
import hotel.util.MessageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static hotel.db.enums.Constants.RECEPTIONIST;
import static hotel.db.enums.Constants.STAFF;

@Service
@AllArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReceptionistServiceImpl receptionistService;

    @Override
    public User getStaff(Integer id){

        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Page<User> getUserListWithFiltersAndPagination(String search,
                                                          String gender,
                                                          String status,
                                                          String sortBy,
                                                          int page,
                                                          int pageSize) {

        List<User> users = userRepository.findByRole(STAFF);

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            users = users.stream()
                    .filter(x -> x.getPhone() != null && x.getPhone().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
        if (gender != null) {
            users = users.stream()
                    .filter(x -> x.getGender().name().equalsIgnoreCase(gender))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            users = users.stream()
                    .filter(x -> x.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        Comparator<User> comparator = Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("name".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase));
        } else if ("email".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareToIgnoreCase));
        } else if ("salary".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(User::getSalary, Comparator.naturalOrder());
        }

        users.sort(comparator);

        int start = Math.min(page * pageSize, users.size());
        int end = Math.min(start + pageSize, users.size());
        List<User> pagedUsers = users.subList(start, end);

        return new PageImpl<>(pagedUsers, PageRequest.of(page, pageSize), users.size());
    }

    @Override
    public MessageResponse createStaff(UserRegisterDto user){
        MessageResponse result = receptionistService.createUserCommon(user, STAFF);
        if(!result.isSuccess()){
            return new MessageResponse(false, result.getMessage());
        }
        return new MessageResponse(true, result.getMessage());
    }
}
