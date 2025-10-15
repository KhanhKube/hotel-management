package hotel.rest.service;


import hotel.db.entity.Hotel;
import hotel.db.entity.Service;
import hotel.db.repository.hoteldetail.HotelRepository;
import hotel.service.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management/hotel/{hotelId}/services")
public class ServiceController {

    private final ServiceService serviceService;
    private final HotelRepository hotelRepository;

    // List all services with pagination, search, and filters
    @GetMapping
    public String listServices(@PathVariable Long hotelId,
                              @RequestParam(required = false) String name,
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

        Page<Service> services = serviceService.getServicesWithFilters(hotelId, name, minPrice, maxPrice, pageable);

        model.addAttribute("services", services);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        model.addAttribute("name", name);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "management/hotel_service/list";
    }

    // View service details
    @GetMapping("/view/{id}")
    public String viewService(@PathVariable Long hotelId, @PathVariable Integer id, Model model) {
        Service service = serviceService.getById(id);
        if (service != null) {
            model.addAttribute("service", service);
            model.addAttribute("hotelId", hotelId);
            return "management/hotel_service/view";
        } else {
            return "redirect:/management/hotel/" + hotelId + "/services";
        }
    }

    // Form thêm dịch vụ
    @GetMapping("/add")
    public String showAddForm(@PathVariable Long hotelId, Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("hotelId", hotelId);
        return "management/hotel_service/form";
    }

    //  Lưu dịch vụ mới
    @PostMapping("/save")
    public String saveService(@PathVariable Long hotelId, @ModelAttribute("service") Service service) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        service.setHotel(hotel);
        serviceService.save(service);
        return "redirect:/management/hotel/" + hotelId + "/services";
    }

    //  Form sửa
    @GetMapping("/edit/{id}")
    public String editService(@PathVariable Long hotelId, @PathVariable Integer id, Model model) {
        model.addAttribute("service", serviceService.getById(id));
        model.addAttribute("hotelId", hotelId);
        return "management/hotel_service/form";
    }

    //  Xóa
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long hotelId, @PathVariable Integer id) {
        serviceService.deleteById(id);
        return "redirect:/management/hotel/" + hotelId + "/services";
    }
}
