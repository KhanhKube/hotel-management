package hotel.rest.service;

import hotel.db.entity.Service;
import hotel.service.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/services")
public class ServiceController {

    private final ServiceService serviceService;

    // List all serv23wsices with pagination, search, and filters
    @GetMapping
    public String listServices(@RequestParam(required = false) String name,
                               @RequestParam(required = false) Double minPrice,
                               @RequestParam(required = false) Double maxPrice,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "serviceId") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               Model model) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Service> services;
        if (name != null && !name.trim().isEmpty()) {
            services = serviceService.searchByName(name, pageable);
        } else if (minPrice != null || maxPrice != null) {
            services = serviceService.filterByPrice(minPrice, maxPrice, pageable);
        } else {
            services = serviceService.findAll(pageable);
        }

        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        model.addAttribute("totalItems", services.getTotalElements());
        model.addAttribute("name", name);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "management/services/service-list";
    }

    // View service details
    @GetMapping("/view/{id}")
    public String viewService(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Service service = serviceService.findById(id);
        if (service != null) {
            model.addAttribute("service", service);
            return "management/services/service-details";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy dịch vụ với ID: " + id);
            return "redirect:/management/services";
        }
    }

    // Form thêm dịch vụ
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("service", new Service());
        return "management/services/service-form";
    }

    // Lưu dịch vụ mới
    @PostMapping("/save")
    public String saveService(@ModelAttribute("service") Service service, RedirectAttributes redirectAttributes) {
        try {
            if (service.getServiceId() == null && serviceService.existsByName(service.getServiceName())) {
                redirectAttributes.addFlashAttribute("error", "Tên dịch vụ đã tồn tại!");
                return "redirect:/management/services/add";
            }

            if (service.getStatus() == null || service.getStatus().isEmpty()) {
                service.setStatus("Active");
            }

            serviceService.save(service);
            redirectAttributes.addFlashAttribute("message",
                    service.getServiceId() == null ? "Thêm dịch vụ thành công!" : "Cập nhật dịch vụ thành công!");
            return "redirect:/management/services";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return service.getServiceId() == null
                    ? "redirect:/management/services/add"
                    : "redirect:/management/services/edit/" + service.getServiceId();
        }
    }


    // Form sửa
    @GetMapping("/edit/{id}")
    public String editService(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Service service = serviceService.findById(id);
        if (service != null) {
            model.addAttribute("service", service);
            return "management/services/service-form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy dịch vụ với ID: " + id);
            return "redirect:/management/services";
        }
    }

    // Xóa dịch vụ (soft delete)
    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Service service = serviceService.findById(id);
            if (service != null) {
                serviceService.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Xóa dịch vụ thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy dịch vụ với ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa dịch vụ: " + e.getMessage());
        }
        return "redirect:/management/services";
    }
}
