package com.moock.moockapiservice;

import com.moock.moockapiservice.filters.AuthFilters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MoockApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoockApiServiceApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean<AuthFilters> filterRegistrationBean() {
		FilterRegistrationBean<AuthFilters> registrationBean = new FilterRegistrationBean<>();
		AuthFilters authFilter = new AuthFilters();
		registrationBean.setFilter(authFilter);

		/* Protected api's that has to receive a token to return a response */
		registrationBean.addUrlPatterns("/api/categories/*");
		registrationBean.addUrlPatterns("/api/items/*");
		registrationBean.addUrlPatterns("/api/search/*");

		return registrationBean;
	}
}
