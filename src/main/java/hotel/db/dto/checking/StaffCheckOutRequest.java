package hotel.db.dto.checking;

import lombok.Data;

@Data
public class StaffCheckOutRequest {
    private Integer orderDetailId;
    private String reportNote; // Ghi chú nếu có thiếu hụt hoặc lỗi
    private Boolean hasIssue; // Có vấn đề không
}
