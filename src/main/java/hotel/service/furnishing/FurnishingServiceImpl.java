package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import hotel.db.repository.furnishing.FurnishingRepository;
import hotel.util.MessageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Override
    public MessageResponse updateFurnishing(int id, Furnishing furnishing) {
        if (furnishing == null) {
            return new MessageResponse(false, "Hãy nhập trường cần thiết");
        }

        if (furnishing.getName() == null || furnishing.getFurnishingDescription() == null) {
            return new MessageResponse(false, "Hãy nhập trường cần thiết");
        }

        if(furnishing.getQuantity() < 0){
            return new MessageResponse(false, "Số lượng phải lớn hơn không");
        }
        Furnishing existing = furnishingRepository.findFurnishingByFurnishingId(id);
        if (existing == null) {
            return new MessageResponse(false, "Không tìm thấy dụng cụ này");
        }

        Optional<Furnishing> duplicate = furnishingRepository.findByName(furnishing.getName())
                .filter(f -> f.getFurnishingId() != id);
        if (duplicate.isPresent()) {
            return new MessageResponse(false, "Đã có dụng cụ này");
        }

        boolean notChanged =
                Objects.equals(existing.getName(), furnishing.getName()) &&
                        Objects.equals(existing.getFurnishingDescription(), furnishing.getFurnishingDescription()) &&
                        Objects.equals(existing.getQuantity(), furnishing.getQuantity());

        if (notChanged) {
            return new MessageResponse(false, "Không có thay đổi nào để cập nhật");
        }

        existing.setName(furnishing.getName());
        existing.setFurnishingDescription(furnishing.getFurnishingDescription());
        existing.setQuantity(furnishing.getQuantity());
        existing.setUpdatedAt(LocalDateTime.now());

        furnishingRepository.save(existing);
        return new MessageResponse(true, "Cập nhật dụng cụ thành công");
    }

    @Override
    public MessageResponse createFurnishing(Furnishing furnishing){
        if(furnishing == null){
            return new MessageResponse(false, "Hãy nhập trường cần thiết");
        }
        if(furnishing.getName() == null||
        furnishing.getFurnishingDescription() == null){
            return new MessageResponse(false, "Hãy nhập trường cần thiết");
        }
        if(furnishingRepository.findFurnishingsByName(furnishing.getName()).size() == 1 ){
            return new MessageResponse(false, "Đã có dụng cụ này");
        }
        if(furnishing.getQuantity() <= 0){
            return new MessageResponse(false, "Số lượng phải lớn hơn bằng không");
        }
        furnishingRepository.save(furnishing);
        return new MessageResponse(true, "Tạo mới dụng cụ thành công");
    }
}
