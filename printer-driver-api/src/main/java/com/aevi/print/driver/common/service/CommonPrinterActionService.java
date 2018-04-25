package com.aevi.print.driver.common.service;

import android.util.Log;

import com.aevi.print.driver.BasePrinterActionService;
import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.driver.common.PrinterDriverFactory;
import com.aevi.print.driver.common.devices.CommonPrinterInfo;
import com.aevi.print.model.PrinterMessages;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import static com.aevi.print.util.Preconditions.checkNotNull;

public abstract class CommonPrinterActionService extends BasePrinterActionService {
    private static final String TAG = CommonPrinterActionService.class.getSimpleName();
    private PrinterDriverFactory printerDriverFactory;

    protected void setPrinterDriverFactory(PrinterDriverFactory printerDriverFactory) {
        checkNotNull(printerDriverFactory, "PrinterDriverFactory must not be null");
        this.printerDriverFactory = printerDriverFactory;
    }

    @Override
    protected void action(String clientId, String printerId, final String action) {
        checkNotNull(printerDriverFactory, "setPrinterDriverFactory must be set before the action method is called");
        Log.d(TAG, "Got action request: " + clientId);

        if (action == null) {
            Log.e(TAG, "Action request cannot be null");
            PrinterStatusStream.emitStatus(printerId, PrinterMessages.UNRECOVERABLE_ERROR);
            return;
        }

        final CommonPrinterInfo printerInfo = getDeviceInfo(printerId);
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

    protected abstract CommonPrinterInfo getDeviceInfo(String printerId);
}
