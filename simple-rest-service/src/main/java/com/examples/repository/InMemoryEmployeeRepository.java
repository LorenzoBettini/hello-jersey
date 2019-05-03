package com.examples.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.examples.model.Employee;

/**
 * An example repository implementation for employees.
 * 
 * In a real application this should be handled by a database.
 */
public class InMemoryEmployeeRepository implements EmployeeRepository {

	private List<Employee> employees = new LinkedList<>();

	public InMemoryEmployeeRepository() {
		// initialize the "db" with some contents
		employees.add(new Employee("ID1", "First Employee", 1000));
		employees.add(new Employee("ID2", "Second Employee", 2000));
		employees.add(new Employee("ID3", "Third Employee", 3000));
	}

	@Override
	public synchronized List<Employee> findAll() {
		return employees;
	}

	@Override
	public synchronized Optional<Employee> findOne(String id) {
		return employees.
				stream().
				filter(e -> e.getEmployeeId().equals(id)).
				findFirst();
	}

	public synchronized Employee save(Employee employee) {
		// dumb way of generating an automatic ID
		employee.setEmployeeId("ID" + (employees.size() + 1));
		employees.add(employee);
		return employee;
	}
}