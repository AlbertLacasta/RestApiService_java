package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.Constants;
import com.flashfind.flashfindapiservice.utils.GoogleAuth;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONObject;
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
@RequestMapping("/api/public")
public class UserResource {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     *
     * @param userMap
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody String userMap) throws Exception {
        JSONObject json     = new JSONObject(userMap);
        String email        = json.getString("email");
        String password     = json.getString("password");
        if(email != null) {
            email.toLowerCase();
        }

        Map<String, Object> user = jdbcTemplate.queryForObject(
        "SELECT USER_ID, USER_EMAIL, USER_PASSWORD, USER_USERNAME FROM USERS WHERE USER_EMAIL = ?",
            new Object[]{email}, userRowMapped
        );

        // TODO: make in client side
        if(!password.equals(user.get("password"))) {
            throw new Exception("Invalid email/password.");
        }

        Map<String, String> tokenMap = __generateJWTToken(
            (Integer) user.get("user_id"),
            (String) user.get("email"),
            (String) user.get("username")
        );

        return new ResponseEntity<>(tokenMap, HttpStatus.OK);
    }

    /**
     *
     * @param userMap
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody String userMap) throws Exception {
        JSONObject json = new JSONObject(userMap);
        String email = json.getString("email");
        String password = json.getString("password");
        String username = json.getString("username");
        String salt = "";

        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        if(email != null) {
            email.toLowerCase();
        }

        if(!pattern.matcher(email).matches()) {
            throw new Exception("Invalid email");
        }

        Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM USERS WHERE USER_EMAIL = ?",
            new Object[]{email}, Integer.class
        );

        if(count > 0) {
            throw new Exception("Email already in use");
        }

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO USERS(USER_ID, USER_EMAIL, USER_PASSWORD, USER_SALT, USER_USERNAME, USER_REGISTER_DATE) VALUES(NEXTVAL(?), ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, "users_user_id_seq");
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, salt);
                ps.setString(5, username);
                ps.setDate(6, new java.sql.Date(System.currentTimeMillis()));

                return ps;
            }, keyHolder);

            Integer userId = (Integer) keyHolder.getKeys().get("USER_ID");

            return new ResponseEntity<>(__generateJWTToken(userId, email, username), HttpStatus.OK);
        } catch(Exception e) {
            throw new Exception(e);
            //throw new Exception("Invalid details. Failed to create account");
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
     * @param email
     * @param username
     * @return
     */
    private Map<String, String> __generateJWTToken(Integer userId, String email, String username) {
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.API_SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        long timestamp = System.currentTimeMillis();

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(timestamp + Constants.TOKEN_VALIDITY))
                .claim("userId", userId)
                .claim("email", email)
                .claim("username", username)
                .signWith(signatureAlgorithm, signingKey);

        Map<String, String> map = new HashMap<>();
        map.put("api_key", builder.compact());
        return map;
    }

    /**
     *
     */
    private RowMapper<HashMap<String, Object>> userRowMapped = ((rs, rowNum) -> {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_id", rs.getInt("USER_ID"));
        map.put("email", rs.getString("USER_EMAIL"));
        map.put("password", rs.getString("USER_PASSWORD"));
        map.put("username", rs.getString("USER_USERNAME"));
        return map;
    });

    /**
     *
     */
    private RowMapper<HashMap<String, Object>> rowMapped = ((rs, rowNum) -> {
        HashMap<String, Object> map = new HashMap<>();

        map.put("password", rs.getString("PASSWORD"));
        return map;
    });

}
