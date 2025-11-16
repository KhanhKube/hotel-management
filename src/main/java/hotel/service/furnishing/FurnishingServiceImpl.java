package hotel.service.furnishing;

import hotel.db.entity.Furnishing;
import hotel.db.repository.furnishing.FurnishingRepository;
import hotel.util.MessageResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            furnishings = furnishings.stream()
                    .filter(x -> x.getName() != null && x.getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
        Comparator<Furnishing> comparator = Comparator.comparing(Furnishing::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("quantity".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Furnishing::getQuantity, Comparator.naturalOrder());
        }
        furnishings.sort(comparator);

        int start = Math.min(page * pageSize, furnishings.size());
        int end = Math.min(start + pageSize, furnishings.size());
        List<Furnishing> pagedFurnishings = furnishings.subList(start, end);
        return new PageImpl<>(pagedFurnishings, PageRequest.of(page, pageSize), furnishings.size());
    }

    @Override
    public List<Furnishing>findAllAndIsDeletedFalse(){
        return furnishingRepository.findFurnishingsByIsDeletedFalse();
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
    @Transactional
    public MessageResponse updateFurnishing(int id, Furnishing furnishing) {
        if (furnishing == null) {
            return new MessageResponse(false, "Hãy nhập trường cần thiết");
        }

        if (furnishing.getName() == null || furnishing.getFurnishingDescription() == null) {
            return new MessageResponse(false, "Hãy nhập trường cần thiết");
        }
        if(furnishing.getName().length() > 30 || furnishing.getFurnishingDescription().length() > 255){
            return new MessageResponse(false, "Quá số lượng kí tự tên < 30, chi tiết < 255 ");
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

    @Override
    @Transactional
    public MessageResponse updateFurnishingStock(List<Integer> selectedIds,
                                                 List<Integer> quantities,
                                                 String actionType) {

        // Kiểm tra không chọn dụng cụ
        if (selectedIds == null || selectedIds.isEmpty()) {
            return new MessageResponse(false, "Vui lòng chọn ít nhất một dụng cụ.");
        }

        // Kiểm tra số lượng nhập vào có khớp số ID không
        if (quantities == null || quantities.isEmpty() || quantities.size() != selectedIds.size()) {
            return new MessageResponse(false, "Số lượng nhập không khớp với số dụng cụ được chọn.");
        }

        // Xử lý update
        for (int i = 0; i < selectedIds.size(); i++) {
            int id = selectedIds.get(i);
            int qty = quantities.get(i);

            if (qty <= 0) {
                return new MessageResponse(false, "Số lượng phải lớn hơn 0.");
            }

            Furnishing item = furnishingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dụng cụ ID: " + id));

            if ("ADD".equalsIgnoreCase(actionType)) {
                item.setQuantity(item.getQuantity() + qty);

            } else if ("TAKE".equalsIgnoreCase(actionType)) {
                if (item.getQuantity() < qty) {
                    return new MessageResponse(false,
                            "Không đủ số lượng trong kho cho: " + item.getName());
                }
                item.setQuantity(item.getQuantity() - qty);
            }

            furnishingRepository.save(item);
        }

        String msg = "ADD".equalsIgnoreCase(actionType)
                ? "Thêm dụng cụ thành công!"
                : "Lấy dụng cụ thành công!";

        return new MessageResponse(true, msg);
    }
}
