package com.flashfind.flashfindapiservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringBootApplication
public class FlashFindApiServiceApplication {

    @CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public static void main(String[] args) {
        SpringApplication.run(FlashFindApiServiceApplication.class, args);
    }
    @Bean
    public FilterRegistrationBean<AuthFilters> filterRegistrationBean() {
        FilterRegistrationBean<AuthFilters> registrationBean = new FilterRegistrationBean<>();
        AuthFilters authFilter = new AuthFilters();
        registrationBean.setFilter(authFilter);

        // Protected api's that has to receive a token to return a response
        registrationBean.addUrlPatterns("/category/*");
        registrationBean.addUrlPatterns("/item/*");

        return registrationBean;
    }

}
