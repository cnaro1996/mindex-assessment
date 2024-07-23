package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingStructure(String employeeId) {
        LOG.debug("Getting reporting structure for employee with id [{}]", employeeId);

        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        if (employee == null) {
            throw new RuntimeException("No employee found with employeeId: " + employeeId);
        }

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(calculateNumberOfReports(employee));

        return reportingStructure;
    }

    /**
     * Recursively calculates the total number of reports under a given employee. The total number of reports is
     * determined by aggregating the number of directReports for an employee and all of their distinct reports.
     */
    private int calculateNumberOfReports(Employee employee) {
        if(employee.getDirectReports() == null || employee.getDirectReports().isEmpty()) {
            return 0;
        }

        int reports = employee.getDirectReports().size();
        for(Employee directReport : employee.getDirectReports()) {
            // The directReports entries are Employee objects with unpopulated directReports fields.
            Employee populatedDirectReport = employeeRepository.findByEmployeeId(directReport.getEmployeeId());
            reports += calculateNumberOfReports(populatedDirectReport);
        }

        return reports;
    }
}
