package com.docflow.docflow_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocFlowCoreApplication {

	public static void main(String[] args) {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			System.out.println("✅ Oracle driver found!");
		} catch (ClassNotFoundException e) {
			System.err.println("❌ Oracle driver NOT found!");
		}

		SpringApplication.run(DocFlowCoreApplication.class, args);
	}

}
