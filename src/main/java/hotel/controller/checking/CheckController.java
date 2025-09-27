package hotel.controller.checking;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckController {

	@GetMapping("/checkin")
	public String checkinPage() {
		return "checkin"; // sẽ tìm tới file checkin.html trong template
	}

	@GetMapping("/checkout")
	public String checkoutPage() {
		return "checkout"; // sẽ tìm tới file checkout.html trong template
	}
}
