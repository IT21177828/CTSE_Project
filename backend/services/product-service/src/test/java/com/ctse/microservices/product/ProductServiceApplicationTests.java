package com.ctse.microservices.product;

import com.ctse.microservice.product.ProductServiceApplication;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;


@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = ProductServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
	@LocalServerPort
	private int port;

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mongoDBContainer.start();
		System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
	}

	@Test
	void shouldCreateProduct() {
		String requestBody = """
				{
				    "name":"Iphone Xs Max pro 2021",
				    "description":"The newly released iphone xs",
				    "price": 700.00
				}
				""";
		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", org.hamcrest.Matchers.notNullValue())
				.body("name", org.hamcrest.Matchers.equalTo("Iphone Xs Max pro 2021"))
				.body("description", org.hamcrest.Matchers.equalTo("The newly released iphone xs"))
				.body("price", org.hamcrest.Matchers.equalTo(700.00f));

	}

}