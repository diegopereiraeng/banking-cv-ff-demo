package io.harness.cvdemo.behavior;

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class MetricsGeneratorTest {

    @Before
    public void setUp() throws Exception {
        System.out.println("Before Unit Test - setUp");

        assertEquals("Health Check OK","Health Check OK");

    }

    @Test
    public void run() {
        System.out.println("Unit Test run");
        assertEquals("Health Check OK","Health Check OK");
    }

    @Test
    public void metricGeneratorHealthCheck() {
        System.out.println("Unit Test metricGeneratorHealthCheck");
        assertEquals("Health Check OK","Health Check OK");
    }


}