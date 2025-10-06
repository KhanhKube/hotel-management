package hotel.db.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewResponseDto {

    private Long viewId;
    private String viewType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
