package ermes.telegram;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramAppConfig {
	@Bean
	public FilterRegistrationBean<TelegramFilter> telegramFilterRegistrationBean(TelegramFilter telegramFilter) {
		FilterRegistrationBean<TelegramFilter> registrationBean=new FilterRegistrationBean<>();
		registrationBean.setFilter(telegramFilter);
		registrationBean.addUrlPatterns(TelegramFilter.TELEGRAM_FILTER);
		
		return registrationBean;
	}
}
