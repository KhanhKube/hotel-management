package hotel.service.image;
import hotel.db.entity.RoomImage;
import hotel.db.repository.roomimage.RoomImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ImageServiceImpl implements  ImageService {

    private final RoomImageRepository roomImageRepository;

    @Override
    public List<RoomImage> getImagesByRoomId(Integer roomId) {
        return roomImageRepository.findByRoomIdAndIsDeletedFalse(roomId);
    }

    @Override
    public RoomImage saveRoomImage(Integer roomId, String imageUrl) {
        RoomImage roomImage = new RoomImage();
        roomImage.setRoomId(roomId);
        roomImage.setRoomImageUrl(imageUrl);
        return roomImageRepository.save(roomImage);
    }

    @Override
    public void deleteRoomImage(Integer roomImageId) {
        RoomImage roomImage = roomImageRepository.findById(roomImageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Soft delete: chỉ đánh dấu là deleted
        roomImage.setIsDeleted(true);
        roomImageRepository.save(roomImage);
        
        log.info("Soft deleted image ID: {}", roomImageId);
    }

}
