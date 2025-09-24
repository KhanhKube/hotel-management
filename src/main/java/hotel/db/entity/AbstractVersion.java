package hotel.db.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractVersion {
	protected String version = "1.0";

	@CreatedDate
	Date createdDate;

	@LastModifiedDate
	Date lastModifiedDate;
}
