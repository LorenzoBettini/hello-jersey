package com.examples;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class EmployeeResourceRestAssuredIT {

	private static final String EMPLOYEES = "employees";
	private HttpServer server;

	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.baseURI = Main.BASE_URI;
	}

	@Before
	public void setUp() throws Exception {
		server = Main.startServer();
	}

	@After
	public void tearDown() throws Exception {
		server.shutdownNow();
	}

	@Test
	public void testGetAllEmployees() {
		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES).
		then().
			statusCode(200).
			assertThat().
			body(
			"employees.employee[0].id", equalTo("ID1"),
			"employees.employee[0].name", equalTo("First Employee"),
			"employees.employee[0].salary", equalTo("1000"),
			"employees.employee[1].id", equalTo("ID2"),
			"employees.employee[1].name", equalTo("Second Employee"),
			"employees.employee[1].salary", equalTo("2000"),
			"employees.employee[2].id", equalTo("ID3"),
			"employees.employee[2].name", equalTo("Third Employee"),
			"employees.employee[2].salary", equalTo("3000")
			);
	}

	@Test
	public void testGetAllEmployeesWithRootPaths() {
		// a variation of the above test showing how to
		// test several XML elements
		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES).
		then().
			statusCode(200).
			assertThat().
				root("employees.employee[0]").
				body(
					"id", equalTo("ID1"),
					"name", equalTo("First Employee"),
					"salary", equalTo("1000")
				).
				root("employees.employee[1]").
				body(
					"id", equalTo("ID2")
					// similar assertions for the other fields
				);
	}

	@Test
	public void testGetOneEmployee() {
		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES + "/ID2").
		then().
			statusCode(200).
			assertThat().
			body(
				"employee.id", equalTo("ID2"),
				"employee.name", equalTo("Second Employee"),
				"employee.salary", equalTo("2000")
			);
	}

	@Test
	public void testGetOneEmployeeWithNonExistingId() {
		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES + "/foo").
		then().
			statusCode(404). // Status code: Not Found
			contentType(MediaType.TEXT_PLAIN).
			body(equalTo("Employee not found with id foo"));
	}

	@Test
	public void testGetAllEmployeesJSON() {
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES).
		then().
			statusCode(200).
			assertThat().
			body(
				"id[0]", equalTo("ID1"),
				"name[0]", equalTo("First Employee"),
				"salary[0]", equalTo(1000),
				// NOTE: "salary" retains its integer type in JSON
				// so it must be equal to 1000 NOT "1000"
				"id[1]", equalTo("ID2"),
				"name[1]", equalTo("Second Employee")
				// other checks omitted
			);
	}

	@Test
	public void testGetOneEmployeeJSON() {
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES + "/ID2").
		then().
			statusCode(200).
			assertThat().
			body(
				"id", equalTo("ID2"),
				"name", equalTo("Second Employee"),
				"salary", equalTo(2000)
				// NOTE: "salary" retains its integer type in JSON
				// so it must be equal to 2000 NOT "2000"
			);
	}

	@Test
	public void testGetOneEmployeeWithNonExistingIdJSON() {
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES + "/foo").
		then().
			statusCode(404). // Status code: Not Found
			contentType(MediaType.TEXT_PLAIN).
			body(equalTo("Employee not found with id foo"));
	}

	@Test
	public void justForDemoCanAccessAlsoMyResource() {
		given().
			accept(MediaType.TEXT_PLAIN).
		when().
			get("myresource").
		then().
			statusCode(200).
			assertThat().
				contentType(MediaType.TEXT_PLAIN).
				and().
				body(equalTo("Got it!"));
	}
}