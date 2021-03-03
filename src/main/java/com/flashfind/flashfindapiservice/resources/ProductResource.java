package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.Constants;
import com.flashfind.flashfindapiservice.utils.QRCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@RestController
@RequestMapping("/item")
public class ProductResource {

    @Autowired
    JdbcTemplate jdbcTemplate;


    /****************************************************************/
    /** PRODUCTS                                                   **/
    /****************************************************************/

    /**
     *
     * @return
     */
    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                "SELECT products.product_id, product_title, products.user_owned, favourites.fav_id " +
                    "FROM products " +
                    "FULL OUTER JOIN favourites on favourites.product_id = products.product_id " +
                    "WHERE active = true " +
                    "ORDER BY date_created DESC"
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
    @PostMapping("/product")
    public ResponseEntity<String> createProduct(@RequestHeader("Authorization") String auth,  @RequestBody String userMap) {
        try {
            Claims decodedToken = __decodeJWT(auth);

            JSONObject json         = new JSONObject(userMap);

            // PRODUCT
            String product_title    = json.getString("product_title");
            String product_desc     = json.getString("product_desc");
            Boolean multiscan       = json.getBoolean("multiscan");
            int category_id         = json.getInt("category_id");
            int user_owned          = (int) decodedToken.get("userId");
            int user_created        = (int) decodedToken.get("userId");
            Date date_created       = new Date();

            // LOCATION
            String city             = json.getString("city");
            int zip                 = json.getInt("zip");
            int aprox_radius        = json.getInt("aprox_radius");
            int aprox_latitude      = json.getInt("aprox_latitude");
            int aprox_longitude     = json.getInt("aprox_longitude");

            // QR
            String qr_data          = json.getString("qr_data");
            byte[] qrByte           = QRCode.generateQR(qr_data);

            // TODO !!!!! ADD QR
            jdbcTemplate.update(
                    "INSERT INTO products(product_title, product_desc, multiscan, category_id, user_owned, " +
                            "user_created, date_created, city, zip, aprox_radius, aprox_latitude, aprox_longitude) " +
                            "VALUES(?, ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?)",
                    product_title, product_desc, multiscan, category_id, user_owned, user_created, date_created, city,
                    zip, aprox_radius, aprox_latitude, aprox_longitude
            );

            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param auth
     * @param product_id
     * @return
     */
    @PostMapping("/product/{product_id}/wishlist")
    public ResponseEntity<Integer> addToWishlist(
            @RequestHeader("Authorization") String auth,
            @PathVariable int product_id
    ) {
        try {
            // Get user id from token
            Claims decodedToken     = __decodeJWT(auth);
            int user_id             = (int) decodedToken.get("userId");

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO favourites(fav_id, product_id, user_id) VALUES(NEXTVAL(?), ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, "favourites_fav_id_seq");
                ps.setInt(2, product_id);
                ps.setInt(3, user_id);

                return ps;
            }, keyHolder);

            Integer fav_id = (Integer) keyHolder.getKeys().get("fav_id");

            return new ResponseEntity<>(fav_id, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search products
     * @return
     */
    @GetMapping("/search/products/{product}")
    public ResponseEntity<List<Map<String, Object>>> searchProducts(
            @PathVariable String product
    ) {
        try {
            String searchQuery = "%" + product.toUpperCase() + "%";

            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                    "SELECT product_id, product_title " +
                        "FROM products " +
                        "WHERE UPPER(product_title) LIKE ? " +
                        "OR UPPER(product_desc) LIKE ? " +
                        "ORDER BY product_id DESC " +
                        "LIMIT 15 ",
                    new Object[]{searchQuery, searchQuery}
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
    @GetMapping("/product/{product_id}")
    public ResponseEntity<Map<String, Object>> getProduct(
            @PathVariable Integer product_id
    ) {
        try {
            Map<String, Object> response = jdbcTemplate.queryForMap(
                    "SELECT product_id, categories.category_id, categories.category_name, " +
                            "active, product_title, product_desc, multiscan, products.user_owned, " +
                            "users.user_username, visit_count, aprox_radius, aprox_latitude, " +
                            "aprox_longitude, city, zip " +
                            "FROM products, categories, users " +
                            "WHERE product_id = ? " +
                            "AND products.category_id = categories.category_id " +
                            "AND products.user_owned = users.user_id",
                    new Object[]{product_id}
            );

            int visitCount = (int) response.get("visit_count");

            jdbcTemplate.update(
                    "UPDATE products SET visit_count = ? WHERE product_id = ?",
                    visitCount+1, product_id);

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
