package hotel.service.image;
import hotel.db.entity.RoomImage;
import hotel.db.repository.roomimage.RoomImageRepository;
import hotel.service.file.RoomImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ImageServiceImpl implements  ImageService {

    private final RoomImageRepository roomImageRepository;
    private final RoomImageUploadService fileUploadService;

    @Override
    public List<RoomImage> getImagesByRoomId(Integer roomId) {
        return roomImageRepository.findByRoomId(roomId);
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

        // Xóa file vật lý
        try {
            fileUploadService.deleteRoomImage(roomImage.getRoomImageUrl());
        } catch (IOException e) {
            log.error("Error deleting image file: {}", e.getMessage());
        }

        // Xóa record trong DB
        roomImageRepository.delete(roomImage);
    }

}
