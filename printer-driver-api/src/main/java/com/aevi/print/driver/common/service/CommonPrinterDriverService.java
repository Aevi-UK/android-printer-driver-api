package com.aevi.print.driver.common.service;

import android.util.Log;

import com.aevi.print.driver.BasePrinterDriverService;
import com.aevi.print.driver.common.PrinterDriverBase;
import com.aevi.print.driver.common.PrinterDriverFactory;
import com.aevi.print.driver.common.devices.CommonPrinterInfo;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.aevi.print.model.PrintJob.State.FAILED;
import static com.aevi.print.util.Preconditions.checkNotNull;

public abstract class CommonPrinterDriverService extends BasePrinterDriverService {
    private static final String TAG = CommonPrinterDriverService.class.getSimpleName();
    private PrinterDriverFactory printerDriverFactory;

    protected void setPrinterDriverFactory(PrinterDriverFactory printerDriverFactory) {
        checkNotNull(printerDriverFactory, "PrinterDriverFactory must not be null");
        this.printerDriverFactory = printerDriverFactory;
    }

    @Override
    protected void print(final String clientId, PrintPayload payload) {
        checkNotNull(printerDriverFactory, "setPrinterDriverFactory must be set before the print method is called");
        if (payload == null) {
            sendErrorMessageToClient(clientId, PrinterMessages.ERROR_PRINT_FAILED, "print payload cannot be null ");
            return;
        }

        Log.d(TAG, "Got print request: " + clientId);
        String printerId = payload.getPrinterId();

        final CommonPrinterInfo printerInfo = getDeviceInfo(printerId);
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

    protected abstract CommonPrinterInfo getDeviceInfo(String printerId);
}
