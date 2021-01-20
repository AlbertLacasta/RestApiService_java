package com.flashfind.flashfindapiservice;

import com.flashfind.flashfindapiservice.AuthFilters;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlashFindApiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashFindApiServiceApplication.class, args);
    }
    @Bean
    public FilterRegistrationBean<AuthFilters> filterRegistrationBean() {
        FilterRegistrationBean<AuthFilters> registrationBean = new FilterRegistrationBean<>();
        AuthFilters authFilter = new AuthFilters();
        registrationBean.setFilter(authFilter);

        // Protected api's that has to receive a token to return a response
        registrationBean.addUrlPatterns("/api/public/*");

        return registrationBean;
    }

}
