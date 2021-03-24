package com.flashfind.flashfindapiservice.resources;

import com.flashfind.flashfindapiservice.Constants;
import com.flashfind.flashfindapiservice.utils.QRCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
     * Minimal list of products, it receive a optional parameter for search products that contain a word
     * @return
     */
    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getProducts(@RequestParam(required = false) String query) {
        try {
            String searchQuery = "%";
            if (query != null) {
                searchQuery += query + "%";
            }

            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                "SELECT products.product_id, product_title, products.user_owned, favourites.fav_id, " +
                    "(SELECT picture_data FROM pictures_product WHERE pictures_product.product_id = products.product_id LIMIT 1 ) picture_data " +
                    "FROM products " +
                    "FULL OUTER JOIN favourites on favourites.product_id = products.product_id " +
                    "WHERE active = true " +
                    "AND UPPER(product_title) LIKE ? " +
                    "OR UPPER(product_desc) LIKE ? " +
                    "ORDER BY date_created DESC",
                    new Object[]{searchQuery, searchQuery}
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the featured products
     * MAX(15)
     * @return
     */
    @GetMapping("/feature/products")
    public ResponseEntity<List<Map<String, Object>>> getFeatureProducts() {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                "SELECT products.product_id, product_title, products.user_owned, favourites.fav_id, "+
                    "(SELECT picture_data FROM pictures_product WHERE pictures_product.product_id = products.product_id LIMIT 1 ) picture_data " +
                    "FROM features, products " +
                    "FULL OUTER JOIN favourites on favourites.product_id = products.product_id " +
                    "WHERE features.product_id = products.product_id " +
                    "AND products.active = true " +
                    "ORDER BY products.date_created DESC " +
                    "LIMIT 15"
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO; move to userResources with path user/{user_id}/products/favourite
    @GetMapping("/products/user/{user_id}/favourites")
    public ResponseEntity<List<Map<String, Object>>> getFavouriteProductsByUser(@PathVariable int user_id) {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                    "SELECT products.product_id, product_title, products.user_owned, favourites.fav_id " +
                        "FROM products " +
                        "FULL OUTER JOIN favourites on favourites.product_id = products.product_id " +
                        "WHERE active = true " +
                        "AND favourites.user_id = ? " +
                        "ORDER BY date_created DESC",
                    new Object[]{user_id}
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO; move to userResources with path user/{user_id}/products/favourite
    @GetMapping("/products/user/{user_id}/scanned")
    public ResponseEntity<List<Map<String, Object>>> getScannedProductsByUser(@PathVariable int user_id) {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                    "SELECT products.product_id, product_title, products.user_owned, favourites.fav_id " +
                            "FROM products " +
                            "FULL OUTER JOIN favourites on favourites.product_id = products.product_id " +
                            "FULL OUTER JOIN scanned on scanned.product_id = products.product_id " +
                            "WHERE active = true " +
                            "AND scanned.user_id = ? " +
                            "ORDER BY date_created DESC",
                    new Object[]{user_id}
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/products/user/{user_id}")
    public ResponseEntity<List<Map<String, Object>>> getProductsByUser(@PathVariable int user_id) {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
                    "SELECT products.product_id, product_title, products.user_owned, favourites.fav_id " +
                            "FROM products " +
                            "FULL OUTER JOIN favourites on favourites.product_id = products.product_id " +
                            "WHERE active = true " +
                            "AND products.user_owned = ? " +
                            "ORDER BY date_created DESC",
                    new Object[]{user_id}
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
    public ResponseEntity<Map<String, Number>> createProduct(
            @RequestHeader("Authorization") String auth,
            @RequestBody String data
    ) {
        try {
            Claims decodedToken = __decodeJWT(auth);

            JSONObject json         = new JSONObject(data);

            // PRODUCT
            String product_title    = json.getString("product_title");
            String product_desc     = json.getString("product_desc");
            Boolean multiscan       = json.getBoolean("multiscan");
            int category_id         = json.getInt("category_id");
            int user_owned          = (int) decodedToken.get("userId");
            int user_created        = (int) decodedToken.get("userId");

            // LOCATION
            String city             = json.getString("city");
            int zip                 = json.getInt("zip");
            int aprox_radius        = json.getInt("aprox_radius");
            double aprox_latitude      = json.getDouble("aprox_latitude");
            double aprox_longitude     = json.getDouble("aprox_longitude");

            // QR
            String qr_data          = json.getString("qr_data");
            String qrBase64           = QRCode.byteArr2Base64(QRCode.generateQR(qr_data));

            // TODO: Add qrData
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO products(date_created, product_title, product_desc, multiscan, category_id, user_owned, " +
                        "user_created, city, zip, aprox_radius, aprox_latitude, aprox_longitude, qr_code) " +
                        "VALUES(CURRENT_DATE, ?, ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1,product_title);
                ps.setString(2,product_desc);
                ps.setBoolean(3,multiscan);
                ps.setInt(4,category_id);
                ps.setInt(5,user_owned);
                ps.setInt(6,user_created);
                ps.setString(7,city);
                ps.setInt(8,zip);
                ps.setInt(9,aprox_radius);
                ps.setDouble(10,aprox_latitude);
                ps.setDouble(11,aprox_longitude);
                ps.setString(12,qrBase64);

                return ps;
            }, keyHolder);

            Map<String, Number> response = new HashMap<>();
            response.put("product_id", (Number) keyHolder.getKeys().get("product_id"));
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    @GetMapping("/product/{product_id}/image")
    public ResponseEntity<Map<String, String>> getProductImage(
        @PathVariable int product_id
    ) {
        Map<String, String> responseMap = new HashMap<>();
        try {
            Map<String, Object> response = jdbcTemplate.queryForMap(
                "SELECT picture_data FROM pictures_product " +
                    "WHERE product_id = ? " +
                    "LIMIT 1 ",
                    new Object[]{product_id}
            );

            responseMap.put("picture_data",  response.get("picture_data").toString());
            return new ResponseEntity<>(responseMap,HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            responseMap.put("picture_data", null);
            return new ResponseEntity<>(responseMap,HttpStatus.OK);
        }
    }

    @PostMapping("/product/{product_id}/image")
    public ResponseEntity<String> createProduct(
            @RequestBody String data,
            @PathVariable int product_id
    ) {
        try {
            JSONObject json         = new JSONObject(data);
            String imageData        = json.getString("image_data");
            jdbcTemplate.update(
                    "INSERT INTO pictures_product(product_id, picture_data) VALUES(?, ?)",
                    product_id, imageData
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
            @PathVariable String product_id
    ) {
        try {
            int productId = Integer.parseInt(product_id);

            Map<String, Object> response = jdbcTemplate.queryForMap(
                    "SELECT product_id, categories.category_id, categories.category_name, " +
                            "active, product_title, product_desc, multiscan, products.user_owned, " +
                            "users.user_username, visit_count, aprox_radius, aprox_latitude, " +
                            "aprox_longitude, city, zip, qr_code, (SELECT count(*) FROM favourites WHERE product_id = products.product_id) favourite_count " +
                            "FROM products, categories, users " +
                            "WHERE product_id = ? " +
                            "AND products.category_id = categories.category_id " +
                            "AND products.user_owned = users.user_id",
                    new Object[]{productId}
            );

            int visitCount = (int) response.get("visit_count");

            jdbcTemplate.update(
                    "UPDATE products SET visit_count = ? WHERE product_id = ?",
                    visitCount+1, productId);

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
