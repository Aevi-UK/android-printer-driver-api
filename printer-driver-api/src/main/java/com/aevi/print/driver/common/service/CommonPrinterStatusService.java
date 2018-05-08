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
package com.aevi.print.driver.common.service;

import com.aevi.print.driver.BasePrinterStatusService;
import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.driver.common.PrinterDriverFactory;
import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrinterMessages;

import static com.aevi.print.util.Preconditions.checkNotNull;


/**
 * Extend this abstract service to provide the print driver status implementation.
 * Use this class when using the printer driver framework (See {@link com.aevi.print.driver.common.PrinterDriverBase})
 */
public abstract class CommonPrinterStatusService extends BasePrinterStatusService {

    private PrinterDriverFactory printerDriverFactory;

    /**
     * The custom PrinterDriverFactory should be set before any other methods in this class are called
     *
     * @param printerDriverFactory the driver implementation of the PrinterDriverFactory
     */
    protected void setPrinterDriverFactory(PrinterDriverFactory printerDriverFactory) {
        checkNotNull(printerDriverFactory, "PrinterDriverFactory must not be null");
        this.printerDriverFactory = printerDriverFactory;
    }

    /**
     * Provides the printer info that will be used by the implementation of {@link com.aevi.print.driver.common.PrinterDriverBase}
     *
     * @param printerId the printer id that uniquely identifies each printer.
     * @return The class providing the details of the printer
     */
    protected abstract BasePrinterInfo getDeviceInfo(String printerId);

    /**
     * The implementation method that prints the payload
     * @param clientId the unique client id
     * @param printerId the printer id that uniquely identifies each printer.
     * @param packageName the calling package name.
     */
    @Override
    protected void handleRequest(String clientId, String printerId, String packageName) {
        checkNotNull(printerDriverFactory, "setPrinterDriverFactory must be set before the handleRequest method is called");

        final BasePrinterInfo printerInfo = getDeviceInfo(printerId);
        if (printerInfo == null) {
            printerDriverFactory.deletePrinterDriver(printerId);
            PrinterStatusStream.emitStatus(printerId, PrinterMessages.ERROR_PRINTER_NOT_FOUND);
            return;
        }

        super.handleRequest(clientId, printerId, packageName);
    }
}
