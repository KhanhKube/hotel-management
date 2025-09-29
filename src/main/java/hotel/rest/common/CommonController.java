package hotel.rest.common;

import hotel.db.dto.user.UserLoginDto;
import hotel.db.entity.User;
import hotel.dto.request.UserLoginDto;
import hotel.dto.request.UserRegisterDto;
import hotel.dto.response.MessageResponse;
import hotel.service.common.CommonService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping({"/","", "/home"})
    public String home(Model model) {
        return "common/home";
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model) {
        model.addAttribute("userLogin", new UserLoginDto());
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "common/home";
        }
        return "common/login";
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
                    model.addAttribute("error", "Invalid username or password");
                    return "common/login";
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
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }
        model.addAttribute("user", user);
        return "redirect:/hotel/home";
    }
}
