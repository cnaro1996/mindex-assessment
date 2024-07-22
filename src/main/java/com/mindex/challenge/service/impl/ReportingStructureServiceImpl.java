package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure get(String employeeId) {
        LOG.debug("Getting reporting structure for employee with id [{}]", employeeId);

        ReportingStructure reportingStructure = new ReportingStructure();

        Employee employee = employeeRepository.findByEmployeeId(employeeId);

        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(calculateNumberOfReports(employee));

        return reportingStructure;
    }

    /**
     * Recursively calculates the total number of reports under a given employee. The total number of reports is
     * determined by aggregating the number of directReports for an employee and all of their distinct reports.
     */
    private int calculateNumberOfReports(Employee employee) {
        if(employee.getDirectReports().isEmpty() || employee.getDirectReports() == null) {
            return 0;
        }

        int reports = employee.getDirectReports().size();
        for(Employee e : employee.getDirectReports()) {
            reports += calculateNumberOfReports(e);
        }

        return reports;
    }
}
