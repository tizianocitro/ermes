package ermes.facebook;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacebookAppConfig {
	@Bean
	public FilterRegistrationBean<FacebookFilter> facebookFilterRegistrationBean(FacebookFilter facebookFilter) {
		FilterRegistrationBean<FacebookFilter> registrationBean=new FilterRegistrationBean<FacebookFilter>();
		registrationBean.setFilter(facebookFilter);
		registrationBean.addUrlPatterns(FacebookFilter.FACEBOOK_FILTER);
		
		return registrationBean;
	}
}
