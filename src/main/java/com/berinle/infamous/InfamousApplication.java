package com.berinle.infamous;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

//	@Bean
//	@LoadBalanced
//	public RestTemplate restTemplate() {
//		return new RestTemplate();
//	}

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(
			@PathVariable String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}


	@GetMapping("/service-instances/{applicationName}/invoke")
	public ResponseEntity<?> invoke(@PathVariable String applicationName) {
		RestTemplate rest = new RestTemplate();
		List<ServiceInstance> sis = this.discoveryClient.getInstances(applicationName);
		if (!sis.isEmpty()) {
			String url = sis.get(0).getUri().toString();
			Object result = rest.postForEntity(url + "/health", null, Object.class);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}

		return ResponseEntity.ok("OK");

	}
}

