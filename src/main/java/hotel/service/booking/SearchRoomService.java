package hotel.service.booking;

import hotel.config.FeignConfig;
import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "n8n-service", url = "${app.n8n-service}", configuration = FeignConfig.class)
public interface SearchRoomService {
	@PostMapping(value = "/webhook/search")
	ListRoomResponse getRoom(@RequestBody SearchRoomRequest request);
}