package hotel.rest.common;

import hotel.db.dto.user.UserLoginDto;
import hotel.db.dto.user.UserProfileDto;
import hotel.db.dto.user.UserRegisterDto;
import hotel.db.dto.user.VerifyOtpDto;
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

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "common/dashboard";
        }
        return "common/login";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "common/home";
        }
        UserLoginDto userLoginDto = (UserLoginDto) model.getAttribute("userLogin");
        if(userLoginDto == null) {
            model.addAttribute("userLogin", new UserLoginDto());
            return "common/login";
        }
        model.addAttribute("userLogin", userLoginDto);
        return "common/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
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
					// Lưu user vào session
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
					if (!user.getRole().equals(CUSTOMER)) {
						return "redirect:/hotel/dashboard";
					}
					session.setAttribute("userId", user.getUserId());

					// Kiểm tra nếu có trang cần quay lại
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
            verifyOtpDto.setUsername(dto.getUsername());
            verifyOtpDto.setPhoneNumber(dto.getPhone());
            verifyOtpDto.setOtp(null);
            redirectAttrs.addFlashAttribute("verifyDto", verifyOtpDto);
            return "redirect:/hotel/verify";
        } else {
            model.addAttribute("error", response.getMessage());
            return "common/register";
        }
    }

    @GetMapping("/verify")
    public String showOtpForm(Model model) {
        VerifyOtpDto verifyOtpDto = (VerifyOtpDto) model.getAttribute("verifyDto");
        model.addAttribute("verifyDto", verifyOtpDto);
        return "common/verify-otp";
    }

    @PostMapping("/verify")
    public String verifyOtp(@ModelAttribute("verifyDto") VerifyOtpDto dto,
                            Model model,
                            RedirectAttributes redirectAttrs) {
        MessageResponse response = commonService.verifyOtp(dto);
        if (response.isSuccess()) {
            model.addAttribute("message", "Xác thực thành công! Hãy đăng nhập.");
            UserLoginDto userLoginDto = new UserLoginDto(dto.getUsername(), null);
            redirectAttrs.addFlashAttribute("userLogin", userLoginDto);
            return "redirect:/hotel/login";
        } else {
            model.addAttribute("error", response.getMessage());
            return "common/verify-otp";
        }
    }

    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, Model model) {
        MessageResponse response = commonService.resendOtp(email);
        model.addAttribute(response.isSuccess() ? "message" : "error", response.getMessage());
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
            } else {
                redirectAttributes.addFlashAttribute("error", response.getMessage());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/hotel/profile";
    }

    // Show profile
    @GetMapping("/change-password")
    public String changePassword(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }
        UserProfileDto userProfileDto = commonService.userToUserProfile(user);
        model.addAttribute("userProfile", userProfileDto);
        return "common/profile";
    }
}