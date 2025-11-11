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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class PublicServiceController {

    private final ServiceService serviceService;

    // Display all services page with filters and pagination
    @GetMapping
    public String servicesList(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "serviceId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        // Create pageable with sorting
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get services with filters
        Page<Service> servicesPage;
        if (!search.isEmpty() || minPrice != null || maxPrice != null) {
            servicesPage = serviceService.findWithFilters(search, minPrice, maxPrice, pageable);
        } else {
            servicesPage = serviceService.findAll(pageable);
        }
        
        // Add attributes to model
        model.addAttribute("servicesPage", servicesPage);
        model.addAttribute("services", servicesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicesPage.getTotalPages());
        model.addAttribute("totalItems", servicesPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "common/services";
    }

    // Display service details
    @GetMapping("/{id}")
    public String serviceDetail(@PathVariable Integer id, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        Service service = serviceService.findById(id);
        if (service == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy dịch vụ với ID: " + id);
            return "redirect:/services";
        }
        model.addAttribute("service", service);
        return "common/service-detail";
    }

    // Get featured services for homepage
    @GetMapping("/featured")
    public String getFeaturedServices(Model model) {
        List<Service> featuredServices = serviceService.findFeaturedServices();
        model.addAttribute("featuredServices", featuredServices);
        return "common/services :: featured-services";
    }
}
