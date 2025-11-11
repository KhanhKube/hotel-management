package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import hotel.db.repository.furnishing.FurnishingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FurnishingServiceImpl implements FurnishingService {
    
    private final FurnishingRepository furnishingRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<Furnishing> getAllFurnishings(Pageable pageable) {
        return furnishingRepository.findByIsDeletedFalse(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Furnishing getFurnishingById(Integer id) {
        return furnishingRepository.findByFurnishingIdAndIsDeletedFalse(id);
    }
    
    @Override
    @Transactional
    public Furnishing saveFurnishing(Furnishing furnishing) {
        return furnishingRepository.save(furnishing);
    }
    
    @Override
    @Transactional
    public void deleteFurnishing(Integer id) {
        Furnishing furnishing = getFurnishingById(id);
        if (furnishing != null) {
            furnishing.setIsDeleted(true);
            furnishingRepository.save(furnishing);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Furnishing> searchByName(String name, Pageable pageable) {
        return furnishingRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Furnishing> findWithFilters(String search, Integer minQuantity, Integer maxQuantity, Pageable pageable) {
        return furnishingRepository.findWithFilters(search, minQuantity, maxQuantity, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return furnishingRepository.existsByNameIgnoreCaseAndIsDeletedFalse(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameExceptItself(String name, Integer furnishingId) {
        return furnishingRepository.existsByNameIgnoreCaseAndFurnishingIdNotAndIsDeletedFalse(name, furnishingId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Furnishing> getAllForDropdown() {
        return furnishingRepository.findAllByIsDeletedFalseOrderByNameAsc();
    }
}
