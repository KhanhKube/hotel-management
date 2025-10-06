package hotel.service.booking;

import hotel.db.dto.room.ListRoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "n8n-service",url = "${app.n8n-service}")
public interface SearchRoomService {
	@PostMapping(value = "/api/n8n/search")
	ListRoomResponse getRoom();
}