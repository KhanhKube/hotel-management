package hotel.db.repository.roomimage;

import hotel.db.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Integer> {

    List<RoomImage> findByRoomId(Integer roomId);

}
