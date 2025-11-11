package hotel.service.service;


import hotel.db.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceService {
    // Basic CRUD operations
    List<Service> findAll();
    Page<Service> findAll(Pageable pageable);
    Service findById(Integer id);
    Service save(Service service);
    void deleteById(Integer id);

    // Search and filter operations
    Page<Service> searchByName(String name, Pageable pageable);
    Page<Service> filterByPrice(Double minPrice, Double maxPrice, Pageable pageable);
    Page<Service> findWithFilters(String name, Double minPrice, Double maxPrice, Pageable pageable);

    // Business logic methods
    boolean existsByName(String serviceName);
    List<Service> findByIds(List<Integer> serviceIds);

    // Public service methods
    List<Service> findFeaturedServices();
    List<Service> findActiveServices();
}