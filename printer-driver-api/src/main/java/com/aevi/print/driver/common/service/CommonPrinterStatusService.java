package com.aevi.print.driver.common.service;

import com.aevi.print.driver.BasePrinterStatusService;
import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.driver.common.PrinterDriverFactory;
import com.aevi.print.driver.common.devices.CommonPrinterInfo;
import com.aevi.print.model.PrinterMessages;

import static com.aevi.print.util.Preconditions.checkNotNull;

public abstract class CommonPrinterStatusService extends BasePrinterStatusService {

    private PrinterDriverFactory printerDriverFactory;

    protected void setPrinterDriverFactory(PrinterDriverFactory printerDriverFactory) {
        checkNotNull(printerDriverFactory, "PrinterDriverFactory must not be null");
        this.printerDriverFactory = printerDriverFactory;
    }

    protected abstract CommonPrinterInfo getDeviceInfo(String printerId);

    @Override
    protected void handleRequest(String clientId, String printerId, String packageName) {
        checkNotNull(printerDriverFactory, "setPrinterDriverFactory must be set before the handleRequest method is called");

        final CommonPrinterInfo printerInfo = getDeviceInfo(printerId);
        if (printerInfo == null) {
            printerDriverFactory.deletePrinterDriver(printerId);
            PrinterStatusStream.emitStatus(printerId, PrinterMessages.ERROR_PRINTER_NOT_FOUND);
            return;
        }

        super.handleRequest(clientId, printerId, packageName);
    }
}
