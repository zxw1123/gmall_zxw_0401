package com.atguigu.gmall0401.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

//@MapperScan(basePackages = "com.atguigu.gmall0401.gmall0401user.mapper")
@SpringBootApplication
public class Gmall0401OrderWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0401OrderWebApplication.class, args);
	}

}
