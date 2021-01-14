package com.flashfind.flashfindapiservice.resources;

import com.moock.moockapiservice.Constants;
import com.moock.moockapiservice.domain.User;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flashfind")
public class FlashFindResource {

    @GetMapping("/hello/{id}/{user}")
    public ResponseEntity<String> getHello(
            @PathVariable String user,
            @PathVariable int id
    ) {
        String response = "Hello " + user + " with id : " + id;
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /*************************************************************/
    /** PRIVATE FUNCTIONS                                       **/
    /*************************************************************/
    /**
     *  TOKEN GENERATOR
     *
     * @param user
     * @return
     */
    private Map<String, String> __generateJWTToken(User user) {
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
                .claim("userId", user.getUserId())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .signWith(signatureAlgorithm, signingKey);

        Map<String, String> map = new HashMap<>();
        map.put("token", builder.compact());
        return map;

    }
}
