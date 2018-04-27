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
package com.aevi.print.driver.common;

import com.aevi.print.model.BasePrinterInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * This class ensures that a single new instance of the printer driver is created for each printer detected.
 * Developers should extend this class and then implement the {@link #createPrinterDriver } method.
 *
 * @see com.aevi.print.driver.common.service.CommonPrinterActionService
 * @see com.aevi.print.driver.common.service.CommonPrinterDriverService
 * @see com.aevi.print.driver.common.service.CommonPrinterStatusService
 */
public abstract class PrinterDriverFactory {

    private final Map<String, PrinterDriverBase> printerDrivers = new HashMap<>();

    /**
     * Returns either an existing instance  or creates a new of instance the printer driver.
     *
     * @param printerInfo the class providing the details of the printer
     * @return the instance of the printer driver
     */
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

    /**
     * Deletes the printer driver
     * @param printerId The id of the printer driver to delete
     */
    public void deletePrinterDriver(String printerId) {
        synchronized (printerDrivers) {
            printerDrivers.remove(printerId);
        }
    }

    /**
     * The implementation class must create a new instance of the printer driver
     *
     * @param printerInfo The class providing the details of the printer
     * @return the new instance of the printer driver to be used
     */
    protected abstract PrinterDriverBase createPrinterDriver(BasePrinterInfo printerInfo);
}
