package com.berinle.infamous;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

@EnableDiscoveryClient
@SpringBootApplication
public class InfamousApplication {

  public static void main(String[] args) {
    SpringApplication.run(InfamousApplication.class, args);
  }

}

@RestController
class ServiceInstanceRestController {

  @Autowired
  private DiscoveryClient discoveryClient;

  private ServiceInstance getServiceInstance(String serviceId) {
    List<ServiceInstance> list = this.discoveryClient.getInstances(serviceId);
    if (!list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  @RequestMapping("/service-instances/{applicationName}")
  public List<ServiceInstance> serviceInstancesByApplicationName(
      @PathVariable String applicationName) {
    return this.discoveryClient.getInstances(applicationName);
  }


  @GetMapping("/service-instances/{applicationName}/health")
  public ResponseEntity<?> invoke(@PathVariable String applicationName) {
    RestTemplate rest = new RestTemplate();
    ServiceInstance si = getServiceInstance(applicationName);
    if (si == null) {
      return ResponseEntity.ok("OK");
    }

    String url = si.getUri().toString();
    return rest.getForEntity(url + "/actuator/health", Object.class);
  }

  @GetMapping("/jokes/{applicationName}")
  public ResponseEntity<?> jokes(@PathVariable String applicationName, @RequestParam(required = false) Integer size) {
    RestTemplate rest = new RestTemplate();
    ServiceInstance si = getServiceInstance(applicationName);
    if (si == null) {
      return ResponseEntity.ok("OK");
    }

    String url = si.getUri().toString();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString("user:changeme".getBytes()));
    HttpEntity requestEntity = new HttpEntity<>(null, headers);

    return rest.exchange(url + "/jokes", HttpMethod.GET, requestEntity, Object.class);
  }
}

