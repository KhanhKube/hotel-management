package hotel.service.account;

import hotel.db.dto.user.AccountRequestDto;
import hotel.db.dto.user.AccountResponseDto;
import hotel.db.entity.User;
import hotel.db.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        log.info("Getting all accounts");
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(user -> !user.getIsDeleted()) // Filter out soft deleted accounts
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccountsExceptAdmin() {
        log.info("Getting all accounts except ADMIN");
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(user -> !"ADMIN".equals(user.getRole()) && !user.getIsDeleted()) // Filter out ADMIN and soft deleted accounts
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> getAccountsWithPagination(String searchTerm, String status, int page, int size) {
        log.info("Getting accounts with pagination - page: {}, size: {}, search: {}, status: {}", page, size, searchTerm, status);
        
        // Build specification for filtering
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Exclude ADMIN role
            predicates.add(cb.notEqual(root.get("role"), "ADMIN"));
            
            // Exclude soft deleted accounts
            predicates.add(cb.equal(root.get("isDeleted"), false));
            
            // Search by full name (firstName + lastName), username, email, or phone
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String search = searchTerm.trim().toLowerCase();
                String pattern = "%" + search + "%";
                // Search in firstName, lastName, username, email, or phone
                Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phonePredicate = cb.like(root.get("phone"), pattern);
                Predicate searchOr = cb.or(firstNamePredicate, lastNamePredicate, usernamePredicate, emailPredicate, phonePredicate);
                predicates.add(searchOr);
            }
            
            // Filter by status
            if (status != null && !status.isEmpty() && !"ALL".equals(status.toUpperCase())) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // Pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // Convert to DTO
        List<AccountResponseDto> dtos = userPage.getContent().stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> getCustomersWithPagination(String searchTerm, String status, int page, int size) {
        log.info("Getting customers with pagination - page: {}, size: {}, search: {}, status: {}", page, size, searchTerm, status);
        
        // Build specification for filtering customers only
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Only CUSTOMER role
            predicates.add(cb.equal(root.get("role"), "CUSTOMER"));
            
            // Exclude soft deleted accounts
            predicates.add(cb.equal(root.get("isDeleted"), false));
            
            // Search by full name (firstName + lastName), username, email, or phone
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String search = searchTerm.trim().toLowerCase();
                String pattern = "%" + search + "%";
                // Search in firstName, lastName, username, email, or phone
                Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phonePredicate = cb.like(root.get("phone"), pattern);
                Predicate searchOr = cb.or(firstNamePredicate, lastNamePredicate, usernamePredicate, emailPredicate, phonePredicate);
                predicates.add(searchOr);
            }
            
            // Filter by status
            if (status != null && !status.isEmpty() && !"ALL".equals(status.toUpperCase())) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // Pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // Convert to DTO
        List<AccountResponseDto> dtos = userPage.getContent().stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> getStaffsWithPagination(String searchTerm, String status, int page, int size) {
        log.info("Getting staffs with pagination - page: {}, size: {}, search: {}, status: {}", page, size, searchTerm, status);
        
        // Build specification for filtering staffs only (STAFF, RECEPTIONIST, MANAGER)
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Only STAFF, RECEPTIONIST, MANAGER roles
            Predicate staffRole = cb.equal(root.get("role"), "STAFF");
            Predicate receptionistRole = cb.equal(root.get("role"), "RECEPTIONIST");
            Predicate managerRole = cb.equal(root.get("role"), "MANAGER");
            Predicate staffRoles = cb.or(staffRole, receptionistRole, managerRole);
            predicates.add(staffRoles);
            
            // Exclude soft deleted accounts
            predicates.add(cb.equal(root.get("isDeleted"), false));
            
            // Search by full name (firstName + lastName), username, email, or phone
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String search = searchTerm.trim().toLowerCase();
                String pattern = "%" + search + "%";
                // Search in firstName, lastName, username, email, or phone
                Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), pattern);
                Predicate phonePredicate = cb.like(root.get("phone"), pattern);
                Predicate searchOr = cb.or(firstNamePredicate, lastNamePredicate, usernamePredicate, emailPredicate, phonePredicate);
                predicates.add(searchOr);
            }
            
            // Filter by status
            if (status != null && !status.isEmpty() && !"ALL".equals(status.toUpperCase())) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // Pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // Convert to DTO
        List<AccountResponseDto> dtos = userPage.getContent().stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByRole(String role) {
        log.info("Getting accounts by role: {}", role);
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .filter(user -> !user.getIsDeleted()) // Filter out soft deleted accounts
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Integer userId) {
        log.info("Getting account by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + userId));
        return new AccountResponseDto(user);
    }

    @Override
    public AccountResponseDto createAccount(AccountRequestDto accountRequestDto) {
        log.info("Creating new account with username: {}", accountRequestDto.getUsername());

        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(accountRequestDto.getUsername())) {
            throw new RuntimeException("Username đã tồn tại: " + accountRequestDto.getUsername());
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(accountRequestDto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + accountRequestDto.getEmail());
        }

        // Kiểm tra phone đã tồn tại chưa
        if (userRepository.existsByPhoneAndUsernameNot(accountRequestDto.getPhone(), accountRequestDto.getUsername())) {
            throw new RuntimeException("Số điện thoại đã tồn tại: " + accountRequestDto.getPhone());
        }

        User user = buildUserFromRequest(accountRequestDto);
        user.setPassword(passwordEncoder.encode("123456")); // Mật khẩu mặc định

        User savedUser = userRepository.save(user);
        log.info("Created account with ID: {}", savedUser.getUserId());

        return new AccountResponseDto(savedUser);
    }

    @Override
    public AccountResponseDto updateAccount(Integer userId, AccountRequestDto accountRequestDto) {
        log.info("Updating account with ID: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + userId));

        // Kiểm tra username đã tồn tại chưa (trừ user hiện tại)
        if (!existingUser.getUsername().equals(accountRequestDto.getUsername()) &&
            userRepository.existsByUsername(accountRequestDto.getUsername())) {
            throw new RuntimeException("Username đã tồn tại: " + accountRequestDto.getUsername());
        }

        // Kiểm tra email đã tồn tại chưa (trừ user hiện tại)
        if (!existingUser.getEmail().equals(accountRequestDto.getEmail()) &&
            userRepository.existsByEmail(accountRequestDto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + accountRequestDto.getEmail());
        }

        // Kiểm tra phone đã tồn tại chưa (trừ user hiện tại)
        if (!existingUser.getPhone().equals(accountRequestDto.getPhone()) &&
            userRepository.existsByPhoneAndUsernameNot(accountRequestDto.getPhone(), existingUser.getUsername())) {
            throw new RuntimeException("Số điện thoại đã tồn tại: " + accountRequestDto.getPhone());
        }

        // Không cho phép đổi role khi update
        if (!existingUser.getRole().equals(accountRequestDto.getRole())) {
            throw new RuntimeException("Không được phép thay đổi vai trò (role) của tài khoản");
        }

        // Cập nhật thông tin
        existingUser.setUsername(accountRequestDto.getUsername());
        existingUser.setEmail(accountRequestDto.getEmail());
        existingUser.setPhone(accountRequestDto.getPhone());
        existingUser.setFirstName(accountRequestDto.getFirstName());
        existingUser.setLastName(accountRequestDto.getLastName());
        existingUser.setGender(accountRequestDto.getGender());
        existingUser.setDob(accountRequestDto.getDob());
        existingUser.setAddress(accountRequestDto.getAddress());
        // Không cập nhật role - giữ nguyên role hiện tại
        existingUser.setStatus(accountRequestDto.getStatus());
        existingUser.setAvatarUrl(accountRequestDto.getAvatarUrl());

        User updatedUser = userRepository.save(existingUser);
        log.info("Updated account with ID: {}", updatedUser.getUserId());

        return new AccountResponseDto(updatedUser);
    }

    @Override
    public AccountResponseDto toggleAccountStatus(Integer userId) {
        log.info("Toggling account status for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + userId));

        // Toggle status
        if (user.getStatus() == User.Status.ACTIVE) {
            user.setStatus(User.Status.INACTIVE);
        } else {
            user.setStatus(User.Status.ACTIVE);
        }

        User updatedUser = userRepository.save(user);
        log.info("Toggled account status for ID: {} to {}", userId, updatedUser.getStatus());

        return new AccountResponseDto(updatedUser);
    }


    @Override
    public void deleteAccount(Integer userId) {
        log.info("=== BẮT ĐẦU SOFT DELETE TÀI KHOẢN ID: {} ===", userId);
        
        // Kiểm tra user có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + userId));
        
        log.info("Tìm thấy user: {} - {}", user.getUsername(), user.getEmail());
        
        // SOFT DELETE - Set isDeleted = true và status = INACTIVE
        user.setIsDeleted(true);
        user.setStatus(User.Status.INACTIVE);
        userRepository.save(user);
        
        log.info("=== SOFT DELETE THÀNH CÔNG ID: {} - ĐÃ ẨN KHỎI GIAO DIỆN ===", userId);
    }

    @Override
    public void deleteAllTestAccounts(Integer userId) {
        log.info("=== BẮT ĐẦU HARD DELETE TÀI KHOẢN ID: {} ===", userId);
        
        // Kiểm tra user có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + userId));
        
        // Không cho phép xóa ADMIN
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Không được phép xóa tài khoản ADMIN");
        }
        
        log.info("Tìm thấy user: {} - {} - Role: {}", user.getUsername(), user.getEmail(), user.getRole());
        
        // HARD DELETE - Xóa vĩnh viễn
        userRepository.delete(user);
        
        log.info("=== HARD DELETE THÀNH CÔNG ID: {} ===", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User buildUserFromRequest(AccountRequestDto accountRequestDto) {
        User user = new User();
        user.setUsername(accountRequestDto.getUsername());
        user.setEmail(accountRequestDto.getEmail());
        user.setPhone(accountRequestDto.getPhone());
        user.setFirstName(accountRequestDto.getFirstName());
        user.setLastName(accountRequestDto.getLastName());
        user.setGender(accountRequestDto.getGender());
        user.setDob(accountRequestDto.getDob());
        user.setAddress(accountRequestDto.getAddress());
        user.setRole(accountRequestDto.getRole());
        user.setStatus(accountRequestDto.getStatus());
        user.setAvatarUrl(accountRequestDto.getAvatarUrl());
        user.setIsDeleted(false);
        return user;
    }
}
