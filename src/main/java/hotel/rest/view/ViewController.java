package hotel.rest.view;

import hotel.db.entity.User;
import hotel.db.entity.View;
import hotel.service.view.ViewService;
import hotel.util.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class ViewController extends BaseController {

    private final ViewService viewService;

    // Hiển thị danh sách
    @GetMapping("/view")
    public String listViews(HttpSession session, Model model) {
        List<View> views = viewService.getAllViews();
        model.addAttribute("views", views);
        return "management/view/view-list1";
    }

    // Form thêm mới
    @GetMapping("/view/new")
    public String createViewForm(Model model) {
        return renderBuilder("management/view/view-form")
                .with("view", new View())
                .build(model);
    }

    // Lưu view mới
    @PostMapping("/view/save")
    public String saveView(@ModelAttribute("view") View view) {
        viewService.saveView(view);
        return "redirect:/hotel-management/view?success=add";
    }

    // Form sửa
    @GetMapping("/view/edit/{id}")
    public String editView(@PathVariable Integer id, Model model) {
        model.addAttribute("view", viewService.getViewById(id));
        return "management/view/view-form";
    }

    // Cập nhật view
    @PostMapping("/view/update/{id}")
    public String updateView(@PathVariable Integer id, @ModelAttribute("view") View view) {
        View existingView = viewService.getViewById(id);
        existingView.setViewType(view.getViewType());
        viewService.saveView(existingView);
        return "redirect:/hotel-management/view?success=edit";
    }

    // Xóa view (hard delete)
    @PostMapping("/view/delete/{id}")
    public String deleteView(@PathVariable Integer id) {
        try {
            viewService.deleteView(id);
            return "redirect:/hotel-management/view?success=delete";
        } catch (Exception e) {
            return "redirect:/hotel-management/view?error=delete";
        }
    }
}
