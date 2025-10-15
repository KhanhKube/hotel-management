package hotel.service.service;


import hotel.db.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceService {
    List<Service> getServicesByHotel(Long hotelId);
    Page<Service> getServicesByHotelWithPagination(Long hotelId, Pageable pageable);
    Page<Service> searchServicesByName(Long hotelId, String name, Pageable pageable);
    Page<Service> filterServicesByPrice(Long hotelId, Double minPrice, Double maxPrice, Pageable pageable);
    Page<Service> getServicesWithFilters(Long hotelId, String name, Double minPrice, Double maxPrice, Pageable pageable);
    Service getById(Integer id);
    Service save(Service service);
    void deleteById(Integer id);
}