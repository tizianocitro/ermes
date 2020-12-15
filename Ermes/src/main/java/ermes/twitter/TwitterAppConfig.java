package ermes.twitter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitterAppConfig {
	@Bean
	public FilterRegistrationBean<TwitterFilter> twitterFilterRegistrationBean(TwitterFilter twitterFilter) {
		FilterRegistrationBean<TwitterFilter> registrationBean=new FilterRegistrationBean<>();
		registrationBean.setFilter(twitterFilter);
		registrationBean.addUrlPatterns(TwitterFilter.TWITTER_FILTER);

		return registrationBean;
	}
}
