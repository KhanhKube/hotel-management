package hotel.service.room;

import hotel.db.dto.furnishing.FurnishingFormDto;
import hotel.db.dto.room.*;
import hotel.db.entity.*;
import hotel.db.repository.floor.FloorRepository;
import hotel.db.repository.furnishing.FurnishingRepository;
import hotel.db.repository.roomfurnishing.RoomFurnishingRepository;
import hotel.db.repository.roommaintenance.RoomMaintenanceRepository;
import hotel.db.repository.roomview.RoomViewRepository;
import hotel.db.repository.user.UserRepository;
import hotel.db.repository.view.ViewRepository;
import hotel.service.size.SizeService;
import hotel.db.repository.orderdetail.OrderDetailRepository;
import hotel.db.repository.room.RoomRepository;
import hotel.db.repository.roomimage.RoomImageRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomViewRepository roomViewRepository;
    private final ViewRepository viewRepository;
    private final SizeService sizeService;
    private final RoomMaintenanceRepository roomMaintenanceRepository;
    private final RoomFurnishingRepository roomFurnishingRepository;
    private final FurnishingRepository furnishingRepository;
    private final UserRepository userRepository;

    public List<String> getRoomViewList(Integer roomId) {
        return roomViewRepository.findRoomViewId(roomId).stream().map(viewId ->
                viewRepository.findViewTypeByViewId(viewId)).collect(Collectors.toList());
    }


    
    @Override
    public List<FurnishingFormDto> getFurnishingsForForm(Integer roomId) {
        // Lấy tất cả vật dụng trong khách sạn (chỉ để hiển thị tên)
        List<Furnishing> allFurnishings = furnishingRepository.findFurnishingsByIsDeletedFalse();
        
        // Lấy vật dụng hiện có của phòng (nếu đang edit)
        Map<Integer, Integer> roomFurnishingMap = new HashMap<>();
        if (roomId != null) {
            List<RoomFurnishing> roomFurnishings = roomFurnishingRepository.findByRoomIdAndIsDeletedFalse(roomId);
            roomFurnishingMap = roomFurnishings.stream()
                .collect(Collectors.toMap(
                    RoomFurnishing::getFurnishingId,
                    RoomFurnishing::getQuantity
                ));
        }
        
        // Tạo DTO cho form
        List<FurnishingFormDto> result = new ArrayList<>();
        for (Furnishing furnishing : allFurnishings) {
            FurnishingFormDto dto = new FurnishingFormDto();
            dto.setFurnishingId(furnishing.getFurnishingId());
            dto.setName(furnishing.getName());
            // Số lượng vật dụng của phòng (mặc định 0 nếu chưa có)
            dto.setRoomQuantity(roomFurnishingMap.getOrDefault(furnishing.getFurnishingId(), 0));
            result.add(dto);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public void saveRoomFurnishings(Integer roomId, List<Integer> furnishingIds, List<Integer> quantities) {
        if (furnishingIds == null || quantities == null || furnishingIds.size() != quantities.size()) {
            return;
        }
        
        // Xóa tất cả vật dụng cũ của phòng
        List<RoomFurnishing> oldRoomFurnishings = roomFurnishingRepository.findByRoomIdAndIsDeletedFalse(roomId);
        roomFurnishingRepository.deleteAll(oldRoomFurnishings);
        
        // Thêm vật dụng mới theo cấu hình
        for (int i = 0; i < furnishingIds.size(); i++) {
            Integer furnishingId = furnishingIds.get(i);
            Integer quantity = quantities.get(i);
            
            // Chỉ lưu nếu quantity > 0
            if (quantity != null && quantity > 0) {
                RoomFurnishing roomFurnishing = new RoomFurnishing();
                roomFurnishing.setRoomId(roomId);
                roomFurnishing.setFurnishingId(furnishingId);
                roomFurnishing.setQuantity(quantity);
                roomFurnishingRepository.save(roomFurnishing);
            }
        }
    }

    @Override
    public void saveMaintenance(Integer roomId, String checkInDate, String checkOutDate,
                                String description, Integer createBy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate startDate = LocalDate.parse(checkInDate, formatter);
        LocalDate endDate = LocalDate.parse(checkOutDate, formatter);

        LocalDateTime startDateTime = startDate.atTime(14, 0);
        LocalDateTime endDateTime = endDate.atTime(12, 0);
        RoomMaintenance maintenance = new RoomMaintenance();
        maintenance.setRoomId(roomId);
        maintenance.setStartDate(startDateTime);
        maintenance.setEndDate(endDateTime);
        maintenance.setStatus("Đã giao");
        maintenance.setDescription(description);
        maintenance.setCreateBy(createBy);

        roomMaintenanceRepository.save(maintenance);
    }


    @Override
    public List<String> getBookedDatesForBookingRoom(Integer roomId) {
        LocalDateTime fromDate = LocalDateTime.now();
        LocalDateTime toDate = fromDate.plusMonths(2);

        List<OrderDetail> bookings = orderDetailRepository.findBookingsByRoomAndDateRange(
                roomId,
                fromDate,
                toDate,
                Arrays.asList("CHECKED_IN", "CHECKED_OUT", "CONFIRMED")
        );

        List<RoomMaintenance> maintenances = roomMaintenanceRepository.findMaintenancesByRoomAndDateRange(
                roomId,
                fromDate,
                toDate,
                Arrays.asList("Đã giao")
        );

        List<String> bookedDates = new ArrayList<>();

        for (OrderDetail booking : bookings) {
            LocalDate start = booking.getStartDate().toLocalDate();
            LocalDate end = booking.getEndDate().toLocalDate();

            LocalDate current = start;
            while (current.isBefore(end)) { // Không bao gồm ngày end
                bookedDates.add(current.toString()); // Format: yyyy-MM-dd
                current = current.plusDays(1);
            }
        }
        for (RoomMaintenance maintenance : maintenances) {
            LocalDate start = maintenance.getStartDate().toLocalDate();
            LocalDate end = maintenance.getEndDate().toLocalDate();
            LocalDate current = start;
            while (current.isBefore(end)) {
                bookedDates.add(current.toString());
                current = current.plusDays(1);
            }
        }

        return bookedDates;
    }

    @Override
    public List<String> getBookedDatesForCheckOut(Integer roomId) {
        LocalDateTime fromDate = LocalDateTime.now();
        LocalDateTime toDate = fromDate.plusMonths(2);

        List<OrderDetail> bookings = orderDetailRepository.findBookingsByRoomAndDateRange(
                roomId,
                fromDate,
                toDate,
                Arrays.asList("CHECKED_IN", "CHECKED_OUT", "CONFIRMED")
        );

        List<RoomMaintenance> maintenances = roomMaintenanceRepository.findMaintenancesByRoomAndDateRange(
                roomId,
                fromDate,
                toDate,
                Arrays.asList("Đã giao")
        );

        List<String> bookedDates = new ArrayList<>();

        for (OrderDetail booking : bookings) {
            LocalDate start = booking.getStartDate().toLocalDate();
            LocalDate end = booking.getEndDate().toLocalDate();

            LocalDate current = start.plusDays(1); // Bắt đầu từ ngày sau start
            while (current.isBefore(end)) { // Dừng trước ngày end
                bookedDates.add(current.toString()); // Format: yyyy-MM-dd
                current = current.plusDays(1);
            }
        }
        for (RoomMaintenance maintenance : maintenances) {
            LocalDate start = maintenance.getStartDate().toLocalDate();
            LocalDate end = maintenance.getEndDate().toLocalDate();
            LocalDate current = start.plusDays(1);
            while (current.isBefore(end)) {
                bookedDates.add(current.toString());
                current = current.plusDays(1);
            }
        }

        return bookedDates;
    }

    @Override
    public Page<RoomBookListDto> getRoomListWithFiltersAndPagination(BigDecimal minPrice, BigDecimal maxPrice, String roomType,
                                                                     Integer floor, String bedType, String sortBy, int page, int size, String date) {
        List<Room> rooms = roomRepository.findAll();

        //Filter theo roomType (Loại phòng)
        if (roomType != null && !roomType.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> x.getRoomType().equals(roomType))
                    .collect(Collectors.toList());
        }
        //Filter theo giá min
        if (minPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        //Filter theo giá trần
        if (maxPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }
        //Filter theo tầng
        if (floor != null) {
            rooms = rooms.stream()
                    .filter(x -> floor.equals(x.getFloorId()))
                    .collect(Collectors.toList());
        }
        //Filter theo bedtype
        if (bedType != null && !bedType.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> bedType.equals(x.getBedType()))
                    .collect(Collectors.toList());
        }
        //Thêm đk Sort
        if (sortBy != null) {
            switch (sortBy) {
                case "price-asc":
                    rooms.sort(Comparator.comparing(Room::getPrice));
                    break;
                case "price-desc":
                    rooms.sort(Comparator.comparing(Room::getPrice).reversed());
                    break;
                case "room-asc":
                    rooms.sort(Comparator.comparing(Room::getRoomNumber));
                    break;
                case "room-desc":
                    rooms.sort(Comparator.comparing(Room::getRoomNumber).reversed());
                    break;
            }
        }
        //Filter theo StartDate EndDate
        if (date != null && !date.isEmpty()) {
            String[] dateArr = date.split(" - ");
            String startDate = dateArr[0];
            String endDate = dateArr[1];
            System.out.println(endDate + "-" + startDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime filterStartDate = LocalDate.parse(startDate, formatter).atTime(14,0); //Thêm nghiệp vụ để để filter chuẩn phòng khách có thể book.
            LocalDateTime filterEndDate = LocalDate.parse(endDate, formatter).atTime(12,0);
            List<Integer> roomIdList = orderDetailRepository.findRoomIdsByFilterEndateAndStatdate(filterStartDate,filterEndDate);
            rooms = rooms.stream().filter(x -> roomIdList.contains(x.getRoomId()))
                    .collect(Collectors.toList());

        }
        List<RoomBookListDto> BookList = rooms.stream() //Lấy Listcác phòng đã được lọc field qua Dto
                .filter(x -> !"Dừng hoạt động".equals(x.getSystemStatus()))
                .map(x -> {
                    return toRoomBookDto(x);
                }).collect(Collectors.toList());

        //Paging thủ công
        int startpage = page * size;
        int endpage = Math.min(startpage + size, BookList.size());

        List<RoomBookListDto> pageDtos = BookList.subList(startpage, endpage);

        return new PageImpl<>(pageDtos, PageRequest.of(page, size), BookList.size());
    }

    /*
    //Dto trả về field cho trang hiển thị danh sách phòng cho bên customer booking
    */
    private RoomBookListDto toRoomBookDto(Room x) {
        RoomBookListDto dto = new RoomBookListDto();
        dto.setRoomId(x.getRoomId());
        dto.setRoomNumber(x.getRoomNumber());
        dto.setRoomType(x.getRoomType());
        dto.setPrice(x.getPrice());
        dto.setRoomDescription(x.getRoomDescription());
        dto.setRoomViews(getRoomViewList(x.getRoomId()));
        
        // Lấy ảnh đầu tiên từ database (URL từ Cloudinary)
        List<RoomImage> images = roomImageRepository.findByRoomId(x.getRoomId());
        if (images != null && !images.isEmpty()) {
            dto.setImageRoom(images.get(0).getRoomImageUrl());
        } else {
            dto.setImageRoom(null); // Sẽ dùng ảnh mặc định trong HTML
        }
        
        return dto;
    }
    /*
    Tìm ngày trống thoả mãn điều kiện booking đầu tiên
    */
    private LocalDate findNextAvailableDate(Integer roomId, LocalDateTime startFrom) {
        // Lấy tất cả booking từ startFrom trở đi
        List<OrderDetail> bookings = orderDetailRepository.findUpcomingBookingsByRoomId(
                roomId,
                startFrom
        );

        // Bắt đầu check từ ngày startFrom
        LocalDate checkDate = startFrom.toLocalDate();

        // Giới hạn check tối đa 365 ngày (tránh loop vô hạn)
        for (int i = 0; i < 365; i++) {
            LocalDateTime checkin = checkDate.atTime(14, 0);
            LocalDateTime checkout = checkDate.plusDays(1).atTime(12, 0);

            // Check xem có booking nào conflict không
            boolean hasConflict = bookings.stream().anyMatch(booking ->
                    booking.getStartDate().isBefore(checkout) &&
                            booking.getEndDate().isAfter(checkin)
            );

            if (!hasConflict) {
                return checkDate; // Tìm thấy ngày trống
            }

            checkDate = checkDate.plusDays(1); // Check ngày tiếp theo
        }

        return null; // chưa có khách book phòng.
    }

    /*
    Listtrả về về List chứa các field cần thiết.
    */
    @Override
    public List<RoomListDto> getRoomList() {
        return roomRepository.findAllByIsDeletedFalse().stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    /*
    Method filter và pagination cho trang quản lý phòng (admin)
    */
    @Override
    public Page<RoomListDto> getRoomListForManagement(String search, String roomType, String status, String systemstatus,
                                                      Integer floor, Double size, BigDecimal minPrice,
                                                      BigDecimal maxPrice, String sortBy, int page, int pageSize) {
        List<Room> rooms = roomRepository.findAllByIsDeletedFalse();

        // Filter theo search (số phòng)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            rooms = rooms.stream()
                    .filter(x -> x.getRoomNumber().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
        // Filter theo loại phòng
        if (roomType != null && !roomType.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> x.getRoomType().equals(roomType))
                    .collect(Collectors.toList());
        }
        // Filter theo Tình trạng phòng
        if (systemstatus != null && !systemstatus.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> x.getSystemStatus().equals(systemstatus))
                    .collect(Collectors.toList());
        }
        // Filter theo trạng thái
        if (status != null && !status.isEmpty()) {
            rooms = rooms.stream()
                    .filter(x -> x.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        // Filter theo tầng
        if (floor != null) {
            rooms = rooms.stream()
                    .filter(x -> {
                        Integer roomFloorId = x.getFloorId();
                        if (roomFloorId == null) return false;
                        return floorRepository.findById(roomFloorId)
                                .map(f -> f.getFloorNumber().equals(floor))
                                .orElse(false);
                    })
                    .collect(Collectors.toList());
        }
        // Filter theo diện tích
        if (size != null) {
            rooms = rooms.stream()
                    .filter(x -> {
                        Integer roomSizeId = x.getSizeId();
                        if (roomSizeId == null) return false;
                        try {
                            Size sizeEntity = sizeService.getSizeById(roomSizeId);
                            return sizeEntity != null && sizeEntity.getSize().equals(size);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
        // Filter theo giá min
        if (minPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
        }
        // Filter theo giá max
        if (maxPrice != null) {
            rooms = rooms.stream()
                    .filter(x -> x.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }
        // Sort
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] sortParams = sortBy.split(",");
            String field = sortParams[0];
            String direction = sortParams.length > 1 ? sortParams[1] : "asc";

            Comparator<Room> comparator = null;

            if ("roomNumber".equals(field)) {
                comparator = Comparator.comparing(Room::getRoomNumber);
            } else if ("price".equals(field)) {
                comparator = Comparator.comparing(Room::getPrice);
            }

            if (comparator != null) {
                if ("desc".equals(direction)) {
                    comparator = comparator.reversed();
                }
                rooms.sort(comparator);
            }
        }
        // Convert to DTO
        List<RoomListDto> roomDtos = rooms.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());

        // Pagination thủ công
        int startPage = page * pageSize;
        int endPage = Math.min(startPage + pageSize, roomDtos.size());

        List<RoomListDto> pagedDtos = roomDtos.subList(startPage, endPage);

        return new PageImpl<>(pagedDtos, PageRequest.of(page, pageSize), roomDtos.size());
    }

    private RoomListDto toListDto(Room room) {
        // Query floor number từ repository
        Integer floorNumber = null;
        if (room.getFloorId() != null) {
            floorNumber = floorRepository.findById(room.getFloorId())
                    .map(floor -> floor.getFloorNumber())
                    .orElse(null);
        }

        // Query size từ repository
        Double sizeValue = null;
        if (room.getSizeId() != null) {
            try {
                Size sizeEntity = sizeService.getSizeById(room.getSizeId());
                sizeValue = sizeEntity != null ? sizeEntity.getSize() : null;
            } catch (Exception e) {
                sizeValue = null;
            }
        }

        return new RoomListDto(
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomType(),
                floorNumber,
                sizeValue,
                room.getPrice(),
                room.getStatus(),
                room.getSystemStatus()
        );
    }

    @Override
    public boolean checkForCreateRoomNumber(String roomNumber) {
        return roomRepository.existsByRoomNumber(roomNumber);
    }

    @Override
    public boolean checkForEditRoomNumber(String roomNumber, Long roomId) {
        return roomRepository.existsByRoomNumberAndRoomId(roomNumber, roomId);
    }

    @Override
    public HashMap<String, String> saveRoom(Room room) {
        HashMap<String, String> result = new HashMap<>();

        try {
            // Validate - truyền thêm roomId để phân biệt CREATE vs UPDATE
            String validationError = validateRoomNumber(room.getRoomNumber(), room.getFloorId(), room.getSizeId(), room.getRoomType(), room.getBedType(), room.getPrice(), room.getRoomId());
            if (validationError != "") {
                result.put("error", validationError);
                return result;
            }

            roomRepository.save(room);
            result.put("success", "Thành công");
            return result;

        } catch (Exception e) {
            log.error("Error saving room: {}", e.getMessage());
            result.put("error", e.getMessage());
            return result;
        }
    }
    @Override
    public void DeleteRoom(Integer id) {
        roomRepository.softDeleteById(id);
    }

    private String validateRoomNumber(String roomNumber, Integer floorId, Integer sizeId, String roomType, String bedType, BigDecimal price, Integer roomId) {
        // Validate floorId và sizeId không null
        if (floorId == null) {
            return "Vui lòng chọn tầng!";
        }
        if (sizeId == null) {
            return "Vui lòng chọn diện tích!";
        }
        
        //lấy số tầng và số size.
        Integer floorNumber = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tầng"))
                .getFloorNumber();

        Double sizeNumber;
        try {
            sizeNumber = sizeService.getSizeById(sizeId).getSize();
        } catch (Exception e) {
            sizeNumber = null;
        }

        if (roomType.equals("Tiêu chuẩn")) {
            if (sizeNumber != null && sizeNumber > 40) {
                return "Phòng Tiêu chuẩn diện tích vui lòng nhỏ hơn hoặc bằng 40 mét vuông!";
            }
            if (bedType.equals("Giường King") || bedType.equals("Giường Queen")) {
                return "Phong tiêu chuẩn chỉ được chọn giường Đơn hoặc Đôi!";
            }
        } else if (roomType.equals("Cao cấp") || roomType.equals("Hạng sang")) {
            if (sizeNumber != null && (sizeNumber > 50 || sizeNumber <= 40)) {
                return "Phòng " + roomType + " diện tích vui lòng nhỏ hơn hoặc bằng " +
                        "50 và lớn hơn 40 mét vuông!";
            }
        } else if (roomType.equals("VIP")) {
            if (sizeNumber != null && (sizeNumber > 70 || sizeNumber <= 50)) {
                return "Phòng " + roomType + " diện tích vui lòng nhỏ hơn hoặc bằng " +
                        "70 và lớn hơn 50 mét vuông!";
            }
        } else {
            if (sizeNumber != null && sizeNumber < 80) {
                return "Phòng " + roomType + " diện tích vui lòng lớn hơn hoặc bằng 80 mét vuông!";
            }
            if (!bedType.equals("Giường King") && !bedType.equals("Giường Queen")) {
                return "Phòng Tổng thống vui lòng chọn giường King hoặc Queen!";
            }
        }

        // Kiểm tra format số phòng - phải là số
        if (!roomNumber.matches("\\d+")) {
            return "Số phòng chỉ được chứa chữ số. Vui lòng nhập lại!";
        }

        // Số phòng phải có ít nhất 3 chữ số
        if (roomNumber.length() < 3) {
            return "Số phòng phải có ít nhất 3 chữ số (VD: 101, 1201). Vui lòng nhập lại!";
        }

        // Lấy 2 số cuối (số phòng trong tầng)
        String lastTwoDigits = roomNumber.substring(roomNumber.length() - 2);
        int roomNumberInFloor = Integer.parseInt(lastTwoDigits);

        // Lấy các số đầu (số tầng)
        String floorPrefix = roomNumber.substring(0, roomNumber.length() - 2);
        int roomFloorNumber = Integer.parseInt(floorPrefix);

        // Các số đầu phải trùng với số tầng đã chọn
        if (roomFloorNumber != floorNumber) {
            return "Số phòng phải bắt đầu bằng số tầng " + floorNumber + " (VD: " + floorNumber + "01, " + floorNumber + "02). Vui lòng nhập lại!";
        }

        // 2 số cuối từ 01-11
        if (roomNumberInFloor < 1 || roomNumberInFloor > 11) {
            return "Số phòng trong tầng (2 số cuối) phải từ 01-11!";
        }

        // Validate giá tiền
        if (price == null) {
            return "Vui lòng nhập giá phòng!";
        }
        
        BigDecimal minPrice = new BigDecimal("100000");
        BigDecimal maxPrice = new BigDecimal("10000000");
        
        if (price.compareTo(minPrice) < 0) {
            return "Giá phòng tối thiểu là 100.000 VNĐ!";
        }
        
        if (price.compareTo(maxPrice) > 0) {
            return "Giá phòng tối đa là 10.000.000 VNĐ!";
        }

        // Đếm các phòng chưa bị xóa theo tầng
        // Nếu là UPDATE (roomId != null), loại trừ phòng hiện tại khỏi count
        long roomCountOnFloor = roomRepository.findAllByIsDeletedFalse().stream()
                .filter(r -> r.getFloorId().equals(floorId))
                .filter(r -> roomId == null || !r.getRoomId().equals(roomId)) // Loại trừ phòng đang update
                .count();
        // Lớn hơn 11 trả về message error
        if (roomCountOnFloor >= 11) {
            return "Số lượng phòng trên tầng "
                    + floorNumber + " đã đầy!";
        }
        return "";
    }

    @Override
    public void incrementView(Integer roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null) {
            Integer currentView = room.getView() != null ? room.getView() : 0;
            room.setView(currentView + 1);
            roomRepository.save(room);
        }
    }

    @Override
    public List<Floor> getAllFloors() {
        return floorRepository.findAll();
    }

    @Override
    public List<Size> getAllSizes() {
        return sizeService.getAllSizes();
    }


    @Override
    @NotNull
    public List<Room> getAllRooms() {
        log.info("Getting all rooms");
        return roomRepository.findAll();
    }

    @Override
    public ListRoomResponse getAllRoomForSearch() {
        List<Room> rooms = roomRepository.findAllByIsDeletedIsFalse();
        return buildListRoomResponse(rooms);
    }

    public ListRoomResponse buildListRoomResponse(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return new ListRoomResponse(Collections.emptyList());
        }

        List<RoomResponseDto> roomDtos = rooms.stream()
                .map(this::buildRoomResponse)
                .collect(Collectors.toList());

        return new ListRoomResponse(roomDtos);
    }

    private RoomResponseDto buildRoomResponse(Room room) {
        if (room == null) return null;

        // Query floor và size từ repository
        Integer floorNumber = null;
        if (room.getFloorId() != null) {
            floorNumber = floorRepository.findById(room.getFloorId())
                    .map(floor -> floor.getFloorNumber())
                    .orElse(null);
        }

        Double size = null;
        if (room.getSizeId() != null) {
            try {
                Size sizeEntity = sizeService.getSizeById(room.getSizeId());
                size = sizeEntity != null ? sizeEntity.getSize() : null;
            } catch (Exception e) {
                size = null;
            }
        }

        return RoomResponseDto.builder()
                .roomId(room.getRoomId() != null ? room.getRoomId().longValue() : null)
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .bedType(room.getBedType())
                .floorNumber(floorNumber)
                .size(size)
                .roomDescription(room.getRoomDescription())
                .price(room.getPrice())
                .status(room.getStatus())
                .sold(room.getSold())
                .view(room.getView())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .isDeleted(room.getIsDeleted())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomById(Integer roomId) {
        log.info("Getting room by ID: {}", roomId);
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));
    }

    @Override
    public Room createRoom(Room room) {
        log.info("Creating new room: {}", room.getRoomNumber());

        // Kiểm tra room number có trùng không
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new RuntimeException("Phòng " + room.getRoomNumber() + " đã tồn tại. Vui lòng nhập số phòng khác.");
        }

        Room savedRoom = roomRepository.save(room);
        log.info("Created room with ID: {}", savedRoom.getRoomId());
        return savedRoom;
    }

    @Override
    public Room updateRoom(Integer roomId, Room room) {
        log.info("Updating room with ID: {}", roomId);

        Room existingRoom = getRoomById(roomId);

        // Kiểm tra room number có trùng không (trừ phòng hiện tại)
        if (!existingRoom.getRoomNumber().equals(room.getRoomNumber()) &&
                roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new RuntimeException("Phòng " + room.getRoomNumber() + " đã tồn tại. Vui lòng nhập số phòng khác.");
        }

        existingRoom.setRoomNumber(room.getRoomNumber());
        existingRoom.setRoomType(room.getRoomType());
        existingRoom.setBedType(room.getBedType());
        existingRoom.setFloorId(room.getFloorId());
        existingRoom.setSizeId(room.getSizeId());
        existingRoom.setPrice(room.getPrice());
        existingRoom.setStatus(room.getStatus());
        existingRoom.setRoomDescription(room.getRoomDescription());
        existingRoom.setSold(room.getSold() != null ? room.getSold() : existingRoom.getSold());
        existingRoom.setView(room.getView() != null ? room.getView() : existingRoom.getView());
        existingRoom.setMaxSizePeople(room.getMaxSizePeople());

        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Updated room with ID: {}", updatedRoom.getRoomId());
        return updatedRoom;
    }

    @Override
    @Transactional
    public void hardDeleteRoom(Integer roomId) {
        log.info("Hard deleting room with ID: {}", roomId);

        // Kiểm tra phòng có tồn tại không
        getRoomById(roomId);

        // Xóa các bản ghi liên quan trước khi xóa room
        // Xóa room views
        roomRepository.deleteRoomViewsByRoomId(roomId);

        // Xóa room furnishings
        roomRepository.deleteRoomFurnishingsByRoomId(roomId);

        // Xóa room images
        roomRepository.deleteRoomImagesByRoomId(roomId);

        // Xóa order details liên quan đến room này
        roomRepository.deleteOrderDetailsByRoomId(roomId);

        // Cuối cùng xóa room
        roomRepository.hardDeleteRoom(roomId);
        log.info("Hard deleted room with ID: {}", roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoomNumber(String roomNumber) {
        log.info("Checking if room number exists: {}", roomNumber);
        return roomRepository.existsByRoomNumber(roomNumber);
    }

    @Override
    public List<RoomHomepageResponseDto> getTop3Rooms() {
        List<Room> rooms = roomRepository.findTop3ByOrderBySoldDesc();
        List<RoomHomepageResponseDto> results = new ArrayList<>();

        for (Room room : rooms) {
            RoomHomepageResponseDto dto = new RoomHomepageResponseDto();
            dto.setRoomId(room.getRoomId());
            dto.setPrice(room.getPrice());
            dto.setRoomType(room.getRoomType());
            dto.setRoomDescription(room.getRoomDescription());

            List<RoomImage> roomImages = roomImageRepository.findByRoomId(room.getRoomId());
            if (roomImages != null && !roomImages.isEmpty()) {
                dto.setImageRoom(roomImages.get(0).getRoomImageUrl());
            }

            results.add(dto);
        }

        return results;
    }


    @Override
    public RoomDetailResponseDto getRoomDetailById(Integer roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            return null;
        }

        // Lấy thông tin kích thước phòng (size)
        Size size;
        try {
            size = sizeService.getSizeById(room.getSizeId());
        } catch (Exception e) {
            size = null;
        }

        // Tạo DTO trả về
        RoomDetailResponseDto dto = new RoomDetailResponseDto();
        dto.setRoomId(room.getRoomId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomType(room.getRoomType());
        dto.setBedType(room.getBedType());
        dto.setFloorNumber(room.getFloorId());
        dto.setSize(size != null ? size.getSize() : null);
        dto.setPrice(room.getPrice());
        dto.setStatus(room.getStatus());
        dto.setSold(room.getSold());
        dto.setView(room.getView());
        dto.setRoomDescription(room.getRoomDescription());
        dto.setMaxSizePeople(room.getMaxSizePeople());

        List<RoomImage> roomImages = roomImageRepository.findByRoomId(room.getRoomId());
        List<String> imageUrls = new ArrayList<>();

        if (roomImages != null && !roomImages.isEmpty()) {
            for (RoomImage roomImage : roomImages) {
                imageUrls.add(roomImage.getRoomImageUrl());
            }
        }

        dto.setImages(imageUrls);
        
        // Lấy danh sách room views
        List<String> roomViews = getRoomViewList(room.getRoomId());
        dto.setRoomViews(roomViews);

        return dto;
    }
}
