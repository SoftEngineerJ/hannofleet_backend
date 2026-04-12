package com.hannofleet.backend.controller;

import com.hannofleet.backend.entity.User;
import com.hannofleet.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Ungültige Anmeldedaten"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Ungültige Anmeldedaten"));
        }

        String token = UUID.randomUUID().toString();
        long expires = System.currentTimeMillis() + 86400000;

        user.setToken(token);
        user.setTokenExpires(expires);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Nicht eingeloggt"));
        }
        String token = authHeader.substring(7);

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getToken()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Ungültiger Token"));
        }

        User user = userOpt.get();
        if (user.getTokenExpires() == null || System.currentTimeMillis() > user.getTokenExpires()) {
            user.setToken(null);
            user.setTokenExpires(null);
            userRepository.save(user);
            return ResponseEntity.status(401).body(Map.of("error", "Token abgelaufen"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userRepository.findAll().stream()
                    .filter(u -> token.equals(u.getToken()))
                    .findFirst()
                    .ifPresent(user -> {
                        user.setToken(null);
                        user.setTokenExpires(null);
                        userRepository.save(user);
                    });
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("role", user.getRole().name());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String role = userData.get("role");

        if (username == null || password == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Alle Felder erforderlich"));
        }

        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Benutzername bereits vergeben"));
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(User.Role.valueOf(role))
                .build();

        User saved = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("username", saved.getUsername());
        response.put("role", saved.getRole().name());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (id.equals(1L)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admin kann nicht gelöscht werden"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        String username = passwordData.get("username");
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        if (username == null || currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Alle Felder erforderlich"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Benutzer nicht gefunden"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Aktuelles Passwort ist falsch"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Passwort erfolgreich geändert"));
    }

    @PutMapping("/username")
    public ResponseEntity<?> changeUsername(@RequestBody Map<String, String> usernameData) {
        String currentUsername = usernameData.get("currentUsername");
        String newUsername = usernameData.get("newUsername");
        String password = usernameData.get("password");

        if (currentUsername == null || newUsername == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Alle Felder erforderlich"));
        }

        Optional<User> userOpt = userRepository.findByUsername(currentUsername);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Benutzer nicht gefunden"));
        }

        if (userRepository.existsByUsername(newUsername)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Benutzername bereits vergeben"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwort ist falsch"));
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        return ResponseEntity
                .ok(Map.of("success", true, "message", "Benutzername erfolgreich geändert", "username", newUsername));
    }
}
