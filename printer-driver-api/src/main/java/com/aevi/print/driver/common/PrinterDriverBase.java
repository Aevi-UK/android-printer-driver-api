package com.aevi.print.driver.common;

import android.util.Log;

import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.driver.common.devices.CommonPrinterInfo;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

import static com.aevi.print.model.PrintJob.State.IN_PROGRESS;

public abstract class PrinterDriverBase<T> {
    private static final String TAG = PrinterDriverBase.class.getSimpleName();

    private final AtomicBoolean printerInUse = new AtomicBoolean();
    private final AtomicBoolean connectedToPrinter = new AtomicBoolean();
    private final AtomicReference<PrintPayload> printPayloadTask = new AtomicReference<>();
    private final AtomicReference<String> printActionTask = new AtomicReference<>();

    private final CommonPrinterInfo printerInfo;
    private ObservableEmitter<PrintJob> printJobEmitter;

    public PrinterDriverBase(CommonPrinterInfo printerInfo) {

        this.printerInfo = printerInfo;
    }

    public T getPrinterInfo() {
        return (T) printerInfo;
    }

    protected abstract void connectToPrinter();

    protected abstract void disconnectFromPrinter();

    protected abstract void executePrintPayloadTask(PrintPayload printPayload);

    protected abstract void executePrintActionTask(String printActionJob);

    public void onPrinterConnected() {
        Log.d(TAG, "Connected to printer " + printerInfo.getPrinterId());
        connectedToPrinter.set(true);
        executePrinterTasks();
    }

    public void onPrinterDisconnected() {
        Log.d(TAG, "Disconnected from printer " + printerInfo.getPrinterId());
        connectedToPrinter.set(false);
        printerInUse.set(false);

        if (isADriverTaskWaiting()) {
            // This catches the case where a new (action) task comes in as the connection is being closed
            Log.d(TAG, "More tasks are waiting, reconnect to the printer so they can be completed. " + printerInfo.getPrinterId());
            connectToPrinter();
        }
    }

    private boolean isADriverTaskWaiting() {
        return printPayloadTask.get() != null || printActionTask.get() != null;
    }

    private void clearAllDriverTasks() {
        printPayloadTask.set(null);
        printActionTask.set(null);
    }

    private void executePrinterTasks() {

        if (connectedToPrinter.get()) {
            PrintPayload printPayloadJob = this.printPayloadTask.getAndSet(null);
            if (printPayloadJob != null) {
                Log.d(TAG, "starting print payload task:  " + printPayloadJob.getPrinterId());
                executePrintPayloadTask(printPayloadJob);
                return;
            }

            String printActionJob = this.printActionTask.getAndSet(null);
            if (printActionJob != null) {
                Log.d(TAG, "starting print action task: " + printActionJob);
                executePrintActionTask(printActionJob);
                return;
            }

            Log.d(TAG, "disconnecting from printer " + printerInfo.getPrinterId());
            disconnectFromPrinter();
        }
    }

    public Observable<PrintJob> print(final PrintPayload printPayload) {

        return Observable.create(new ObservableOnSubscribe<PrintJob>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<PrintJob> emitter) throws Exception {

                if (!PrinterDriverBase.this.printerInUse.getAndSet(true)) {

                    printJobEmitter = emitter;
                    PrinterDriverBase.this.printPayloadTask.set(printPayload);
                    emitter.onNext(new PrintJob(IN_PROGRESS));
                    Log.d(TAG, "starting connection to printer: " + printerInfo.getPrinterId());
                    connectToPrinter();
                } else {
                    emitter.onNext(new PrintJob(PrintJob.State.FAILED, PrinterMessages.ERROR_BUSY));
                    emitter.onComplete();
                }
            }
        });
    }

    public void sendPrinterAction(String printAction) {

        // This must be set first so that an already running print job can pick up the task
        printActionTask.set(printAction);
        if (!printerInUse.getAndSet(true)) {
            Log.d(TAG, "starting connection to printer");
            connectToPrinter();
        }
    }

    /*
     * Must be called to indicated that a task has been completed successfully
     */
    public void onTaskCompletedSuccessfully() {
        Log.w(TAG, "Printing task completed successfully for pinter : " + printerInfo.getPrinterId());
        if (printJobEmitter != null) {
            printJobEmitter.onNext(new PrintJob(PrintJob.State.PRINTED));
            printJobCompleted();
        }
        executePrinterTasks();
    }


    /*
     * Called one no other task can continue e.g. when the printer is off line.
     * All other task are cancelled, disconnectFromPrinter will not be called.
     * The actual printer driver is responsible for cleaning up before this is called.
     */
    public void onFatalError(String failedReason, String diagnosticMessage) {
        Log.w(TAG, "Fatal printing error : " + failedReason + " - " + diagnosticMessage);
        clearAllDriverTasks();
        if (printJobEmitter != null) {
            printJobEmitter.onNext(new PrintJob(PrintJob.State.FAILED, failedReason, diagnosticMessage));
            printJobCompleted();
        } else {
            emitPrinterStatus(failedReason);
        }
        connectedToPrinter.set(false);
        printerInUse.set(false);
    }

    /*
     * Called when printing failed but other task can continue
     * (e.g if printer is out of paper the open cash drawer still be opened)
     * disconnectFromPrinter will be called automatically when all task have completed
     */
    public void onPrintingFailed(String failedReason, String diagnosticMessage) {
        Log.w(TAG, "Printing failed : " + failedReason + " - " + diagnosticMessage);
        printPayloadTask.set(null);
        if (printJobEmitter != null) {
            printJobEmitter.onNext(new PrintJob(PrintJob.State.FAILED, failedReason, diagnosticMessage));
            printJobCompleted();
        }
        executePrinterTasks();
    }

    /*
     * Called when sending a printer action has failed but other task can continue
     * disconnectFromPrinter will be called automatically when all task have completed
     */
    public void onActionFailed(String failedReason, String diagnosticMessage) {
        Log.d(TAG, "Print action failed : " + failedReason + " - " + diagnosticMessage);
        emitPrinterStatus(failedReason);
        executePrinterTasks();
    }

    private void printJobCompleted() {
        printJobEmitter.onComplete();
        printJobEmitter = null;
    }

    public void emitPrinterStatus(String status) {
        PrinterStatusStream.emitStatus(printerInfo.getPrinterId(), status);
    }
}
