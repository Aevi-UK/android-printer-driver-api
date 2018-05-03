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
package com.aevi.print.driver.common;

import android.support.annotation.NonNull;
import android.util.Log;

import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.aevi.print.model.PrintJob.State.IN_PROGRESS;

public abstract class PrinterDriverBase<T extends BasePrinterInfo> {
    private static final String TAG = PrinterDriverBase.class.getSimpleName();

    private final AtomicBoolean printerInUse = new AtomicBoolean();
    private final AtomicBoolean connectedToPrinter = new AtomicBoolean();
    private final AtomicReference<PrintJobTask> printJobTask = new AtomicReference<>();
    private final AtomicReference<String> printActionTask = new AtomicReference<>();

    private final BasePrinterInfo printerInfo;

    public PrinterDriverBase(BasePrinterInfo printerInfo) {

        this.printerInfo = printerInfo;
    }

    public T getPrinterInfo() {
        return (T) printerInfo;
    }

    protected abstract void connectToPrinter();

    protected abstract void disconnectFromPrinter();

    protected abstract void executePrintPayloadTask(@NonNull PrintPayload printPayload);

    protected abstract void executePrintActionTask(@NonNull String printActionJob);

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
            Log.d(TAG, "More tasks are waiting, reconnecting to the printer: " + printerInfo.getPrinterId());
            connectToPrinterIfRequired();
        }
    }

    private boolean isADriverTaskWaiting() {
        return printJobTask.get() != null || printActionTask.get() != null;
    }

    private void clearAllDriverTasks() {
        printJobTask.set(null);
        printActionTask.set(null);
    }

    private void executePrinterTasks() {

        if (connectedToPrinter.get()) {
            PrintPayload printPayload = getAndClearPrintPayload();
            if (printPayload != null) {
                Log.d(TAG, "starting print payload task:  " + printPayload.getPrinterId());
                executePrintPayloadTask(printPayload);
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
        } else {
            Log.w(TAG, "Ignoring tasks when not connected to printer" + printerInfo.getPrinterId());
        }
    }

    private PrintPayload getAndClearPrintPayload() {
        PrintJobTask printJobTask = this.printJobTask.get();
        if (printJobTask == null) {
            return null;
        } else {
            return printJobTask.getAndClearPrintPayload();
        }
    }

    private boolean hasPrintJobTaskCompleted() {
        PrintJobTask printJobTask = this.printJobTask.get();
        if (printJobTask == null) {
            return true;
        } else {
            return printJobTask.hasPrintJobTaskCompleted();
        }
    }

    public Observable<PrintJob> print(@NonNull final PrintPayload printPayload) {
        Log.d(TAG, "Received print request from: " + printerInfo.getPrinterId());

        return Observable.create(new ObservableOnSubscribe<PrintJob>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<PrintJob> emitter) throws Exception {
                PrintJobTask printJobTask = new PrintJobTask(emitter, printPayload);

                if (PrinterDriverBase.this.printJobTask.compareAndSet(null, printJobTask)) {
                    emitter.onNext(new PrintJob(IN_PROGRESS));
                    connectToPrinterIfRequired();
                } else {
                    emitter.onNext(new PrintJob(PrintJob.State.FAILED, PrinterMessages.ERROR_BUSY));
                    emitter.onComplete();
                }
            }
        });
    }

    public void sendPrinterAction(@NonNull String printAction) {
        Log.d(TAG, "Received action request from: " + printerInfo.getPrinterId());

        // This must be set first so that an already running print job can pick up the task
        printActionTask.set(printAction);
        connectToPrinterIfRequired();
    }

    private void connectToPrinterIfRequired() {
        if (printerInUse.compareAndSet(false, true)) {
            Log.d(TAG, "Starting connection to printer " + printerInfo.getPrinterId());
            connectToPrinter();
        }
    }

    /*
     * Must be called to indicated that a task has been completed successfully
     */
    public void onTaskCompletedSuccessfully() {
        Log.d(TAG, "Printing task completed successfully for printer : " + printerInfo.getPrinterId());
        if (hasPrintJobTaskCompleted()) {
            completePrintJob(new PrintJob(PrintJob.State.PRINTED));
        }
        executePrinterTasks();
    }

    /*
     * Called one no other task can continue e.g. when the printer is off line.
     * All other task are cancelled, disconnectFromPrinter will not be called.
     * The actual printer driver is responsible for cleaning up before this is called.
     */
    public void onFatalError(@NonNull String failedReason, String diagnosticMessage) {
        Log.w(TAG, "Fatal printing error : " + failedReason + " - " + diagnosticMessage);
        if (!completePrintJob(new PrintJob(PrintJob.State.FAILED, failedReason, diagnosticMessage))) {
            emitPrinterStatus(failedReason);
        }
        clearAllDriverTasks();
        connectedToPrinter.set(false);
        printerInUse.set(false);
    }

    /*
     * Called when printing failed but other task can continue
     * (e.g if printer is out of paper the open cash drawer still be opened)
     * disconnectFromPrinter will be called automatically when all task have completed
     */
    public void onPrintingFailed(@NonNull String failedReason, String diagnosticMessage) {
        Log.w(TAG, "Printing failed : " + failedReason + " - " + diagnosticMessage);
        completePrintJob(new PrintJob(PrintJob.State.FAILED, failedReason, diagnosticMessage));
        executePrinterTasks();
    }

    /*
     * Called when sending a printer action has failed but other task can continue
     * disconnectFromPrinter will be called automatically when all task have completed
     */
    public void onActionFailed(@NonNull String failedReason, String diagnosticMessage) {
        Log.d(TAG, "Print action failed : " + failedReason + " - " + diagnosticMessage);
        emitPrinterStatus(failedReason);
        executePrinterTasks();
    }

    private boolean completePrintJob(PrintJob printJob) {
        PrintJobTask printJobTask = this.printJobTask.getAndSet(null);
        if (printJobTask != null) {
            ObservableEmitter<PrintJob> emitter = printJobTask.getPrintJobEmitter();
            emitter.onNext(printJob);
            emitter.onComplete();
            return true;
        } else {
            return false;
        }
    }

    public void emitPrinterStatus(@NonNull String status) {
        PrinterStatusStream.emitStatus(printerInfo.getPrinterId(), status);
    }

    private class PrintJobTask {
        @NonNull
        private final ObservableEmitter<PrintJob> printJobEmitter;

        private final AtomicReference<PrintPayload> printPayload = new AtomicReference<>();

        PrintJobTask(@NonNull ObservableEmitter<PrintJob> printJobEmitter, @NonNull PrintPayload printPayload) {
            this.printJobEmitter = printJobEmitter;
            this.printPayload.set(printPayload);
        }

        @NonNull
        public ObservableEmitter<PrintJob> getPrintJobEmitter() {
            return printJobEmitter;
        }

        public PrintPayload getAndClearPrintPayload() {
            return printPayload.getAndSet(null);
        }

        public boolean hasPrintJobTaskCompleted() {
            return printPayload.get() == null;
        }
    }
}
