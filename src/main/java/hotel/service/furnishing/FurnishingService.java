package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import hotel.util.MessageResponse;
import org.springframework.data.domain.Page;

public interface FurnishingService {
    Page<Furnishing> findFurnishingFilter(String search,
                                          String sortBy,
                                          int page,
                                          int pageSize);

    Furnishing findFurnishingById(int id);

    MessageResponse updateFurnishing(int id, Furnishing furnishing);

    MessageResponse createFurnishing(Furnishing furnishing);
}
