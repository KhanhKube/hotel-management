package hotel.rest.room;

import hotel.db.dto.room.RoomBookListDto;
import hotel.db.enums.BedType;
import hotel.db.enums.RoomStatus;
import hotel.db.enums.RoomType;
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.size.SizeRepository;
import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomViewController {

    private final RoomService roomService;
    private final FloorRepository floorRepository;
    private final SizeRepository sizeRepository;

    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("roomTypes", RoomType.ALL);
        model.addAttribute("bedTypes", BedType.ALL);
        model.addAttribute("floors", floorRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
    }

    @GetMapping
    public String viewListRooms(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String bedType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Page<RoomBookListDto> roomPage = roomService.getRoomListWithFiltersAndPagination(
                minPrice, maxPrice, roomType, floor, bedType, sortBy, page, size
        );
        //field pagnitation
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("totalItems", roomPage.getTotalElements());
        //field thanh filter
        model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("selectedFloor", floor);
        model.addAttribute("selectedBedType", bedType);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        return "common/room-booklist";
    }
}
