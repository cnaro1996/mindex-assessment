package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;
    private String compensationGetUrl;
    private String compensationCreateUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        String localHostPrefix = "http://localhost:";

        employeeUrl = localHostPrefix + port + "/employee";
        employeeIdUrl = localHostPrefix + port + "/employee/{id}";

        reportingStructureUrl = localHostPrefix + port + "/employee/{id}/reporting-structure";

        compensationGetUrl =  localHostPrefix + port + "/employee/{id}/compensation";
        compensationCreateUrl = localHostPrefix + port + "/employee/compensation";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }

    /**
     * If I had more time, I would extract the setup code in these tests into a builder class so that the test data
     * portion could be abstracted away from the functionality portion of the tests.
     */
    @Test
    public void testGetReportingStructure() {
        Employee testEmployee = new Employee();
        testEmployee.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Lennon");
        testEmployee.setPosition("Development Manager");
        testEmployee.setDepartment("Engineering");

        Employee testDirectReport1 = new Employee();
        testDirectReport1.setEmployeeId("b7839309-3348-463b-a7e3-5de1c168beb3");
        Employee testDirectReport2 = new Employee();
        testDirectReport2.setEmployeeId("03aa1462-ffa9-4978-901b-7c001562cf6f");
        testEmployee.setDirectReports(Arrays.asList(testDirectReport1, testDirectReport2));

        ReportingStructure testReportingStructure = new ReportingStructure();
        testReportingStructure.setEmployee(testEmployee);
        testReportingStructure.setNumberOfReports(4);

        ReportingStructure fetchedReportingStructure = restTemplate.getForEntity(reportingStructureUrl,
                ReportingStructure.class, testEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(testEmployee, fetchedReportingStructure.getEmployee());
        assertEquals(testEmployee.getEmployeeId(), fetchedReportingStructure.getEmployee().getEmployeeId());
        assertEquals(4, fetchedReportingStructure.getNumberOfReports());
    }

    /**
     * Normally, I would separate this into two tests, testCreateCompensation and testGetCompensation. Due to there
     * being no bootstrapping of Compensation data into the persistence layer, and in the interest of time, I've
     * combined both of them into this single test instead to reuse data, similar to the testCreateReadUpdate test.
     */
    @Test
    public void testCreateGetCompensation() {
        Employee testEmployee = new Employee();
        testEmployee.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Lennon");
        testEmployee.setPosition("Development Manager");
        testEmployee.setDepartment("Engineering");

        Employee testDirectReport1 = new Employee();
        testDirectReport1.setEmployeeId("b7839309-3348-463b-a7e3-5de1c168beb3");
        Employee testDirectReport2 = new Employee();
        testDirectReport2.setEmployeeId("03aa1462-ffa9-4978-901b-7c001562cf6f");
        testEmployee.setDirectReports(Arrays.asList(testDirectReport1, testDirectReport2));

        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(testEmployee);
        testCompensation.setSalary(90000);
        testCompensation.setEffectiveDate(LocalDate.of(2024, 7, 23));

        // Create checks
        Compensation createdTestCompensation = restTemplate.postForEntity(compensationCreateUrl,
                testCompensation, Compensation.class).getBody();

        assertNotNull(createdTestCompensation);
        assertCompensationEquivalence(testCompensation, createdTestCompensation);

        // Get checks
        Compensation fetchedTestCompensation = restTemplate.getForEntity(compensationGetUrl,
                Compensation.class, testEmployee.getEmployeeId()).getBody();

        assertCompensationEquivalence(createdTestCompensation, fetchedTestCompensation);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
