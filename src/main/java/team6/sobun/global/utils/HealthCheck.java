package team6.sobun.global.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/health-check")
public class HealthCheck {
    // EC2 상태검사용 200 뿌리기
    // 테스트

    @Value("${targetGroupHealthCheckUrl}")
    private String targetGroupHealthCheckUrl;

    @Value("${targetEndpoint}")
    private String targetEndpoint;
    private final RestTemplate restTemplate;

    @Autowired
    public HealthCheck(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    @ResponseBody
    public String checkHealthAndSendRequests() {
        // 대상 그룹 상태 검사
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(targetGroupHealthCheckUrl, String.class);

        if (healthResponse.getStatusCode().is2xxSuccessful()) {
            sendRequests(targetEndpoint, 200);
            return "Health check successful, sent requests!";
        } else {
            return "Health check failed!";
        }
    }

    private void sendRequests(String endpoint, int count) {
        for (int i = 0; i < count; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            System.out.println("Response: " + response.getStatusCodeValue());
        }
    }
}