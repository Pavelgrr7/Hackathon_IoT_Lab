package com.tst.iotlab.auth;

import java.io.*;
import java.util.Map;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class UserManager {
    private final Map<String, String[]> users = new HashMap<>();

    public UserManager() {
        try {
            loadUsers("C:\\Users\\Pavel\\IdeaProjects\\iotlab\\users\\Users.txt");
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