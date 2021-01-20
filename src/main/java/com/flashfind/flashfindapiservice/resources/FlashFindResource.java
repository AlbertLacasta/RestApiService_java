package com.flashfind.flashfindapiservice.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashfind")
public class FlashFindResource {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /*************************************************************/
    /** TESTING                                                 **/
    /*************************************************************/

    /**
     *
     * @param user
     * @param id
     * @return
     */
    @GetMapping("/hello/{id}/{user}")
    public ResponseEntity<String> getHello(
            @PathVariable String user,
            @PathVariable int id
    ) {
        String response = "Hello " + user + " with id : " + id;
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     *
     * @return
     */
    @GetMapping("/items")
    public ResponseEntity< List<Map<String, Object>>> getItems() {
        try {
            List<Map<String, Object>> response = jdbcTemplate.queryForList(
            "select * from ff_items"
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*************************************************************/
    /** PRIVATE FUNCTIONS                                       **/
    /*************************************************************/

    private List<Map<String, Object>> __reultSet2MapArray(ResultSet rs, int rowNum)
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

        rs.close();

        return mapArray;
    }

}
