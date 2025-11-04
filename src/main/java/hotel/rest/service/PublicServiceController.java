package hotel.rest.service;

import hotel.db.entity.Service;
import hotel.service.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class PublicServiceController {

    private final ServiceService serviceService;

    // Display all services page
    @GetMapping
    public String servicesList(Model model) {
        List<Service> services = serviceService.findAll();
        model.addAttribute("services", services);
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
