package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import hotel.db.repository.furnishing.FurnishingRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FurnishingServiceImpl implements FurnishingService {
    private final FurnishingRepository furnishingRepository;

    @Override
    public Page<Furnishing> findFurnishingFilter(String search,
                                         String sortBy,
                                         int page,
                                         int pageSize) {
        List<Furnishing> furnishings = furnishingRepository.findFurnishingsByIsDeletedFalse();


        int start = Math.min(page * pageSize, furnishings.size());
        int end = Math.min(start + pageSize, furnishings.size());
        List<Furnishing> pagedFurnishings = furnishings.subList(start, end);
        return new PageImpl<>(pagedFurnishings, PageRequest.of(page, pageSize), furnishings.size());
    }

    @Override
    public Furnishing findFurnishingById(int furnishingId){

        Furnishing furnishing = furnishingRepository.findFurnishingByFurnishingId(furnishingId);
        if(furnishing == null){
            return null;
        }
        return furnishing;
    }
}
