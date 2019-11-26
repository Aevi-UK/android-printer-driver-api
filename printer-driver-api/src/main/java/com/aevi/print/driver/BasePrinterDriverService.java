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

import com.aevi.android.rxmessenger.ChannelServer;
import com.aevi.android.rxmessenger.service.AbstractChannelService;
import com.aevi.print.model.ChannelPrintingContext;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrintingContext;

import io.reactivex.functions.Consumer;

/**
 * This abstract service should be extended to provide a print driver service implementation
 *
 * @see com.aevi.print.driver.common.service.CommonPrinterDriverService
 */
public abstract class BasePrinterDriverService extends AbstractChannelService {

    @Override
    protected void onNewClient(ChannelServer channelServer, final String callingPackageName) {
        PrintingContext printingContext = new ChannelPrintingContext(channelServer);
        channelServer.subscribeToMessages().subscribe(new Consumer<String>() {
            @Override
            public void accept(String payload) {
                print(printingContext, PrintPayload.fromJson(payload));
            }
        });
    }

    protected abstract void print(PrintingContext printingContext, PrintPayload payload);

    protected void sendResponse(PrintingContext printingContext, PrintJob printJob) {
        printingContext.send(printJob.toJson());
        if (printJob.getPrintJobState() == PrintJob.State.FAILED || printJob.getPrintJobState() == PrintJob.State.PRINTED) {
            printingContext.sendEndStream();
        }
    }
}
