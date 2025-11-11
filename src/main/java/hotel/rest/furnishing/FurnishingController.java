package hotel.rest.furnishing;

import hotel.db.entity.Furnishing;
import hotel.service.furnishing.FurnishingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/furnishings")
public class FurnishingController {
    
    private final FurnishingService furnishingService;
    
    // List all furnishings with filters and pagination
    @GetMapping
    public String listFurnishings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model) {
        
        // Parse sortBy parameter
        String sortField = "furnishingId";
        String sortDir = "asc";
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] sortParams = sortBy.split(",");
            if (sortParams.length == 2) {
                sortField = sortParams[0];
                sortDir = sortParams[1];
            }
        }
        
        // Create pageable with sorting
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortField).ascending() 
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        
        // Get furnishings with filters
        Page<Furnishing> furnishingsPage;
        if ((search != null && !search.isEmpty()) || minQuantity != null || maxQuantity != null) {
            furnishingsPage = furnishingService.findWithFilters(search, minQuantity, maxQuantity, pageable);
        } else {
            furnishingsPage = furnishingService.getAllFurnishings(pageable);
        }
        
        // Add attributes to model
        model.addAttribute("furnishings", furnishingsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", furnishingsPage.getTotalPages());
        model.addAttribute("totalElements", furnishingsPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("search", search);
        model.addAttribute("minQuantity", minQuantity);
        model.addAttribute("maxQuantity", maxQuantity);
        model.addAttribute("sortBy", sortBy);
        
        return "management/furnishings/furnishing-list";
    }
    
    // Show create form
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("furnishing", new Furnishing());
        model.addAttribute("isEdit", false);
        return "management/furnishings/furnishing-form";
    }
    
    // Show edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Furnishing furnishing = furnishingService.getFurnishingById(id);
        if (furnishing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nội thất với ID: " + id);
            return "redirect:/management/furnishings";
        }
        model.addAttribute("furnishing", furnishing);
        model.addAttribute("isEdit", true);
        return "management/furnishings/furnishing-form";
    }
    
    // Save furnishing (create or update)
    @PostMapping("/save")
    public String saveFurnishing(@ModelAttribute("furnishing") Furnishing furnishing,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        // Validation: Check if name is empty
        if (furnishing.getName() == null || furnishing.getName().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Tên nội thất không được để trống!");
            model.addAttribute("furnishing", furnishing);
            model.addAttribute("isEdit", furnishing.getFurnishingId() != null);
            return "management/furnishings/furnishing-form";
        }
        
        // Validation: Check if quantity is valid
        if (furnishing.getQuantity() == null || furnishing.getQuantity() < 0) {
            model.addAttribute("errorMessage", "Số lượng phải lớn hơn hoặc bằng 0!");
            model.addAttribute("furnishing", furnishing);
            model.addAttribute("isEdit", furnishing.getFurnishingId() != null);
            return "management/furnishings/furnishing-form";
        }
        
        // Validation: Check duplicate name
        if (furnishing.getFurnishingId() == null) {
            // CREATE: check if name exists
            if (furnishingService.existsByName(furnishing.getName())) {
                model.addAttribute("errorMessage", "Tên nội thất này đã tồn tại!");
                model.addAttribute("furnishing", furnishing);
                model.addAttribute("isEdit", false);
                return "management/furnishings/furnishing-form";
            }
        } else {
            // UPDATE: check if name exists except itself
            if (furnishingService.existsByNameExceptItself(furnishing.getName(), furnishing.getFurnishingId())) {
                model.addAttribute("errorMessage", "Tên nội thất này đã tồn tại!");
                model.addAttribute("furnishing", furnishing);
                model.addAttribute("isEdit", true);
                return "management/furnishings/furnishing-form";
            }
        }
        
        // Save furnishing
        furnishingService.saveFurnishing(furnishing);
        redirectAttributes.addFlashAttribute("successMessage", 
            furnishing.getFurnishingId() == null 
                ? "Thêm nội thất thành công!" 
                : "Cập nhật nội thất thành công!");
        
        return "redirect:/management/furnishings";
    }
    
    // Delete furnishing (soft delete)
    @GetMapping("/delete/{id}")
    public String deleteFurnishing(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Furnishing furnishing = furnishingService.getFurnishingById(id);
        if (furnishing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nội thất với ID: " + id);
            return "redirect:/management/furnishings";
        }
        
        furnishingService.deleteFurnishing(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nội thất thành công!");
        return "redirect:/management/furnishings";
    }
    
    // View furnishing details
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Furnishing furnishing = furnishingService.getFurnishingById(id);
        if (furnishing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nội thất với ID: " + id);
            return "redirect:/management/furnishings";
        }
        model.addAttribute("furnishing", furnishing);
        return "management/furnishings/furnishing-detail";
    }
}
