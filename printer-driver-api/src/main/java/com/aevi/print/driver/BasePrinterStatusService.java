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
package com.aevi.print.driver;

import com.aevi.android.rxmessenger.AbstractMessengerService;
import com.aevi.print.model.PrinterStatus;
import com.aevi.print.model.PrinterStatusRequest;

/**
 * This abstract service should be extended to provide a print driver status implementation
 */
public abstract class BasePrinterStatusService extends AbstractMessengerService<PrinterStatusRequest, PrinterStatus> {

    private PrinterStatusStream printerStatusStream;

    protected BasePrinterStatusService() {
        super(PrinterStatusRequest.class);
        printerStatusStream = new PrinterStatusStream(this);
    }

    @Override
    protected void handleRequest(PrinterStatusRequest statusRequest, String packageName) {
        printerStatusStream.subscribeToStatus(statusRequest);
    }
}
