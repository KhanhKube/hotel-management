package hotel.rest.size;

import hotel.db.entity.Size;
import hotel.service.size.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    // Hiển thị danh sách
    @GetMapping("/size")
    public String listSizes(Model model) {
        List<Size> sizes = sizeService.getAllSizes();
        model.addAttribute("sizes", sizes);
        return "management/size/size-list";
    }

    // Form thêm mới
    @GetMapping("/size/new")
    public String createSizeForm(Model model) {
        model.addAttribute("size", new Size());
        return "management/size/form";
    }

    // Lưu size (tạo mới hoặc cập nhật)
    @PostMapping("/size/new")
    public String saveSize(@ModelAttribute("size") Size size) {
        try {
            sizeService.saveSize(size);
            return "redirect:/hotel-management/size?message=Size saved successfully";
        } catch (Exception e) {
            return "redirect:/hotel-management/size?error=" + e.getMessage();
        }
    }

    // Form sửa
    @GetMapping("/size/edit/{id}")
    public String editSize(@PathVariable Integer id, Model model) {
        try {
            model.addAttribute("size", sizeService.getSizeById(id));
            return "management/size/form";
        } catch (Exception e) {
            return "redirect:/hotel-management/size?error=" + e.getMessage();
        }
    }

    // Xóa
    @GetMapping("/size/delete/{id}")
    public String deleteSize(@PathVariable Integer id) {
        try {
            sizeService.deleteSize(id);
            return "redirect:/hotel-management/size?message=Size deleted successfully";
        } catch (Exception e) {
            return "redirect:/hotel-management/size?error=" + e.getMessage();
        }
    }
}
