package hotel.db.dto.checking;

import lombok.Data;

@Data
public class StaffCheckOutRequest {
    private Integer orderDetailId;
    private Boolean furnishingCheckCompleted; // Đã kiểm tra dụng cụ chưa (BẮT BUỘC = true)
    private String issueNote; // Ghi chú nếu có dụng cụ thiếu/hỏng (không bắt buộc)
}
