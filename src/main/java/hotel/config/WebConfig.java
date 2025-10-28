package hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Ensure /images/ path is served from classpath:/static/images/
		registry.addResourceHandler("/images/**")
				.addResourceLocations("classpath:/static/images/");

		// Also ensure /img/ path
		registry.addResourceHandler("/img/**")
				.addResourceLocations("classpath:/static/img/");
	}
}
