package hotel.service.hotel;

import hotel.db.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface HotelService {
    Hotel save(Hotel hotel);
    
    Hotel findById(Long id);

    Page<Hotel> getAllHotels(Pageable pageable);
    Page<Hotel> searchHotelsByName(String name, Pageable pageable);
    Page<Hotel> filterHotelsByStars(Integer stars, Pageable pageable);
    Page<Hotel> filterHotelsByStatus(String status, Pageable pageable);
    Page<Hotel> getHotelsWithFilters(String name, Integer stars, String status, Pageable pageable);
    Optional<Hotel> getHotelById(Long id);
    Hotel saveHotel(Hotel hotel);
    void deleteHotel(Long id);
    
    Hotel changeStatus(Long id, String status);
    
    boolean checkAvailableRoom(Long roomId);
}
