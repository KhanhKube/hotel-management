package hotel.service.hotel;

import org.springframework.stereotype.Service;

@Service
public abstract class HotelServiceImpl implements HotelService {

    @Override
    public boolean checkAvailableRoom(Long roomId) {
        // logic kiểm tra phòng trống
        // ví dụ tạm: nếu id chẵn thì còn phòng, lẻ thì hết phòng
        return roomId % 2 == 0;
    }
}
