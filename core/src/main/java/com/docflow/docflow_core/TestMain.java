package com.docflow.docflow_core;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestMain {

    public static void main(String[] args){
        System.out.println(new BCryptPasswordEncoder().encode("Docflow#123"));
        System.out.println(new BCryptPasswordEncoder().matches("Docflow#123", "$2a$10$s7TSI5SSAO1UpQFn6VQQJ.6bN7caOfVUKpNBkjwKdEqwZ3WR4hQbq"));

    }
}
