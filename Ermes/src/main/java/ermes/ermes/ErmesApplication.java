package ermes.ermes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@ComponentScan({"ermes.facebook", "ermes.twitter", "ermes.telegram", "ermes.swagger"})
@PropertySources({
	@PropertySource("file:/Users/tizianocitro/SpringProperties/ermes.properties")
})
public class ErmesApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(ErmesApplication.class, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ErmesApplication.class);
	}
}
