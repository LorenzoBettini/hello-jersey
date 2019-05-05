package com.examples.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.model.Employee;
import com.examples.repository.EmployeeRepository;

public class EmployeeServiceImplTest {

	@Mock
	private EmployeeRepository employeeRepository;

	@InjectMocks
	private EmployeeServiceImpl employeeService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAllEmployees() {
		Employee e1 = new Employee();
		Employee e2 = new Employee();
		when(employeeRepository.findAll())
			.thenReturn(Arrays.asList(e1, e2));

		assertThat(employeeService.allEmployees())
			.containsExactly(e1, e2);
	}

	@Test
	public void testGetEmployeeByIdWhenEmployeeIsFound() {
		Employee employee = new Employee();
		when(employeeRepository.findOne(anyString()))
			.thenReturn(Optional.of(employee));

		assertThat(employeeService.getEmployeeById("an id"))
			.isEqualTo(employee);
	}

	@Test
	public void testGetEmployeeByIdWhenEmployeeIsNotFound() {
		when(employeeRepository.findOne(anyString()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> employeeService.getEmployeeById("an id"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("Employee not found with id an id");
	}

	@Test
	public void testAddEmployeeWhenIdIsNullReturnsTheSavedEmployee() {
		// the id of the employee to save must be null
		Employee toAdd = new Employee();
		// we don't care about the contents of the saved employee
		Employee toReturn = new Employee();

		when(employeeRepository.save(toAdd))
			.thenReturn(toReturn);

		assertThat(employeeService.addEmployee(toAdd))
			.isSameAs(toReturn);
	}

	@Test
	public void testAddEmployeeWhenIdIsNotNull() {
		assertThatThrownBy(() -> employeeService.addEmployee(new Employee("ID", "", 1000)))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Unexpected id specification for Employee");

		verifyNoMoreInteractions(employeeRepository);
	}

	@Test
	public void testAddEmployeeWhenArgumentIsNull() {
		assertThatThrownBy(() -> employeeService.addEmployee(null))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Missing values for Employee");

		verifyNoMoreInteractions(employeeRepository);
	}

	@Test
	public void testReplaceEmployeeWhenEmployeeIsFoundReturnsTheSavedEmployee() {
		// the id of the employee to save must be null
		Employee toPass = spy(new Employee());
		// we don't care about the contents of the saved employee
		Employee toReturn = new Employee();

		when(employeeRepository.findOne("an id"))
			.thenReturn(Optional.of(new Employee("an id", null, 0)));
		when(employeeRepository.save(toPass))
			.thenReturn(toReturn);

		assertThat(employeeService.replaceEmployeeById("an id", toPass))
			.isSameAs(toReturn);

		// make sure the service sets the passed id in the employee
		// before saving
		verify(toPass).setEmployeeId("an id");
	}

	@Test
	public void testReplaceEmployeeWhenArgumentIdIsNotNull() {
		assertThatThrownBy(
				() -> employeeService.replaceEmployeeById("any id", new Employee("ID", "", 1000)))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Unexpected id specification for Employee");
		verifyNoMoreInteractions(employeeRepository);
	}

	@Test
	public void testReplaceEmployeeWhenArgumentIsNull() {
		assertThatThrownBy(() -> employeeService.replaceEmployeeById("any id", null))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Missing values for Employee");
		verifyNoMoreInteractions(employeeRepository);
	}

	@Test
	public void testReplaceEmployeeWhenEmployeeIsNotFound() {
		when(employeeRepository.findOne(anyString()))
			.thenReturn(Optional.empty());
		assertThatThrownBy(() -> employeeService.replaceEmployeeById("AN ID", new Employee()))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("Employee not found with id AN ID");
		verifyNoMoreInteractions(ignoreStubs(employeeRepository));
	}

	@Test
	public void testDeleteEmployeeByIdJustDelegatesToRepository() {
		Employee employee = new Employee();
		when(employeeRepository.deleteById("an-id"))
			.thenReturn(employee);
		assertThat(employeeService.deleteEmployeeById("an-id"))
			.isSameAs(employee);
	}

}
