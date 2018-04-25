package com.aevi.print.driver.common.devices;

import com.aevi.print.model.PrinterSettings;

public interface CommonPrinterInfo {
    PrinterSettings getPrinterSettings();
    String getDeviceName();
    String getDeviceType();
    String getDisplayName();
    String getPrinterId();
}
