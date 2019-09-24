package com.atguigu.gmall0401.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.atguigu.gmall0401.cart.mapper")
@SpringBootApplication
@ComponentScan(basePackages = "com.atguigu.gmall0401")
public class Gma0401CartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gma0401CartServiceApplication.class, args);
	}

}
