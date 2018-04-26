package com.aevi.print.driver.common;

import com.aevi.print.model.BasePrinterInfo;

import java.util.HashMap;
import java.util.Map;

public abstract class PrinterDriverFactory {

    private final Map<String, PrinterDriverBase> printerDrivers = new HashMap<>();

    public PrinterDriverBase getPrinterDriver(final BasePrinterInfo printerInfo) {
        String printerId = printerInfo.getPrinterId();
        synchronized (printerDrivers) {
            PrinterDriverBase printerDriver = printerDrivers.get(printerId);
            if (printerDriver == null) {
                printerDriver = createPrinterDriver(printerInfo);
                printerDrivers.put(printerId, printerDriver);
            }
            return printerDriver;
        }
    }

    public void deletePrinterDriver(String printerId) {
        synchronized (printerDrivers) {
            printerDrivers.remove(printerId);
        }
    }

    protected abstract PrinterDriverBase createPrinterDriver(BasePrinterInfo printerInfo);
}
