package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import hotel.util.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface FurnishingService {
    Page<Furnishing> findFurnishingFilter(String search,
                                          String sortBy,
                                          int page,
                                          int pageSize);
    List<Furnishing> findAllAndIsDeletedFalse();

    Furnishing findFurnishingById(int id);

    MessageResponse updateFurnishing(int id, Furnishing furnishing);

    MessageResponse createFurnishing(Furnishing furnishing);

    MessageResponse  updateFurnishingStock(List<Integer> selectedIds,
                                           Map<String, String> params,
                                           String actionType);
}
