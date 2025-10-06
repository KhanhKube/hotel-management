package hotel.rest.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckOutStaffConfirmDto;
import hotel.db.dto.checking.CheckOutFinalizeRequestDto;
import hotel.service.checking.CheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/checking")
@RequiredArgsConstructor
public class CheckController {

//    private final CheckService checkService;
//
//    // ========== TRẢ VỀ VIEW =========
//	@GetMapping("/checkin")
//	public String checkinPage() {
//		return "checkin"; // sẽ tìm tới file checkin.html trong template
//	}
//
//
//    @GetMapping("/checkout")
//    public String checkoutPage() {
//        return "checking/checkout"; // sẽ load file checkout.html trong templates/checking/
//    }
//
//    // ========== API ==========
//    @PostMapping("/api/in")
//    @ResponseBody
//    public Booking checkIn(@RequestBody @Valid CheckInRequestDto dto) {
//        return checkService.checkIn(dto);
//    }
//
//    @PostMapping("/api/out/staff")
//    @ResponseBody
//    public Booking staffConfirm(@RequestBody @Valid CheckOutStaffConfirmDto dto) {
//        return checkService.staffConfirmCheckout(dto);
//    }
//
//    @PostMapping("/api/out/finalize")
//    @ResponseBody
//    public Booking finalizeCheckout(@RequestBody @Valid CheckOutFinalizeRequestDto dto) {
//        return checkService.receptionistFinalizeCheckout(dto);
//    }
}
