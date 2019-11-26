package com.aevi.print.model;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class BasePrinterInfoTest {

    @Test
    public void testMatchingAddressAndPrinter() {
        TestingBasePrinterInfo printer1 = new TestingBasePrinterInfo("ID-A");
        TestingBasePrinterInfo printer2 = new TestingBasePrinterInfo("ID-A");
        assertTrue(printer1.sameAddressAndPrinter(printer2));
        assertTrue(printer2.sameAddressAndPrinter(printer1));
        assertTrue(printer1.sameAddressAndPrinter(printer1));
    }

    @Test
    public void testDifferentId() {
        TestingBasePrinterInfo printer1 = new TestingBasePrinterInfo("ID-A");
        TestingBasePrinterInfo printer2 = new TestingBasePrinterInfo("ID-B");
        assertFalse(printer1.sameAddressAndPrinter(printer2));
        assertFalse(printer2.sameAddressAndPrinter(printer1));
    }

    @Test
    public void testWithNullObject() {
        TestingBasePrinterInfo printer1 = new TestingBasePrinterInfo("ID-A");
        assertFalse(printer1.sameAddressAndPrinter(null));
    }

    @Test
    public void testWithBasePrinterInfoObject() {
        TestingBasePrinterInfo printer1 = new TestingBasePrinterInfo("ID-A");
        BasePrinterInfo anotherPrinter = new BasePrinterInfo() {

            @Override
            public String getPrinterId() {
                return "ID-A";
            }

            @Override
            public boolean sameAddressAndPrinter(BasePrinterInfo anotherPrinter) {
                return true;
            }
        };
        assertTrue(printer1.sameAddressAndPrinter(anotherPrinter));
    }

    @Test
    public void testWithNullId() {
        TestingBasePrinterInfo printer1 = new TestingBasePrinterInfo("ID-A");
        TestingBasePrinterInfo printer2 = new TestingBasePrinterInfo(null);
        assertFalse(printer1.sameAddressAndPrinter(printer2));
        assertFalse(printer2.sameAddressAndPrinter(printer1));
    }

    @Test
    public void testWithNullIds() {
        TestingBasePrinterInfo printer1 = new TestingBasePrinterInfo(null);
        TestingBasePrinterInfo printer2 = new TestingBasePrinterInfo(null);
        assertFalse(printer1.sameAddressAndPrinter(printer2));
        assertFalse(printer2.sameAddressAndPrinter(printer1));
    }

    class TestingBasePrinterInfo implements BasePrinterInfo {

        private final String printerId;

        public TestingBasePrinterInfo(String printerId) {

            this.printerId = printerId;
        }

        @Override
        public String getPrinterId() {
            return printerId;
        }
    }

}
