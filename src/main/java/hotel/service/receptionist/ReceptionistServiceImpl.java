package hotel.service.receptionist;

import hotel.db.dto.user.UserRegisterDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import hotel.util.MessageResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ch.qos.logback.core.util.StringUtil.isNullOrEmpty;
import static hotel.db.enums.Constants.*;

@Service
@RequiredArgsConstructor
public class ReceptionistServiceImpl implements ReceptionistService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getListReceptionist(){
        List<User> users = userRepository.findByRole(RECEPTIONIST);
        if(users.isEmpty()){
            return null;
        }
        return users;
    }

    @Override
    public User getReceptionist(Integer id){
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Page<User> searchReceptionists(String keyword, String gender, String status, String sort, int page, int size) {
        // Build dynamic query
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show receptionists
            predicates.add(cb.equal(root.get("role"), RECEPTIONIST));

            // Keyword: search by name, email, or phone
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                Predicate nameMatch = cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern)
                );
                predicates.add(nameMatch);
            }

            // Gender filter
            if (gender != null && !gender.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("gender")), gender.toLowerCase()));
            }

            // Status filter
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Sort
        Sort sortConfig = Sort.unsorted();
        if (sort != null) {
            switch (sort) {
                case "nameAsc" -> sortConfig = Sort.by("firstName").ascending();
                case "nameDesc" -> sortConfig = Sort.by("firstName").descending();
                case "dobAsc" -> sortConfig = Sort.by("dob").ascending();
                case "dobDesc" -> sortConfig = Sort.by("dob").descending();
                case "createdAsc" -> sortConfig = Sort.by("createdAt").ascending();
                case "createdDesc" -> sortConfig = Sort.by("createdAt").descending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortConfig);
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public MessageResponse createReceptionist(UserRegisterDto user){
        MessageResponse result = createUserCommon(user, RECEPTIONIST);
        if(!result.isSuccess()){
            return new MessageResponse(false, result.getMessage());
        }
        return new MessageResponse(true, result.getMessage());
    }

    public MessageResponse createUserCommon(UserRegisterDto user, String role){
        if (user == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (isNullOrEmpty(user.getUsername()) ||
                isNullOrEmpty(user.getEmail()) ||
                isNullOrEmpty(user.getPassword()) ||
                isNullOrEmpty(user.getConfirmPassword()) ||
                isNullOrEmpty(user.getFirstName()) ||
                isNullOrEmpty(user.getLastName()) ||
                isNullOrEmpty(user.getPhone()) ||
                user.getDob() == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new MessageResponse(false, EMAILINVALID);
        }
        if (!user.getPhone().matches("^0\\d{9}$")) {
            return new MessageResponse(false, PHONEINVALID);
        }
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            return new MessageResponse(false, PASSWORDNOTMATCH);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return new MessageResponse(false, EMAILDUPLICATE);
        }
        LocalDate today = LocalDate.now();
        if (Period.between(user.getDob(), today).getYears() < 12) {
            return new MessageResponse(false, DOBINVALID);
        }
        User.Gender gender = Boolean.TRUE.equals(user.getGender()) ? User.Gender.MALE : User.Gender.FEMALE;

        User userResult = new User();
        userResult.setEmail(user.getEmail());
        userResult.setRole(role);
        userResult.setPassword(passwordEncoder.encode(user.getPassword()));
        userResult.setPhone(user.getPhone());
        userResult.setFirstName(user.getFirstName());
        userResult.setLastName(user.getLastName());
        userResult.setGender(gender);
        userResult.setDob(user.getDob());
        userResult.setAddress(user.getAddress());
        userRepository.save(userResult);
        return new MessageResponse(true, REGISTERSUCCESS);

    }

    @Override
    public MessageResponse updateReceptionist(Integer receptionistId, User user){
        if (user == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        boolean exists = userRepository.findById(receptionistId).isPresent();
        if(!exists){
            return new MessageResponse(false, USERNOTEXIST);
        }
        if (isNullOrEmpty(user.getEmail()) ||
                isNullOrEmpty(user.getFirstName()) ||
                isNullOrEmpty(user.getLastName()) ||
                isNullOrEmpty(user.getPhone()) ||
                user.getDob() == null) {
            return new MessageResponse(false, FILLALLFEILD);
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new MessageResponse(false, EMAILINVALID);
        }
        if (!user.getPhone().matches("^0\\d{9}$")) {
            return new MessageResponse(false, PHONEINVALID);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return new MessageResponse(false, EMAILDUPLICATE);
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            return new MessageResponse(false, PHONEDUPLICATE);
        }
        LocalDate today = LocalDate.now();
        if (Period.between(user.getDob(), today).getYears() < 12) {
            return new MessageResponse(false, DOBINVALID);
        }
        User.Gender gender = Boolean.TRUE.equals(user.getGender()) ? User.Gender.MALE : User.Gender.FEMALE;

        User userResult = userRepository.findById(receptionistId).get();
        userResult.setEmail(user.getEmail());
        userResult.setPhone(user.getPhone());
        userResult.setFirstName(user.getFirstName());
        userResult.setLastName(user.getLastName());
        userResult.setGender(gender);
        userResult.setDob(user.getDob());
        userResult.setAddress(user.getAddress());
        userRepository.save(userResult);
        return new MessageResponse(true, REGISTERSUCCESS);

    }

}
