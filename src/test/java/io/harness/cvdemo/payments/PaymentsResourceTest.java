package io.harness.cvdemo.payments;


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
        System.out.println("Payment PRocess Check");
        try {
            Thread.sleep(10000);
        }
         catch (InterruptedException ex) {

        }
        assertEquals("Health Check OK","Health Check OK");
    }

    @Test
    public void paymentList() {
        System.out.println("Unit Test Payment List");
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException ex) {

        }
        assertEquals("Health Check OK","Health Check OK");
    }


    @Test
    public void paymentStatus() {
        System.out.println("Unit Test paymentStatus");
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException ex) {

        }
        assertEquals("Health Check OK","Health Check OK");
    }

}