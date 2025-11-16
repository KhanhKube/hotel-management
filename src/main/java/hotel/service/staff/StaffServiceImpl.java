package hotel.service.staff;

import hotel.db.dto.staff.StaffListDto;
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
        if (gender != null && !gender.isEmpty()) {
            users = users.stream()
                    .filter(x -> x.getGender().name().equalsIgnoreCase(gender))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
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

    @Override
    public Page<StaffListDto> getStaffListForManagement(String search,
                                                        String role,
                                                        String gender,
                                                        String status,
                                                        String sortBy,
                                                        int page,
                                                        int pageSize) {
        List<User> users = userRepository.findByRoleStaffOrReceptionist();

        // Filter theo role
        if (role != null && !role.isEmpty()) {
            users = users.stream()
                    .filter(x -> role.equals(x.getRole()))
                    .collect(Collectors.toList());
        }

        // Filter theo search (tìm theo họ tên hoặc mã nhân viên)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            users = users.stream()
                    .filter(x -> {
                        String fullName = (x.getFirstName() != null ? x.getFirstName() : "") + " " + 
                                        (x.getLastName() != null ? x.getLastName() : "");
                        fullName = fullName.trim().toLowerCase();
                        String codeStaff = x.getCodeStaff() != null ? x.getCodeStaff().toLowerCase() : "";
                        return fullName.contains(searchLower) || codeStaff.contains(searchLower);
                    })
                    .collect(Collectors.toList());
        }

        // Filter theo gender
        if (gender != null && !gender.isEmpty()) {
            users = users.stream()
                    .filter(x -> x.getGender() != null && x.getGender().name().equalsIgnoreCase(gender))
                    .collect(Collectors.toList());
        }

        // Filter theo status
        if (status != null && !status.isEmpty()) {
            users = users.stream()
                    .filter(x -> x.getStatus() != null && x.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // Sort
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] sortParams = sortBy.split(",");
            String field = sortParams[0];
            String direction = sortParams.length > 1 ? sortParams[1] : "asc";

            Comparator<User> comparator = null;

            if ("name".equals(field)) {
                comparator = Comparator.comparing(u -> {
                    String fullName = (u.getFirstName() != null ? u.getFirstName() : "") + " " + 
                                    (u.getLastName() != null ? u.getLastName() : "");
                    return fullName.trim();
                }, Comparator.nullsLast(String::compareToIgnoreCase));
            } else if ("salary".equals(field)) {
                comparator = Comparator.comparing(User::getSalary, Comparator.nullsLast(Comparator.naturalOrder()));
            }

            if (comparator != null) {
                if ("desc".equals(direction)) {
                    comparator = comparator.reversed();
                }
                users.sort(comparator);
            }
        }

        // Convert to DTO
        List<StaffListDto> staffDtos = users.stream()
                .map(u -> new StaffListDto(
                        u.getUserId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getCodeStaff(),
                        u.getPhone(),
                        u.getEmail(),
                        u.getGender() != null ? u.getGender().name() : null,
                        u.getRole(),
                        u.getSalary(),
                        u.getStatus() != null ? u.getStatus().name() : null
                ))
                .collect(Collectors.toList());

        // Pagination
        int start = Math.min(page * pageSize, staffDtos.size());
        int end = Math.min(start + pageSize, staffDtos.size());
        List<StaffListDto> pagedStaff = staffDtos.subList(start, end);

        return new PageImpl<>(pagedStaff, PageRequest.of(page, pageSize), staffDtos.size());
    }

    @Override
    public String createStaffFromForm(User user) {
        try {
            // Validate firstName (tên) - max 50 ký tự
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                return "Tên không được để trống";
            }
            if (user.getFirstName().length() > 50) {
                return "Tên không được vượt quá 50 ký tự";
            }

            // Validate lastName (họ) - max 50 ký tự
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                return "Họ không được để trống";
            }
            if (user.getLastName().length() > 50) {
                return "Họ không được vượt quá 50 ký tự";
            }

            // Validate email
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return "Email không được để trống";
            }
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return "Email đã tồn tại trong hệ thống";
            }

            // Validate phone - phải đúng 10 số
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                return "Số điện thoại không được để trống";
            }
            if (!user.getPhone().matches("\\d{10}")) {
                return "Số điện thoại phải có đúng 10 chữ số";
            }
            if (userRepository.findByPhone(user.getPhone()).isPresent()) {
                return "Số điện thoại đã tồn tại trong hệ thống";
            }

            // Validate password
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return "Mật khẩu không được để trống";
            }

            // Validate address - max 150 ký tự
            if (user.getAddress() != null && user.getAddress().length() > 150) {
                return "Địa chỉ không được vượt quá 150 ký tự";
            }

            // Validate role
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                return "Vai trò không được để trống";
            }
            if (!user.getRole().equals(RECEPTIONIST) && !user.getRole().equals(STAFF)) {
                return "Vai trò không hợp lệ";
            }

            // Validate gender
            if (user.getGender() == null) {
                return "Giới tính không được để trống";
            }

            // Validate dob
            if (user.getDob() == null) {
                return "Ngày sinh không được để trống";
            }

            // Validate contract end date - tối thiểu 7 ngày từ hôm nay
            if (user.getContractEndDate() == null) {
                return "Ngày hết hạn hợp đồng không được để trống";
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate minEndDate = today.plusDays(7);
            if (user.getContractEndDate().isBefore(minEndDate)) {
                return "Ngày hết hạn hợp đồng phải cách ngày hôm nay ít nhất 7 ngày";
            }

            // Set contract start date = today
            user.setContractStartDate(today);

            // Username để null cho STAFF và RECEPTIONIST
            user.setUsername(null);

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Set default status
            if (user.getStatus() == null) {
                user.setStatus(User.Status.ACTIVE);
            }

            // Set otp verified = true để không cần xác thực OTP
            user.setOtpVerified(true);

            // Generate code staff
            String codeStaff = generateStaffCode();
            user.setCodeStaff(codeStaff);

            // Set default salary
            if (user.getSalary() == null) {
                user.setSalary(BigDecimal.ZERO);
            }

            // Save user
            userRepository.save(user);

            return null; // null = success, no error
        } catch (Exception e) {
            return "Có lỗi xảy ra: " + e.getMessage();
        }
    }

    @Override
    public String updateStaffFromForm(User user) {
        try {
            // Lấy user hiện tại từ database
            User existingUser = userRepository.findById(user.getUserId()).orElse(null);
            if (existingUser == null) {
                return "Không tìm thấy nhân viên";
            }

            // Validate firstName (tên) - max 50 ký tự
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                return "Tên không được để trống";
            }
            if (user.getFirstName().length() > 50) {
                return "Tên không được vượt quá 50 ký tự";
            }

            // Validate lastName (họ) - max 50 ký tự
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                return "Họ không được để trống";
            }
            if (user.getLastName().length() > 50) {
                return "Họ không được vượt quá 50 ký tự";
            }

            // Validate address - max 150 ký tự
            if (user.getAddress() != null && user.getAddress().length() > 150) {
                return "Địa chỉ không được vượt quá 150 ký tự";
            }

            // Validate gender
            if (user.getGender() == null) {
                return "Giới tính không được để trống";
            }

            // Validate dob
            if (user.getDob() == null) {
                return "Ngày sinh không được để trống";
            }

            // Validate contract end date - tối thiểu 7 ngày từ hôm nay
            if (user.getContractEndDate() == null) {
                return "Ngày hết hạn hợp đồng không được để trống";
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate minEndDate = today.plusDays(7);
            if (user.getContractEndDate().isBefore(minEndDate)) {
                return "Ngày hết hạn hợp đồng phải cách ngày hôm nay ít nhất 7 ngày";
            }

            // Update các trường được phép thay đổi
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setGender(user.getGender());
            existingUser.setDob(user.getDob());
            existingUser.setAddress(user.getAddress());
            existingUser.setContractEndDate(user.getContractEndDate());

            // Giữ nguyên các trường không được phép thay đổi: email, phone, role, username, password, contractStartDate, codeStaff

            // Save user
            userRepository.save(existingUser);

            return null; // null = success, no error
        } catch (Exception e) {
            return "Có lỗi xảy ra: " + e.getMessage();
        }
    }

    @Override
    public void deleteStaff(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setIsDeleted(true);
            userRepository.save(user);
        }
    }

    private String generateStaffCode() {
        String prefix = "NV";
        long count = userRepository.count() + 1;
        return prefix + String.format("%05d", count);
    }
}
