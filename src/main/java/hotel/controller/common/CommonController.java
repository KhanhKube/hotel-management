package hotel.controller.common;

import hotel.dto.request.UserLoginDto;
import hotel.service.common.CommonService;
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
    public String loginForm(Model model) {
        model.addAttribute("userLogin", new UserLoginDto());
        return "common/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") UserLoginDto formUser, Model model) {
        return commonService.login(formUser.getUsername(), formUser.getPassword())
                .map(user -> {
                    model.addAttribute("message", "Welcome " + user.getFirstName());
                    return "common/home"; // success page
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid username or password");
                    return "common/login"; // reload login page with error
                });
    }
}
