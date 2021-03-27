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
import java.util.HashMap;
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
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) String user_id
    ) {
        try {
            // Get user id from token
            Claims decodedToken     = __decodeJWT(auth);
            int userId             = (int) decodedToken.get("userId");

            // If usr Id is provided thn use it
            if (user_id != null) {
                userId = Integer.parseInt(user_id);
            }

            Map<String, Object> response = jdbcTemplate.queryForMap(
                    "SELECT user_email, user_username, user_firstname, user_lastname, user_register_date, user_picture, " +
                        "(SELECT count(*) FROM favourites WHERE favourites.user_id = users.user_id) favourite_count, " +
                        "(SELECT count(*) FROM scanned WHERE scanned.user_id = users.user_id) scanned_count, " +
                        "(SELECT count(*) FROM products WHERE products.user_owned = users.user_id) products_count " +
                        "FROM users WHERE user_id = ?",
                    new Object[]{userId}
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    @GetMapping("/user/id")
    public ResponseEntity<Map<String, Integer>> getUserId(
            @RequestHeader("Authorization") String auth
    ) {
        try {
            // Get user id from token
            Claims decodedToken     = __decodeJWT(auth);
            int userId              = (int) decodedToken.get("userId");

            Map<String, Integer> responseMap = new HashMap<>();
            responseMap.put("user_id",  userId);

            return new ResponseEntity<>(responseMap, HttpStatus.OK);
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
