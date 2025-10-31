package hotel.rest.view;

import hotel.db.entity.View;
import hotel.service.view.ViewService;
import hotel.service.common.ViewImageService;
import hotel.util.BaseController;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
@Slf4j
public class ViewController extends BaseController {

    private final ViewService viewService;
    private final ViewImageService viewImageService;

    // Hiển thị danh sách
    @GetMapping("/view")
    public String listViews(HttpSession session, Model model) {
        List<View> views = viewService.getAllViews();
        // Add image paths to each view
        for (View view : views) {
            viewImageService.getImageForView(view.getViewId());
            // We'll add this to the model as a separate attribute
        }
        model.addAttribute("views", views);
        model.addAttribute("viewImageService", viewImageService);
        return "management/view/view-list-card";
    }

    // Form thêm mới
    @GetMapping("/view/new")
    public String createViewForm(Model model) {
        model.addAttribute("view", new View());
        model.addAttribute("viewImageService", viewImageService);
        return "management/view/view-form";
    }

    // GET method để xử lý khi truy cập trực tiếp vào /view/save
    @GetMapping("/view/save")
    public String saveViewGet() {
        log.info("GET request to /view/save - redirecting to form");
        return "redirect:/hotel-management/view/new";
    }

    // Lưu view mới
    @PostMapping("/view/save")
    public String saveView(@ModelAttribute("view") View view, 
                          @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                          Model model) {
        log.info("Received request to save view: {}", view.getViewType());
        try {
            // Save view first to get the ID
            View savedView = viewService.saveView(view);
            log.info("View saved successfully with ID: {}", savedView.getViewId());
            
            // Upload image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imagePath = viewImageService.uploadImageForView(savedView.getViewId(), imageFile);
                    log.info("Image uploaded for new view {}: {}", savedView.getViewId(), imagePath);
                    // Force refresh image mappings to ensure latest image is used
                    viewImageService.refreshImageMappings();
                } catch (Exception imageError) {
                    log.error("Error uploading image, but view was saved: {}", imageError.getMessage());
                }
            }
            
            log.info("Redirecting to view list");
            // Add timestamp to prevent browser cache
            return "redirect:/hotel-management/view";
        } catch (IllegalArgumentException e) {
            log.warn("Validation error when saving view: {}", e.getMessage());
            // Hiển thị error ngay trên màn hình
            model.addAttribute("error", "duplicate");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("view", view); // Giữ lại data người dùng đã nhập
            model.addAttribute("viewImageService", viewImageService);
            return "management/view/view-form";
        } catch (Exception e) {
            log.error("Error saving view", e);
            model.addAttribute("error", "upload");
            model.addAttribute("message", "Lỗi khi tạo view. Vui lòng thử lại.");
            model.addAttribute("view", view);
            model.addAttribute("viewImageService", viewImageService);
            return "management/view/view-form";
        }
    }

    // Xem chi tiết view
    @GetMapping("/view/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model) {
        View view = viewService.getViewById(id);
        model.addAttribute("view", view);
        model.addAttribute("viewImageService", viewImageService);
        return "management/view/view-detail";
    }

    // Form sửa
    @GetMapping("/view/edit/{id}")
    public String editView(@PathVariable Integer id, Model model) {
        View view = viewService.getViewById(id);
        model.addAttribute("view", view);
        model.addAttribute("viewImageService", viewImageService);
        return "management/view/view-form";
    }

    // Cập nhật view
    @PostMapping("/view/update/{id}")
    public String updateView(@PathVariable Integer id, 
                           @ModelAttribute("view") View view,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           Model model,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            View existingView = viewService.getViewById(id);
            existingView.setViewType(view.getViewType());
            
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = viewImageService.uploadImageForView(id, imageFile);
                log.info("Image updated for view {}: {}", id, imagePath);
                // Force refresh image mappings to ensure latest image is used
                viewImageService.refreshImageMappings();
            }
            
            viewService.saveView(existingView);
            // Redirect back to edit page and show success message via flash attribute
            redirectAttributes.addFlashAttribute("message", "View đã được cập nhật thành công!");
            return "redirect:/hotel-management/view/edit/" + id;
        } catch (IllegalArgumentException e) {
            log.warn("Validation error when updating view: {}", e.getMessage());
            // Hiển thị error ngay trên màn hình
            model.addAttribute("error", "duplicate");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("view", view); // Giữ lại data người dùng đã nhập
            model.addAttribute("viewImageService", viewImageService);
            return "management/view/view-form";
        } catch (Exception e) {
            log.error("Error updating view with image", e);
            model.addAttribute("error", "upload");
            model.addAttribute("message", "Lỗi khi cập nhật view. Vui lòng thử lại.");
            model.addAttribute("view", view);
            model.addAttribute("viewImageService", viewImageService);
            return "management/view/view-form";
        }
    }

    // Xóa view (hard delete) - GET method
    @GetMapping("/view/delete/{id}")
    public String deleteView(@PathVariable Integer id) {
        try {
            // Check if view exists first
            viewService.getViewById(id);
            
            // Delete associated image first (even if no image exists)
            boolean imageDeleted = viewImageService.deleteImageForView(id);
            log.info("Image deletion result for view {}: {}", id, imageDeleted);
            
            // Then delete the view
            viewService.deleteView(id);
            log.info("View {} deleted successfully", id);
            
            return "redirect:/hotel-management/view?success=delete";
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                log.warn("View {} not found for deletion", id);
                return "redirect:/hotel-management/view?error=notfound";
            } else {
                log.error("Error deleting view {}", id, e);
                return "redirect:/hotel-management/view?error=delete";
            }
        } catch (Exception e) {
            log.error("Unexpected error deleting view {}", id, e);
            return "redirect:/hotel-management/view?error=delete";
        }
    }

    // Khôi phục view đã xóa
    @GetMapping("/view/restore/{id}")
    public String restoreView(@PathVariable Integer id) {
        try {
            viewService.restoreView(id);
            log.info("View {} restored successfully", id);
            return "redirect:/hotel-management/view?success=restore";
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                log.warn("View {} not found for restoration", id);
                return "redirect:/hotel-management/view?error=notfound";
            } else {
                log.error("Error restoring view {}", id, e);
                return "redirect:/hotel-management/view?error=restore";
            }
        } catch (Exception e) {
            log.error("Unexpected error restoring view {}", id, e);
            return "redirect:/hotel-management/view?error=restore";
        }
    }
    
    

    // Delete all views (soft delete all)
    @GetMapping("/view/delete-all")
    public String deleteAllViews() {
        try {
            viewService.deleteAllViews();
            // Refresh images mapping as many views now hidden
            viewImageService.refreshImageMappings();
            return "redirect:/hotel-management/view?success=deleteall";
        } catch (Exception e) {
            log.error("Error deleting all views", e);
            return "redirect:/hotel-management/view?error=deleteall";
        }
    }

    // Restore all soft-deleted views
    @GetMapping("/view/restore-all")
    public String restoreAllViews() {
        try {
            viewService.restoreAllViews();
            viewImageService.refreshImageMappings();
            return "redirect:/hotel-management/view?success=restoreall";
        } catch (Exception e) {
            log.error("Error restoring all views", e);
            return "redirect:/hotel-management/view?error=restoreall";
        }
    }

}
