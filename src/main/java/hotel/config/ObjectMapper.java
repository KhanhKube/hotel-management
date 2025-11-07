package hotel.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@ComponentScan(basePackages = {"hotel.rest"})
public class ObjectMapper implements WebMvcConfigurer {

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Bean
	public Jackson2ObjectMapperBuilder jacksonBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

		// Use default camelCase naming strategy for cart endpoints
		// builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

		builder.featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

		builder.serializationInclusion(JsonInclude.Include.NON_NULL);

		// Configure JavaTimeModule with custom date format
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addDeserializer(LocalDateTime.class,
				new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
		javaTimeModule.addSerializer(LocalDateTime.class,
				new LocalDateTimeSerializer(DATE_TIME_FORMATTER));

		builder.modules(javaTimeModule);

		return builder;
	}
}