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

import android.util.Log;

import com.aevi.print.driver.BasePrinterDriverService;
import com.aevi.print.driver.common.PrinterDriverBase;
import com.aevi.print.driver.common.PrinterDriverFactory;
import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.aevi.print.model.PrintJob.State.FAILED;
import static com.aevi.print.util.Preconditions.checkNotNull;


/**
 * Extend this abstract service to provide the print driver service implementation
 * Use this class when using the printer driver framework (See {@link com.aevi.print.driver.common.PrinterDriverBase})
 */
public abstract class CommonPrinterDriverService extends BasePrinterDriverService {
    private static final String TAG = CommonPrinterDriverService.class.getSimpleName();
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
     * @param payload the payload to be sent to the pinter
     */
    @Override
    protected void print(final String clientId, PrintPayload payload) {
        checkNotNull(printerDriverFactory, "setPrinterDriverFactory must be set before the print method is called");
        if (payload == null) {
            sendErrorMessageToClient(clientId, PrinterMessages.ERROR_PRINT_FAILED, "print payload cannot be null ");
            return;
        }

        Log.d(TAG, "Got print request: " + clientId);
        String printerId = payload.getPrinterId();

        final BasePrinterInfo printerInfo = getDeviceInfo(printerId);
        if (printerInfo == null) {
            printerDriverFactory.deletePrinterDriver(printerId);
            sendMessageToClient(clientId, new PrintJob(FAILED, PrinterMessages.ERROR_PRINTER_NOT_FOUND, "Unknown printer").toJson());
            return;
        }

        PrinterDriverBase printerDriverBase = printerDriverFactory.getPrinterDriver(printerInfo);

        printerDriverBase.print(payload)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<PrintJob>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull PrintJob printJob) {
                        sendMessageToClient(clientId, printJob.toJson());
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        Log.e(TAG, "Print failed", throwable);
                        sendErrorMessageToClient(clientId, PrinterMessages.ERROR_PRINT_FAILED, "Failed to print: " + throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        sendEndStreamMessageToClient(clientId);
                    }
                });
    }

}
