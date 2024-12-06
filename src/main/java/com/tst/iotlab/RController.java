package com.tst.iotlab;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
//@CrossOrigin(origins = "http://192.168.0.139:8080")
@CrossOrigin(origins = "http://192.168.1.244:8080")
@RestController
@RequestMapping("/api")
public class RController {
    public static JsonObject formatToJson(String payload) {
        String[] vals = payload.split(";");
        StringBuilder myJSON = new StringBuilder();
        myJSON.append("{");
        for (String val : vals) {
            myJSON.append(val);
            myJSON.append(",");
        }
        myJSON.append("{");
        //JSON Parser from Gson Library
        JsonParser parser = new JsonParser();
        //Creating JSONObject from String using parser
        return parser.parse(String.valueOf(myJSON)).getAsJsonObject();
    }
//    Map<String, String> users = UserManager.getUsers();

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

//    @PostMapping("/data")
//    public Map<String, String> processData(@RequestBody Map<String, String> data) {
////        for (String key : data.keySet()) {
////            if (users.containsKey(key)) {
////                if (users.get(key).equals(data.get(key))) {
////                    data.put("Nice", "Success");
////                }
////            }
////        }
//        data.put("status", "processed");
//        return data;
//    }

    @PostMapping("/recivedData")
    public ResponseEntity<String> receiveData(@RequestBody Map<String, String> requestData) {
        System.out.println("Полученные данные: " + requestData);
        String response = "Данные успешно получены";
        return ResponseEntity.ok(response);
    }
}

