package hotel.controller.common;

import hotel.db.entity.User;
import hotel.dto.request.UserLoginDto;
import hotel.service.common.CommonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

@RequestMapping("/hotel")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/header")
    public String header() {
        return "common/header";
    }

    @GetMapping("/footer")
    public String footer() {
        return "common/footer";
    }

    @GetMapping()
    public String home() {
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
                        HttpSession session) {
        return commonService.login(formUser.getUsername(), formUser.getPassword())
                .map(user -> {
                    // Lưu user vào session
                    session.setAttribute("user", user);

                    model.addAttribute("message", "Welcome " + user.getFirstName());
                    return "common/home";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid username or password");
                    return "common/login";
                });
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "common/login";
        }
        model.addAttribute("user", user);
        return "common/home";
    }
}
