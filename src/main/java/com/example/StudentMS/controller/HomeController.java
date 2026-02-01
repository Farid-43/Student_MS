package com.example.StudentMS.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class HomeController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Student Management System");
        info.put("version", "1.0.0");
        info.put("description", "REST API for Student Management with role-based access control");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("login", "POST /api/auth/login");
        endpoints.put("register", "POST /api/auth/register");
        endpoints.put("admin", "/api/admin/** (ADMIN role required)");
        endpoints.put("teacher", "/api/teacher/** (ADMIN or TEACHER role required)");
        endpoints.put("student", "/api/student/** (ADMIN, TEACHER, or STUDENT role required)");
        info.put("endpoints", endpoints);

        Map<String, String> defaultCredentials = new HashMap<>();
        defaultCredentials.put("admin", "admin / admin123");
        info.put("defaultCredentials", defaultCredentials);

        info.put("apiTester", "/api-tester.html");

        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Student Management System is running!");
        return ResponseEntity.ok(health);
    }
}
