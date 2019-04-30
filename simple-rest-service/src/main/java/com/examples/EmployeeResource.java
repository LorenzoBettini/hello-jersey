package com.examples;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.examples.model.Employee;
import com.examples.repository.EmployeeRepository;

/**
 * Root resource (exposed at "employees" path)
 */
@Path("employees")
public class EmployeeResource {

	@Inject
	private EmployeeRepository employeeRepository;

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}

	@GET
	// Defines that the next path parameter after "employees is
	// treated as a parameter and passed to the EmployeeResource
	// Allows to type http://localhost:8080/myapp/employees/ID1
	// ID1 will be treated as parameter "id" and passed to this method
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Employee getOneEmployee(@PathParam("id") String id) {
		return employeeRepository
			.findOne(id)
			.orElseThrow(() -> 
				new NotFoundException("Employee not found with id " + id));
	}

	// returns the number of employees
	// Use http://localhost:8080/myapp/employees/count
	// to get the total number of records
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		return String.valueOf(employeeRepository.findAll().size());
	}
}
