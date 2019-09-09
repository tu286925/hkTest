package com.hx.hkTest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hx.hkTest.mapper")
public class HkTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(HkTestApplication.class, args);
	}
}
