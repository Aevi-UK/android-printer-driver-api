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
package com.aevi.demoprinterdriver.framework.driver;

import android.support.annotation.NonNull;

import com.aevi.demoprinterdriver.framework.model.DemoPrinterInfo;
import com.aevi.print.driver.common.PrinterDriverBase;
import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;

class DemoPrinterDriver extends PrinterDriverBase<DemoPrinterInfo> {
    public DemoPrinterDriver(BasePrinterInfo printerInfo) {
        super(printerInfo);
    }

    @Override
    protected void connectToPrinter() {

        // Add code to connect to the printer

        // If there is an error call this method
        // onDriverError(PrinterMessages.PRINTER_OFFLINE, "diagnostic message goe here");

        // only call this method if the connection is successful
        onPrinterConnected();
    }

    @Override
    protected void disconnectFromPrinter() {

        // Add code to disconnect from the printer

        // If there is an error call this method
        // onDriverError(PrinterMessages.PRINTER_OFFLINE, "diagnostic message goe here");

        // only if the disconnection is successful then call this method
        onPrinterDisconnected();
    }

    @Override
    protected void executePrintPayloadTask(@NonNull PrintPayload printPayload) {

        // Add code to print the printPayload

        // Example Status message
        emitPrinterStatus(PrinterMessages.PRINTER_READY);

        // If there is an error call onActionFailed or onPrintingFailed otherwise use the method below
        onTaskCompletedSuccessfully();
    }

    @Override
    protected void executePrintActionTask(@NonNull String printActionJob) {

        if (printActionJob.equals(PrinterMessages.ACTION_OPEN_CASH_DRAWER)) {

            // Add code to open the cash drawer via the printer

            // Example Status message
            emitPrinterStatus(PrinterMessages.DRAWER_OPENED);

            // If there is an error call onActionFailed or onActionFailed otherwise use the method below
            onTaskCompletedSuccessfully();
        } else {
            onActionFailed(PrinterMessages.ERROR_SERVICE_NOT_AVAILABLE, "Invalid print action : " + printActionJob);
        }
    }
}
