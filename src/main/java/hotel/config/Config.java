package hotel.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images từ thư mục ngoài project
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir + "/");
        
        // Serve static images từ resources (nếu cần)
        registry.addResourceHandler("/static/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
