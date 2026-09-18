package com.example.anagram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class})
public class AnagramApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnagramApplication.class, args);
    }
}
