package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
                    "SELECT category_id, category_name, category_icon, category_parent_id FROM categories WHERE category_parent_id IS NULL"
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
            "SELECT category_id, category_name, category_icon, category_parent_id FROM categories"
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
     * Map the ResultSet to Map Array
     * IMPORTANT!!! The ResultSet is closed by the jdbcTemplate after the RowMapper
     *
     * @param rs
     * @param rowNum
     * @return
     * @throws SQLException
     */
    private List<Map<String, Object>> __resultSet2MapArray(ResultSet rs, int rowNum)
            throws SQLException
    {
        List<Map<String, Object>> mapArray = new ArrayList<>();

        while (rs.next()) {
            Map<String, Object> rsMap = new HashMap<>();
            for (int col = 1; col <= rs.getMetaData().getColumnCount(); col++) {
                rsMap.put(rs.getMetaData().getColumnName(col), rs.getObject(col));
            }

            mapArray.add(rsMap);
        }

        return mapArray;
    }

    /**
     * Map the ResultSet to Map
     * IMPORTANT!!! The ResultSet is closed by the jdbcTemplate after the RowMapper
     *
     * @param rs
     * @param rowNum
     * @return
     * @throws SQLException
     */
    private Map<String, Object> __resultSet2Map(ResultSet rs, int rowNum)
            throws SQLException
    {
        Map<String, Object> rsMap = new HashMap<>();
        for (int col = 1; col <= rs.getMetaData().getColumnCount(); col++) {
            rsMap.put(rs.getMetaData().getColumnName(col), rs.getObject(col));
        }
        return rsMap;
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
