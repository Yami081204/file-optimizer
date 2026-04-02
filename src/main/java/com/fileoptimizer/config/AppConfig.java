package com.fileoptimizer.config;

import com.fileoptimizer.app.controller.MainController;
import com.fileoptimizer.app.controller.DashboardController;

import java.util.HashMap;
import java.util.Map;


public class AppConfig {
    private final Map<Class<?>, Object> controllers = new HashMap<>();

    public Object resolveController(Class<?> type) {
        // Simple manual DI or instantiation
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate controller: " + type.getName(), e);
        }
    }
}
