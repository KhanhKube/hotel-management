package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FurnishingService {
    
    // CRUD operations
    Page<Furnishing> getAllFurnishings(Pageable pageable);
    Furnishing getFurnishingById(Integer id);
    Furnishing saveFurnishing(Furnishing furnishing);
    void deleteFurnishing(Integer id);
    
    // Search and filter
    Page<Furnishing> searchByName(String name, Pageable pageable);
    Page<Furnishing> findWithFilters(String search, Integer minQuantity, Integer maxQuantity, Pageable pageable);
    
    // Validation
    boolean existsByName(String name);
    boolean existsByNameExceptItself(String name, Integer furnishingId);
    
    // Get all for dropdown
    List<Furnishing> getAllForDropdown();
}
