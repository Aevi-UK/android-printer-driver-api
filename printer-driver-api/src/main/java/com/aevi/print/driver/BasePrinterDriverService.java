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
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;

/**
 * This abstract service should be extended to provide a print driver service implementation
 */
public abstract class BasePrinterDriverService extends AbstractMessengerService<PrintPayload, PrintJob> {

    protected BasePrinterDriverService() {
        super(PrintPayload.class);
    }

    @Override
    protected void handleRequest(PrintPayload payload, String packageName) {
        print(payload);
    }

    protected abstract void print(PrintPayload payload);

    protected void sendResponse(PrintPayload payload, PrintJob printJob) {
        sendMessageToClient(payload.getId(), printJob);
        if (printJob.getPrintJobState() == PrintJob.State.FAILED || printJob.getPrintJobState() == PrintJob.State.PRINTED) {
            sendEndStreamMessageToClient(payload.getId());
        }
    }
}
