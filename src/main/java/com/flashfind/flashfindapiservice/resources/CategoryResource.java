package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     *
     * @return
     */
    @GetMapping("/main/categories")
    public ResponseEntity<List<Map<String, Object>>> getMainCategories() {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                    "SELECT category_id, category_name, category_icon, category_color, category_parent_id FROM categories WHERE category_parent_id IS NULL ORDER BY category_name"
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO:!!!!!!!!
     *
     * @return
     */
    @GetMapping("/tree/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
            "SELECT category_id, category_name, category_icon, category_parent_id FROM categories "
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /****************************************************************/
    /** PRIVATE FUNCTIONS                                       **/
    /****************************************************************/

    /**
     *
     * @param jwt
     * @return
     */
    private static Claims __decodeJWT(String jwt) {
        jwt = jwt.replace("Bearer ", "");

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.API_SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(jwt).getBody();
        return claims;
    }

}
