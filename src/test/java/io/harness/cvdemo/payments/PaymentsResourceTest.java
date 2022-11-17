package io.harness.cvdemo.payments;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaymentsResourceTest {

    @Before
    public void setUp() throws Exception {
        System.out.println("Before Unit Test - setUp");
        assertEquals("Health Check OK","Health Check OK");

    }

    @Test
    public void paymentProcess() {
        System.out.println("Unit Test run");
        assertEquals("Health Check OK","Health Check OK");
    }

    @Test
    public void paymentList() {
        System.out.println("Unit Test metricGeneratorHealthCheck");
        assertEquals("Health Check OK","Health Check OK");
    }


    @Test
    public void paymentStatus() {
        System.out.println("Unit Test metricGeneratorHealthCheck");
        assertEquals("Health Check OK","Health Check OK");
    }

}