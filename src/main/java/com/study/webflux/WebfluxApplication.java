package com.study.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.study.webflux.voice.v2.VoiceV2Properties;

@SpringBootApplication
@EnableConfigurationProperties(VoiceV2Properties.class)
public class WebfluxApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxApplication.class, args);
	}

}
