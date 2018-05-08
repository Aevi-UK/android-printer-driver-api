/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aevi.demoprinterdriver.framework.model;

import com.aevi.print.model.PrinterSettings;

import java.util.HashMap;
import java.util.Map;

public class AvailablePrinters {
    private final static String EXAMPLE_PRINTER_ID = "DemoFrameworkPrinter:01";

    private static AvailablePrinters instance;
    private final Map<String, DemoPrinterInfo> availablePrinters = new HashMap<>();

    public static AvailablePrinters getInstance() {
        if (instance == null) {
            instance = new AvailablePrinters();
        }
        return instance;
    }

    private AvailablePrinters() {
        // Create a fake example printer and add it to the list of available printers
        availablePrinters.put(EXAMPLE_PRINTER_ID, new DemoPrinterInfo(EXAMPLE_PRINTER_ID));
    }

    public DemoPrinterInfo getDeviceInfo(String printerId) {
        return availablePrinters.get(printerId);
    }

    public PrinterSettings[] getPrintersSettings() {
        synchronized (availablePrinters) {
            PrinterSettings[] printerSettingsArray = new PrinterSettings[availablePrinters.size()];
            int count = 0;
            for (DemoPrinterInfo info : availablePrinters.values()) {
                printerSettingsArray[count++] = info.getPrinterSettings();
            }
            return printerSettingsArray;
        }
    }
}
