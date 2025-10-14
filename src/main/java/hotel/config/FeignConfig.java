package hotel.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FeignConfig implements WebMvcConfigurer {

	@Bean
	public HttpMessageConverters feignHttpMessageConverters(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);
		return new HttpMessageConverters(converter);
	}

	@Bean
	public Decoder feignDecoder(HttpMessageConverters converters) {
		return new SpringDecoder(() -> converters);
	}

	@Bean
	public Encoder feignEncoder(HttpMessageConverters converters) {
		return new SpringEncoder(() -> converters);
	}
}