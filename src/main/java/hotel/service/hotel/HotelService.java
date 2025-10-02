package hotel.service.hotel;

import hotel.db.entity.Hotel;

public interface HotelService {
    boolean checkAvailableRoom(Long roomId);

    Hotel findById(Long id);

    void save(Hotel hotel);
}
