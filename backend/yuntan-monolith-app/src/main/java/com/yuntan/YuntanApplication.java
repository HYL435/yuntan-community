package com.yuntan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement // 启动事务管理
@EnableAsync // 启动异步任务
public class YuntanApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuntanApplication.class, args);
    }

}
