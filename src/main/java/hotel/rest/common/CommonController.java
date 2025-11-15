package hotel.rest.common;

import hotel.db.dto.user.*;
import hotel.db.entity.User;
import hotel.service.common.CommonService;
import hotel.util.MessageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class CommonController {


    private final CommonService commonService;

    @GetMapping("/header")
    public String header() {
        return "common/header";
    }

    @GetMapping("/footer")
    public String footer() {
        return "common/footer";
    }

    @GetMapping("/sidebar")
    public String sidebar(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        if (user != null) {
            return "common/sidebar";
        }
        return "common/login";
    }

    @RequestMapping("/**")
    public String handleInvalidHotelUrl() {
        return "error/404";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if(user.getRole().equals(STAFF) || user.getRole().equals(RECEPTIONIST)) {
            return "redirect:/hotel-management/checking";
        }
        model.addAllAttributes(commonService.getDashboardData());
        return "common/dashboard";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "common/home";
        }
        UserLoginDto userLoginDto = (UserLoginDto) model.getAttribute("userLogin");
        if (userLoginDto == null) {
            model.addAttribute("userLogin", new UserLoginDto());
            return "common/login";
        }
        model.addAttribute("userLogin", userLoginDto);
        return "common/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "common/home";
        }
        commonService.logout(user.getEmail());
        session.invalidate();
        return "redirect:/hotel/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") UserLoginDto formUser,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttrs) {
        return commonService.login(formUser.getUsername(), formUser.getPassword())
                .map(user -> {
                    session.setAttribute("user", user);
                    if (!user.getOtpVerified()) {
                        commonService.resendOtp(user.getEmail());
                        VerifyOtpDto verifyOtpDto = new VerifyOtpDto();
                        verifyOtpDto.setEmail(user.getEmail());
                        verifyOtpDto.setUsername(user.getUsername());
                        verifyOtpDto.setPhoneNumber(user.getPhone());
                        verifyOtpDto.setOtp(null);
                        redirectAttrs.addFlashAttribute("verifyDto", verifyOtpDto);
                        return "redirect:/hotel/verify";
                    }
                    if (user.getRole().equals(ADMIN) || user.getRole().equals(MANAGER)) {
                        return "redirect:/hotel/dashboard";
                    }
                    if(user.getRole().equals(STAFF) || user.getRole().equals(RECEPTIONIST)) {
                        return "redirect:/hotel-management/checking";
                    }
                    session.setAttribute("userId", user.getUserId());

                    String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
                    if (redirectUrl != null) {
                        session.removeAttribute("redirectAfterLogin");
                        return "redirect:" + redirectUrl;
                    }

                    model.addAttribute("message", "Welcome " + user.getFirstName());
                    redirectAttrs.addFlashAttribute("message", "Xin chào " + user.getFirstName());
                    return "redirect:/hotel";
                })
                .orElseGet(() -> {
                    redirectAttrs.addFlashAttribute("error", LOGININVALID);
                    redirectAttrs.addFlashAttribute("userLogin", formUser);
                    return "redirect:/hotel/login";
                });
    }

    // Hiển thị form register
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userRegister", new UserRegisterDto());
        return "common/register";
    }

    // Submit form register
    @PostMapping("/register")
    public String register(@ModelAttribute("userRegister") UserRegisterDto dto,
                           Model model,
                           RedirectAttributes redirectAttrs) {
        MessageResponse response = commonService.registerUser(dto);
        if (response.isSuccess()) {
            model.addAttribute("message", response.getMessage());
            VerifyOtpDto verifyOtpDto = new VerifyOtpDto();
            verifyOtpDto.setEmail(dto.getEmail());
            verifyOtpDto.setPhoneNumber(dto.getPhone());
            verifyOtpDto.setOtp(null);
            redirectAttrs.addFlashAttribute("message", response.getMessage());
            redirectAttrs.addFlashAttribute("verifyDto", verifyOtpDto);
            return "redirect:/hotel/verify";
        } else {
            model.addAttribute("error", response.getMessage());
            return "common/register";
        }
    }

    @GetMapping("/verify")
    public String showOtpForm(Model model,
                              HttpSession session) {
        VerifyOtpDto verifyOtpDto = (VerifyOtpDto) model.getAttribute("verifyDto");
        assert verifyOtpDto != null;
        session.setAttribute("email", verifyOtpDto.getEmail());
        model.addAttribute("verifyDto", verifyOtpDto);
        return "common/verify-otp";
    }

    @PostMapping("/verify")
    public String verifyOtp(@ModelAttribute("verifyDto") VerifyOtpDto dto,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        MessageResponse response = commonService.verifyOtp(dto);
        if (response.isSuccess()) {
            UserLoginDto userLoginDto = new UserLoginDto(dto.getEmail(), null);
            redirectAttrs.addFlashAttribute("message", "Xác thực thành công! Hãy đăng nhập.");
            redirectAttrs.addFlashAttribute("userLogin", userLoginDto);
            return "redirect:/hotel/login";
        } else {
            model.addAttribute("error", response.getMessage());
            return "common/verify-otp";
        }
    }

    @GetMapping("/resend-otp")
    public String resendOtp(Model model,
                            HttpSession session) {
        String email = (String) session.getAttribute("email");
        MessageResponse response = commonService.resendOtp(email);
        model.addAttribute(response.isSuccess() ? "message" : "error", response.getMessage());
        VerifyOtpDto verifyOtpDto = new VerifyOtpDto();
        verifyOtpDto.setEmail(email);
        model.addAttribute("verifyDto", verifyOtpDto);
        return "common/verify-otp";
    }

    // Show profile
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }
        UserProfileDto userProfileDto = commonService.userToUserProfile(user);
        model.addAttribute("userProfile", userProfileDto);
        return "common/profile";
    }

    // Show form edit user profile
    @GetMapping("/edit-profile")
    public String editProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }
        if (!user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        UserProfileDto dto = commonService.userToUserProfile(user);
        model.addAttribute("userProfileDto", dto);
        return "common/edit-profile";
    }

    // Submit form user profile
    @PostMapping("/edit-profile")
    public String updateProfile(@ModelAttribute("user") UserProfileDto dto,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttrs) {
        User userSession = (User) session.getAttribute("user");
        if (userSession == null) {
            return "redirect:/hotel";
        }
        if (!userSession.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }

        MessageResponse response = commonService.editUserProfile(dto);
        if (response.isSuccess()) {
            User user = commonService.getUserByPhoneOrEmail(dto.getPhone());
            session.setAttribute("user", user);
            model.addAttribute("message", response.getMessage());
            redirectAttrs.addFlashAttribute("message", response.getMessage());
            return "redirect:/hotel/profile";
        } else {
            model.addAttribute("error", response.getMessage());
            redirectAttrs.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel/edit-profile";
        }
    }

    @PostMapping("/update-avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", LOGINFIRST);
                return "redirect:/login";
            }

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", CHOOSEPICTURE);
                return "redirect:/hotel/profile";
            }

            MessageResponse response = commonService.updateAvatar(user.getPhone(), file);

            if (response.isSuccess()) {
                User updatedUser = commonService.getUserByPhoneOrEmail(user.getPhone());
                if (user == null) {
                    return "redirect:/login";
                }
                session.setAttribute("user", updatedUser);

                redirectAttributes.addFlashAttribute("success", response.getMessage());
                return "redirect:/hotel/profile";
            } else {
                redirectAttributes.addFlashAttribute("error", response.getMessage());
                return "redirect:/hotel/profile";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/hotel/profile";
    }

    @GetMapping("/change-password")
    public String changePassword(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setUsername(user.getEmail());
        model.addAttribute("changePasswordDto", dto);
        return "common/change-password";

    }

    @PostMapping("/change-password")
    public String changePasswordConfirm(
            @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttrs) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        MessageResponse response = commonService.changePassword(dto);

        if (!response.isSuccess()) {
            model.addAttribute("error", response.getMessage());
            return "common/change-password";
        }
        User userNew = commonService.getUserByPhoneOrEmail(user.getEmail());
        session.setAttribute("user", userNew);
        redirectAttrs.addFlashAttribute("message", response.getMessage());
        return "redirect:/hotel/profile";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(HttpSession session, Model model) {
        return "common/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendForgotPasswordOtp(@RequestParam("email") String email,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần nhập email để gửi mật khẩu!");
            return "redirect:/hotel/forgot-password";
        }

        MessageResponse response = commonService.forgotPassword(email);
        if (!response.isSuccess()) {
            redirectAttributes.addFlashAttribute("error", response.getMessage());
            return "redirect:/hotel/forgot-password";
        }
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername(email);
        redirectAttributes.addFlashAttribute("userLogin", loginDto);

        redirectAttributes.addFlashAttribute("success", response.getMessage());

        return "redirect:/hotel/login";
    }
}