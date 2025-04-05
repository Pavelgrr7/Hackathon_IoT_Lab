package com.tst.iotlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IotlabApplication {
	public static void main(String[] args) {
		SpringApplication.run(IotlabApplication.class, args);
	}
}
