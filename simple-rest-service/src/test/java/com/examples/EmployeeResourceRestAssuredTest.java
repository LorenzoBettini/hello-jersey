package com.examples;

import static io.restassured.RestAssured.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.examples.model.Employee;
import com.examples.repository.EmployeeRepository;

import io.restassured.RestAssured;

public class EmployeeResourceRestAssuredTest extends JerseyTest {

	private static final String EMPLOYEES = "employees";

	@Mock
	private EmployeeRepository employeeRepository;

	@Override
	protected Application configure() {
		MockitoAnnotations.initMocks(this);
		// register only the EmployeeResource
		return new ResourceConfig(EmployeeResource.class)
			// we must make Jersey aware of our mapper
			// since we don't make it scan the whole package
			.register(NotFoundMapper.class)
			// inject the mock in our EmployeeResource
			.register(new AbstractBinder() {
				@Override
				protected void configure() {
					// differently from Guice,
					// bind(concrete).to(abstract)
					bind(employeeRepository)
						.to(EmployeeRepository.class);
				}
			});
	}

	@Before
	public void configureRestAssured() {
		// retrieve the base URI of the JerseyTest server
		RestAssured.baseURI = getBaseUri().toString();
	}

	@Test
	public void justForDemoCannotAccessAlsoMyResource() {
		given().
			accept(MediaType.TEXT_PLAIN).
		when().
			get("myresource").
		then().
			statusCode(404);
	}

	@Test
	public void testGetAllEmployees() {
		when(employeeRepository.findAll())
			.thenReturn(asList(
				new Employee("ID1", "First Employee", 1000),
				new Employee("ID2", "Second Employee", 2000)
			));

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
			"employees.employee[1].salary", equalTo("2000")
			);
	}


	@Test
	public void testGetOneEmployee() {
		when(employeeRepository.findOne("ID1"))
			.thenReturn(
				Optional.of(new Employee("ID1", "An Employee", 2000)));

		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES + "/ID1").
		then().
			statusCode(200).
			assertThat().
			body(
				"employee.id", equalTo("ID1"),
				"employee.name", equalTo("An Employee"),
				"employee.salary", equalTo("2000")
			);
	}

	@Test
	public void testGetOneEmployeeWithNonExistingId() {
		when(employeeRepository.findOne(Mockito.anyString()))
			.thenReturn(Optional.empty());

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
		when(employeeRepository.findAll())
			.thenReturn(asList(
				new Employee("ID1", "First Employee", 1000),
				new Employee("ID2", "Second Employee", 2000)
			));

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
		when(employeeRepository.findOne("ID1"))
			.thenReturn(
				Optional.of(new Employee("ID1", "An Employee", 2000)));

		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES + "/ID1").
		then().
			statusCode(200).
			assertThat().
			body(
				"id", equalTo("ID1"),
				"name", equalTo("An Employee"),
				"salary", equalTo(2000)
				// NOTE: "salary" retains its integer type in JSON
				// so it must be equal to 2000 NOT "2000"
			);
	}

	@Test
	public void testGetOneEmployeeWithNonExistingIdJSON() {
		when(employeeRepository.findOne(Mockito.anyString()))
			.thenReturn(Optional.empty());

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
	public void testCount() {
		List<Employee> employees = asList(new Employee(), new Employee());
		when(employeeRepository.findAll())
			.thenReturn(employees);

		when().
			get(EMPLOYEES + "/count").
		then().
			statusCode(200).
			assertThat().
			body(equalTo("" + employees.size()));
	}

	@Test
	public void testPostNewEmployee() {
		// values for the new Employee in the request body
		JsonObject newObject = Json.createObjectBuilder()
				.add("name", "passed name")
				.add("salary", 1000)
				.build();

		// when we pass an Employee with the values of the request body
		when(employeeRepository.save(new Employee(null, "passed name", 1000)))
			.thenReturn(new Employee("ID", "returned name", 2000));
			// the repository returns a new Employee object
			// possibly with different values
			// but for sure with a generated id

		given().
			contentType(MediaType.APPLICATION_JSON).
			body(newObject.toString()).
		when().
			post(EMPLOYEES).
		then().
			statusCode(201).
			assertThat().
			// make sure we return the Employee returned by the repository
			body(
				"id", equalTo("ID"),
				"name", equalTo("returned name"),
				"salary", equalTo(2000)
			).
			header("Location",
				response -> endsWith(EMPLOYEES + "/ID"));
	}
}
