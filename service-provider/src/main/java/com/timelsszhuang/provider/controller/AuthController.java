package com.timelsszhuang.provider.controller;

import com.timelsszhuang.provider.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关功能
 *
 * @author timelsszhuang
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * 简化示例，实际项目中应该从数据库验证用户信息
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {
        logger.info("用户登录请求: {}", loginRequest.getUsername());

        Map<String, Object> response = new HashMap<>();

        // 简化的验证逻辑（实际应该查询数据库）
        if (isValidUser(loginRequest.getUsername(), loginRequest.getPassword())) {
            // 生成 Token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", "USER");

            String token = jwtUtil.generateToken(loginRequest.getUsername(), extraClaims);

            response.put("code", 200);
            response.put("message", "登录成功");

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenType", "Bearer");
            data.put("username", loginRequest.getUsername());
            data.put("expiresIn", 24 * 60 * 60); // 秒

            response.put("data", data);
            response.put("timestamp", System.currentTimeMillis());

            logger.info("用户 {} 登录成功，生成 Token", loginRequest.getUsername());
        } else {
            response.put("code", 401);
            response.put("message", "用户名或密码错误");
            response.put("data", null);
            response.put("timestamp", System.currentTimeMillis());

            logger.warn("用户 {} 登录失败：用户名或密码错误", loginRequest.getUsername());
        }

        return response;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("用户注册请求: {}", registerRequest.getUsername());

        Map<String, Object> response = new HashMap<>();

        // 简化的注册逻辑（实际应该存储到数据库）
        if (registerRequest.getUsername() != null &&
            !registerRequest.getUsername().isEmpty() &&
            registerRequest.getPassword() != null &&
            registerRequest.getPassword().length() >= 6) {

            response.put("code", 200);
            response.put("message", "注册成功");

            Map<String, Object> data = new HashMap<>();
            data.put("username", registerRequest.getUsername());
            data.put("email", registerRequest.getEmail());

            response.put("data", data);
            response.put("timestamp", System.currentTimeMillis());

            logger.info("用户 {} 注册成功", registerRequest.getUsername());
        } else {
            response.put("code", 400);
            response.put("message", "注册失败：用户名或密码不符合要求");
            response.put("data", null);
            response.put("timestamp", System.currentTimeMillis());

            logger.warn("用户 {} 注册失败：参数不合法", registerRequest.getUsername());
        }

        return response;
    }

    /**
     * 验证 Token
     */
    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);

                response.put("code", 200);
                response.put("message", "Token 有效");

                Map<String, Object> data = new HashMap<>();
                data.put("valid", true);
                data.put("username", username);

                response.put("data", data);
            } else {
                response.put("code", 401);
                response.put("message", "Token 无效或已过期");
                response.put("data", Map.of("valid", false));
            }
        } else {
            response.put("code", 400);
            response.put("message", "缺少或无效的 Authorization 头");
            response.put("data", Map.of("valid", false));
        }

        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 生成测试用 Token
     */
    @GetMapping("/generate-test-token")
    public Map<String, Object> generateTestToken(@RequestParam(defaultValue = "testuser") String username) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "TEST_USER");

        String token = jwtUtil.generateToken(username, extraClaims);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "测试 Token 生成成功");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        data.put("username", username);
        data.put("expiresIn", 24 * 60 * 60);
        data.put("fullToken", "Bearer " + token);

        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());

        logger.info("生成测试 Token: username={}", username);

        return response;
    }

    /**
     * 简化的用户验证逻辑
     * 实际项目中应该查询数据库
     */
    private boolean isValidUser(String username, String password) {
        // 演示用的固定用户
        // 实际应该查询数据库进行验证
        return ("admin".equals(username) && "admin123".equals(password)) ||
               ("user".equals(username) && "user123".equals(password)) ||
               ("test".equals(username) && "test123".equals(password));
    }

    /**
     * 登录请求对象
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 注册请求对象
     */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

