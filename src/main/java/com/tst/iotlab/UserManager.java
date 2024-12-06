package com.tst.iotlab;

import java.io.*;
import java.util.Map;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class UserManager {
    private Map<String, String[]> users = new HashMap<>();

    public UserManager() {
        try {
            loadUsers("users/Users.txt");
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void loadUsers(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String username = parts[0];
                    String password = parts[1];
                    String role = parts[2];
                    users.put(username, new String[] {password, role});
                }
            }
        }
    }

    public boolean authenticate(String username, String password) {
        if (users.containsKey(username)) {
            return users.get(username)[0].equals(password);
        }
        return false;
    }

    public boolean isAdmin(String username) {
        return users.containsKey(username) && "admin".equals(users.get(username)[1]);
    }
}

//public class UserManager {
//    static Map<String, String> lines;
//    public UserManager(File file) {
//        try {
//            String regex = ":";
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            lines = br.lines()
//                    .map(line -> line.split(regex))
//                    .collect(Collectors.toMap(
//                            arr -> arr[0], // Ключ — имя пользователя
//                            arr -> arr[1]  // Значение — пароль
//                    ));
//        } catch (IOException e) {
//            lines = null;
//            e.printStackTrace();
//        }
//    }
//
//    public static Map<String, String> getUsers(){
//        return lines;
//    }
//}
