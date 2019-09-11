package com.atguigu.gmall0401.gmall0401user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.atguigu.gmall0401.gmall0401user.mapper")
@SpringBootApplication
@EnableTransactionManagement
public class Gmall0401UserManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0401UserManageApplication.class, args);
	}

}
