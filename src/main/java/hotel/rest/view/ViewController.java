package hotel.rest.view;

import hotel.db.entity.View;
import hotel.service.view.ViewService;
import hotel.util.BaseController;
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
    public String listViews(Model model) {
        List<View> views = viewService.getAllViews();
//        model.addAttribute("views", views);
//        return "management/view/view-list";
            return renderBuilder("management/view/view-list").with("views",views).build(model);
    }

    // Form thêm mới
    @GetMapping("/view/new")
    public String createViewForm(Model model) {
        return renderBuilder("management/view/form")
                .with("view", new View())
                .build(model);
    }

    // Lưu view (tạo mới hoặc cập nhật)
    @PostMapping("/view/new")
    public String saveView(@ModelAttribute("view") View view) {
        viewService.saveView(view);
        return "redirect:/hotel-management/view";
    }

    // Form sửa
    @GetMapping("/view/edit/{id}")
    public String editView(@PathVariable Integer id, Model model) {
        model.addAttribute("view", viewService.getViewById(id));
        return "management/view/form";
    }

    // Xóa
    @GetMapping("/view/delete/{id}")
    public String deleteView(@PathVariable Integer id) {
        viewService.deleteView(id);
        return "redirect:/hotel-management/view";
    }
}
