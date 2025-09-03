package com.wky.controller;

import cn.hutool.json.JSONUtil;
import com.wky.model.dto.LoginRequest;
import com.wky.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author wky
 * @date 2025/09/03
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {

    private final RestTemplate restTemplate;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            String url = loginRequest.getUrl();
            ResponseEntity<String> healthcheckResp = restTemplate.getForEntity(url + "/api/v2/healthcheck", String.class);
            if (!healthcheckResp.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(500).body("Service is not available");
            }
            String tenant = loginRequest.getTenant();
            ResponseEntity<String> getTenantResp = restTemplate.getForEntity(url + "/api/v2/tenants/" + tenant, String.class);
            if (!getTenantResp.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(500).body("Invalid tenant");
            }
            String token = authService.createToken(url, tenant);
            return ResponseEntity.ok(JSONUtil.parse(Map.of("token", token)).toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("登录失败，请检查服务地址和租户是否正确");
        }
    }

}
