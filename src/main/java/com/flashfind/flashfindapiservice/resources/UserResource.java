package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.types.GoogleAuth;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class UserResource {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public static final String API_SECRET_KEY = "flashfindappapikey";

    public static final long TOKEN_VALIDITY = 2 * 60 * 60 * 1000;

    /**
     *
     * @param userMap
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, Object> userMap) throws Exception {
        String email        = (String) userMap.get("email");
        String password     = (String) userMap.get("password");

        if(email != null) {
            email.toLowerCase();
        }
        Map<String, Object> user = jdbcTemplate.queryForObject(
        "SELECT USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD FROM FF_USERS WHERE EMAIL = ?",
            new Object[]{email}, rowMapped
        );

        if(!BCrypt.checkpw(password, (String) user.get("password"))) {
            throw new Exception("Invalid email/password.");
        }

        Map<String, String> tokenMap = __generateJWTToken(
                (Integer) user.get("user_id"),
                (String) user.get("first_name"),
                (String) user.get("last_name"),
                (String) user.get("email")
        );

        return new ResponseEntity<>(tokenMap, HttpStatus.OK);
    }

    /**
     *
     * @param userMap
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody Map<String, Object> userMap) throws Exception {
        String firstName    = (String) userMap.get("firstName");
        String lastName     = (String) userMap.get("lastName");
        String email        = (String) userMap.get("email");
        String password     = (String) userMap.get("password");

        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        if(email != null) {
            email.toLowerCase();
        }

        if(!pattern.matcher(email).matches()) {
            throw new Exception("Invalid email");
        }

        Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM FF_USERS WHERE EMAIL = ?",
            new Object[]{email}, Integer.class
        );

        if(count > 0) {
            throw new Exception("Email already in use");
        }

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO FF_USERS(USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES(NEXTVAL('FF_USERS_SEQ'), ?, ?, ?, ?)"
                        , Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, firstName);
                        ps.setString(2, lastName);
                        ps.setString(3, email);
                        ps.setString(4, password);
                return ps;
            }, keyHolder);

            Integer userId = (Integer) keyHolder.getKeys().get("USER_ID");
            return new ResponseEntity<>(__generateJWTToken(userId, firstName, lastName, email), HttpStatus.OK);
        } catch(Exception e) {
            throw new Exception("Invalid details. Failed to create account");
        }
    }

    /**
     *
     * @param tokenId
     * @return
     * @throws Exception
     */
    @GetMapping("/tokensignin")
    public ResponseEntity<String> tokenSignIn( @PathVariable String tokenId)
            throws Exception
    {
        GoogleIdToken.Payload payload = GoogleAuth.verify(tokenId);

        return new ResponseEntity<>("", HttpStatus.OK);
    };

    /**
     * Generate token
     *
     * @param userId
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    private Map<String, String> __generateJWTToken(Integer userId, String firstName, String lastName, String email) {
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(API_SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        long timestamp = System.currentTimeMillis();

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(timestamp + TOKEN_VALIDITY))
                .claim("userId", userId)
                .claim("email", email)
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .signWith(signatureAlgorithm, signingKey);

        Map<String, String> map = new HashMap<>();
        map.put("api_key", builder.compact());
        return map;
    }

    /**
     *
     */
    private RowMapper<Map<String, Object>> rowMapped = ((rs, rowNum) -> {
        Map<String, Object> rsMap = new HashMap<>();

        if(rs.next()) {
            for (int col = 1; col <= rs.getMetaData().getColumnCount(); col++) {
                rsMap.put(rs.getMetaData().getColumnName(col), rs.getObject(col));
            }
        }

        rs.close();

        return rsMap;
    });

}
