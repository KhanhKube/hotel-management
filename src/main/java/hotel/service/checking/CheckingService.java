package hotel.service.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckInResponseDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.entity.Room;
import hotel.db.entity.User;

import java.util.List;

public interface CheckingService {
    
    // Check-in methods
    CheckInResponseDto processCheckIn(CheckInRequestDto request);
    CheckInResponseDto getCheckInById(Integer id);
    List<CheckInResponseDto> getAllActiveCheckIns();
    
    // Check-out methods
    void processCheckOut(CheckOutRequestDto request);
    CheckInResponseDto getCheckOutById(Integer id);
    List<CheckInResponseDto> getCheckOutCandidates();
    List<CheckInResponseDto> getAllCheckOutHistory();
    
    // Utility methods
    List<Room> getAvailableRooms();
    List<User> getAllCustomers();
}

