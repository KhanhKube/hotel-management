package hotel.service.image;

import hotel.db.entity.RoomImage;

import java.util.List;

public interface ImageService {

    List<RoomImage> getImagesByRoomId(Integer roomId);

    RoomImage saveRoomImage(Integer roomId, String imageUrl);

    void deleteRoomImage(Integer roomImageId);

}
