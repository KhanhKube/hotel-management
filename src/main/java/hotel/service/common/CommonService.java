package hotel.service.common;

import hotel.db.dto.user.ChangePasswordDto;
import hotel.db.dto.user.UserRegisterDto;
import hotel.db.dto.user.UserProfileDto;
import hotel.db.dto.user.VerifyOtpDto;
import hotel.db.entity.User;

import hotel.util.MessageResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public interface CommonService {

    User getUserByPhoneOrEmail(String request);

    Optional<User> login(String request, String password);

    void logout(String request);

    MessageResponse registerUser(UserRegisterDto userRegisterDto);

    MessageResponse editUserProfile(UserProfileDto dto);

    UserProfileDto userToUserProfile(User user);

    MessageResponse updateAvatar(String phone, MultipartFile file) throws IOException;

    MessageResponse verifyOtp(VerifyOtpDto dto);

    MessageResponse resendOtp(String email);

    MessageResponse changePassword(ChangePasswordDto dto);

    MessageResponse forgotPassword(String email);

    Map<String, Object> getDashboardData();
    
    void sendCancellationEmail(String toEmail, String fullName, String roomNumber, 
                               String startDate, String endDate, String reason);
}
