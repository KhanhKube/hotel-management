package hotel.config;
import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CloudinaryConfig {

    private String Cloundinary = "cloudinary://577342716538944:ggo3_y8kZJs7Hlez_adsw6e7Pwo@dpkrl8ur1";

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Cloundinary);
    }
}

