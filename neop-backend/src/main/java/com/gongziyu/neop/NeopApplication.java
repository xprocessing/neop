package com.gongziyu.neop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.gongziyu.neop.mapper")
@EnableScheduling
public class NeopApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeopApplication.class, args);
    }
}
