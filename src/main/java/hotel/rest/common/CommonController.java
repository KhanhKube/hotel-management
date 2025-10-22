package hotel.rest.common;

import hotel.db.dto.user.UserLoginDto;
import hotel.db.dto.user.UserProfileDto;
import hotel.db.dto.user.UserRegisterDto;
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

                    model.addAttribute("message", "Welcome " + user.getFirstName());
                    redirectAttrs.addFlashAttribute("message", "Welcome " + user.getFirstName());
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
    public String register(@ModelAttribute("userRegister") UserRegisterDto dto, Model model) {
        MessageResponse response = commonService.registerUser(dto);
        if (response.isSuccess()) {
            model.addAttribute("message", response.getMessage());
            return "redirect:/hotel/login";
        } else {
            model.addAttribute("error", response.getMessage());
            return "common/register";
        }
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
            User user = commonService.getUserByUsername(dto.getUsername());
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

            MessageResponse response = commonService.updateAvatar(user.getUsername(), file);

            if (response.isSuccess()) {
                User updatedUser = commonService.getUserByUsername(user.getUsername());
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

}