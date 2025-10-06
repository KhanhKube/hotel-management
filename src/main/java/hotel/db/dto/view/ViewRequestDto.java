package hotel.db.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewRequestDto {

    @NotBlank(message = "Loại view không được để trống")
    @Size(max = 100, message = "Loại view không được vượt quá 100 ký tự")
    private String viewType;
}
