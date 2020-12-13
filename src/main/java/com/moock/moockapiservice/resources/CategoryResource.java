package com.moock.moockapiservice.resources;

import com.moock.moockapiservice.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryResource {

    @GetMapping("")
    public String getAllCategories(HttpServletRequest request) {
        return "";
    }
}
