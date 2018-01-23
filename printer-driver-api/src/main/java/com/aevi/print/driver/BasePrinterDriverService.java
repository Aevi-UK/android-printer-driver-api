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

import com.aevi.android.rxmessenger.service.AbstractMessengerService;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;

/**
 * This abstract service should be extended to provide a print driver service implementation
 */
public abstract class BasePrinterDriverService extends AbstractMessengerService {

    @Override
    protected void handleRequest(String clientId, String payload, String packageName) {
        print(clientId, PrintPayload.fromJson(payload));
    }

    protected abstract void print(String clientId, PrintPayload payload);

    protected void sendResponse(String clientId, PrintJob printJob) {
        sendMessageToClient(clientId, printJob.toJson());
        if (printJob.getPrintJobState() == PrintJob.State.FAILED || printJob.getPrintJobState() == PrintJob.State.PRINTED) {
            sendEndStreamMessageToClient(clientId);
        }
    }
}
