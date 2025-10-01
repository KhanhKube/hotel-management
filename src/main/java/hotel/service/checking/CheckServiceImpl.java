package hotel.service.checking;

import hotel.db.entity.Room;
import hotel.db.enums.RoomStatus;   // ✅ sửa lại đường dẫn import đúng
import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckOutStaffConfirmDto;
import hotel.db.dto.checking.CheckOutFinalizeRequestDto;
import hotel.db.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckServiceImpl implements CheckService {

//    private final RoomRepository roomRepo;
//    private final BookingRepository bookingRepo;
//
//    // ================== Check-in ==================
//    @Override
//    @Transactional
//    public Booking checkIn(CheckInRequestDto dto) {
//        Room room = roomRepo.findById(dto.getRoomId())
//                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
//        if (room.getStatus() != RoomStatus.AVAILABLE) {
//            throw new IllegalStateException("Room not available");
//        }
//        room.setStatus(RoomStatus.OCCUPIED);
//        roomRepo.save(room);
//
//        Booking booking = Booking.builder()
//                .customerName(dto.getCustomerName())
//                .customerPhone(dto.getCustomerPhone())
//                .customerEmail(dto.getCustomerEmail())
//                .checkInDate(LocalDateTime.now())
//                .room(room)
//                .active(true)
//                .totalAmount(dto.getExpectedPrice())
//                .note(dto.getNote())
//                .servicesJson(dto.getServices() == null ? null : dto.getServices().toString())
//                .build();
//
//        return bookingRepo.save(booking);
//    }
//
//    // ================== Staff confirm checkout ==================
//    @Override
//    @Transactional
//    public Booking staffConfirmCheckout(CheckOutStaffConfirmDto dto) {
//        Booking booking = bookingRepo.findById(dto.getBookingId())
//                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
//        String msg = dto.isOk() ? "Staff OK" : "Staff REJECT";
//        booking.setNote(merge(booking.getNote(), msg + " " + dto.getNote()));
//        return bookingRepo.save(booking);
//    }
//
//    // ================== Receptionist finalize checkout ==================
//    @Override
//    @Transactional
//    public Booking receptionistFinalizeCheckout(CheckOutFinalizeRequestDto dto) {
//        Booking booking = bookingRepo.findById(dto.getBookingId())
//                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
//        if (!booking.isActive()) throw new IllegalStateException("Already checked out");
//
//        double total = booking.getTotalAmount() == null ? 0d : booking.getTotalAmount();
//        if (dto.getExtraFee() != null) total += dto.getExtraFee();
//        booking.setTotalAmount(total);
//        booking.setActive(false);
//        booking.setCheckOutDate(LocalDateTime.now());
//        booking.setNote(merge(booking.getNote(), "Reception finalize " + dto.getNote()));
//        bookingRepo.save(booking);
//
//        // Sau khi checkout → chuyển phòng về CLEANING
//        Room room = booking.getRoom();
//        room.setStatus(RoomStatus.CLEANING);
//        roomRepo.save(room);
//
//        return booking;
//    }
//
//    // ================== After checkout: Staff dọn phòng ==================
//    @Override
//    @Transactional
//    public Room markRoomAvailable(Long roomId) {
//        Room room = roomRepo.findById(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
//        room.setStatus(RoomStatus.AVAILABLE);
//        return roomRepo.save(room);
//    }
//
//    // ================== Helper ==================
//    private String merge(String oldNote, String add) {
//        if (oldNote == null || oldNote.isBlank()) return add;
//        return oldNote + " | " + add;
//    }
}
