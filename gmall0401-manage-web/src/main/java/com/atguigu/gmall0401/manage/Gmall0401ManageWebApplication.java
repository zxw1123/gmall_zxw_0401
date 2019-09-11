package com.atguigu.gmall0401.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall0401")
public class Gmall0401ManageWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0401ManageWebApplication.class, args);
	}

}
