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

import com.aevi.print.driver.BasePrinterActionService;
import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.driver.common.PrinterDriverFactory;
import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrinterMessages;
import com.aevi.print.model.PrintingContext;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static com.aevi.print.util.Preconditions.checkNotNull;

/**
 * Extend this abstract service to provide the print action service implementation.
 * Use this class when using the printer driver framework (See {@link com.aevi.print.driver.common.PrinterDriverBase})
 */
public abstract class CommonPrinterActionService extends BasePrinterActionService {
    private static final String TAG = CommonPrinterActionService.class.getSimpleName();
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
     * The implementation method that responds to the printer action
     *
     * @param printingContext the printing context
     * @param printerId       the unique printer id that identifies the printer
     * @param action          the printer action to perform
     */
    @Override
    protected void action(PrintingContext printingContext, String printerId, final String action) {
        checkNotNull(printerDriverFactory, "setPrinterDriverFactory must be set before the action method is called");
        Log.d(TAG, "Got action request: " + printingContext);

        if (action == null) {
            Log.e(TAG, "Action request cannot be null");
            PrinterStatusStream.emitStatus(printerId, PrinterMessages.UNRECOVERABLE_ERROR);
            return;
        }

        final BasePrinterInfo printerInfo = getDeviceInfo(printerId);
        if (printerInfo == null) {
            Log.e(TAG, "Unknown printer " + printerId);
            printerDriverFactory.deletePrinterDriver(printerId);
            PrinterStatusStream.emitStatus(printerId, PrinterMessages.ERROR_PRINTER_NOT_FOUND);
            return;
        }

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                printerDriverFactory.getPrinterDriver(printerInfo).sendPrinterAction(action);
            }
        }).subscribeOn(Schedulers.newThread()).subscribe();
    }
}
