package hotel.service.hotel;

import hotel.db.entity.Hotel;
import hotel.db.repository.hoteldetail.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

   private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public Hotel save(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Hotel findById(Long id) {
        return hotelRepository.findById(id).orElse(null);
    }




    @Override
    public Page<Hotel> getAllHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    @Override
    public Page<Hotel> searchHotelsByName(String name, Pageable pageable) {
      //  return hotelRepository.findByNameContainingIgnoreCase(name, pageable);
        return null;

    }

    @Override
    public Page<Hotel> filterHotelsByStars(Integer stars, Pageable pageable) {
     //   return hotelRepository.findByStars(stars, pageable);
        return null;

    }

    @Override
    public Page<Hotel> filterHotelsByStatus(String status, Pageable pageable) {
       // return hotelRepository.findByStatus(status, pageable);
        return null;

    }

    @Override
    public Page<Hotel> getHotelsWithFilters(String name, Integer stars, String status, Pageable pageable) {
      //  return hotelRepository.findByFilters(name, stars, status, pageable);
        return null;
    }

    @Override
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    @Override
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
    @Override
    @Transactional
    public Hotel changeStatus(Long id, String status) {
        Hotel hotel = findById(id);
        if (hotel != null) {
        //    hotel.setStatus(status);
            return hotelRepository.save(hotel);
        }
        throw new RuntimeException("Không tìm thấy khách sạn với ID: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAvailableRoom(Long roomId) {
        // Implementation for checking room availability
        return true;
    }
}
