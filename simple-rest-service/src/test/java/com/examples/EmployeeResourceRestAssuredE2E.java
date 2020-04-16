package com.examples;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * These tests assume that the server is already running. By default it uses the
 * base URI http://localhost:8080/myapp/. If set, it uses the system properties
 * "server.host" (default "http://localhost") and "server.port" (default "8080") to
 * connect to the server.
 * 
 * @author Lorenzo Bettini
 *
 */
public class EmployeeResourceRestAssuredE2E {

	private static final String BASE_URI =
			System.getProperty("server.host", "http://localhost") +
			":" +
			Integer.parseInt(System.getProperty("server.port", "8080")) +
			"/myapp/";
	private static final String EMPLOYEES = "employees";

	@BeforeClass
	public static void configureRestAssured() {
		Logger.
			getLogger(EmployeeResourceRestAssuredE2E.class.toString()).
			info("Using URL: " + BASE_URI);
		RestAssured.baseURI = BASE_URI;
	}

	@Test
	public void testRestEndPoints() {
		JsonObject jsonBody;

		// create a new Employee
		jsonBody = Json.createObjectBuilder()
				.add("name", "test employee")
				.add("salary", 1000)
				.build();

		Response response = given().
				contentType(MediaType.APPLICATION_JSON).
				body(jsonBody.toString()).
			when().
				post(EMPLOYEES);

		// get the ID of the new employee
		String id = response.body().path("id");
		String employeeURI = response.header("Location");

		assertThat(employeeURI, endsWith(id));

		// read the saved employee with the returned URI
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(employeeURI).
		then().
			statusCode(200).
			assertThat().
			body(
				"id", equalTo(id),
				"name", equalTo("test employee"),
				"salary", equalTo(1000)
			);

		// replace the employee
		jsonBody = Json.createObjectBuilder()
				.add("name", "modified employee")
				.add("salary", 3000)
				.build();

		given().
			contentType(MediaType.APPLICATION_JSON).
			body(jsonBody.toString()).
		when().
			put(employeeURI).
		then().
			statusCode(200).
			assertThat().
			body(
				"id", equalTo(id),
				"name", equalTo("modified employee"),
				"salary", equalTo(3000)
			);

		// read the replaced employee
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(employeeURI).
		then().
			statusCode(200).
			assertThat().
			body(
				"id", equalTo(id),
				"name", equalTo("modified employee"),
				"salary", equalTo(3000)
			);

		// delete the employee
		when().
			delete(employeeURI).
		then().
			statusCode(202).
			assertThat().
			body("name", equalTo("modified employee"));

		// make sure it's deleted
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(employeeURI).
		then().
			statusCode(404);
	}

}
