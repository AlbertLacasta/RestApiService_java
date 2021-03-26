package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserResource {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     *
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(
            @RequestHeader("Authorization") String auth
    ) {
        try {
            // Get user id from token
            Claims decodedToken     = __decodeJWT(auth);
            int user_id             = (int) decodedToken.get("userId");

            Map<String, Object> response = jdbcTemplate.queryForMap(
                    "SELECT user_email, user_username, user_firstname, user_lastname, user_register_date, user_picture, " +
                        "(SELECT count(*) FROM favourites WHERE favourites.user_id = users.user_id) favourite_count, " +
                        "(SELECT count(*) FROM scanned WHERE scanned.user_id = users.user_id) scanned_count, " +
                        "(SELECT count(*) FROM products WHERE products.user_owned = users.user_id) products_count " +
                        "FROM users WHERE user_id = ?",
                    new Object[]{user_id}
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


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
