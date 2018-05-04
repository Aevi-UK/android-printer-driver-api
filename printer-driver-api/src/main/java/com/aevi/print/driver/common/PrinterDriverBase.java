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

/**
 * This class provides a common framework to simplify the implementation of a printer printer driver.
 * It works with a manufactures POS receipt printer SDK that either uses blocking API calls
 * or non-blocking API and callbacks.
 * All methods called in this class are thread safe and so can be called from different threads.
 *
 * @param <T> The class that has been derived from {@link BasePrinterInfo}
 */
public abstract class PrinterDriverBase<T extends BasePrinterInfo> {
    private static final String TAG = PrinterDriverBase.class.getSimpleName();

    private final AtomicBoolean printerInUse = new AtomicBoolean();
    private final AtomicBoolean connectedToPrinter = new AtomicBoolean();
    private final AtomicReference<PrintJobTask> printJobTask = new AtomicReference<>();
    private final AtomicReference<String> printActionTask = new AtomicReference<>();

    private final BasePrinterInfo printerInfo;

    /**
     * The constructor for PrinterDriverBase
     *
     * @param printerInfo The class providing the details of the printer
     */
    public PrinterDriverBase(BasePrinterInfo printerInfo) {

        this.printerInfo = printerInfo;
    }

    /**
     * Get the information on the printer being used
     *
     * @return The class providing the details of the printer
     */
    public T getPrinterInfo() {
        return (T) printerInfo;
    }

    /**
     * The implementation of this method should open a connection to the printer.
     * When the connection has completed successfully then the method {@link #onPrinterConnected} should be called.
     * If there is an error during the connection then {@link #onFatalError} should be called instead.
     */
    protected abstract void connectToPrinter();

    /**
     * The implementation of this method should close the connection to the printer
     * and call {@link #onPrinterDisconnected} on completion or alternatively call {@link #onFatalError} if there has been an error.
     */
    protected abstract void disconnectFromPrinter();

    /**
     * This method is called after the printer connection has been made and when there is data ready to be printed.
     * The implementation of this method should convert the printer payload to a form the printer will accept and then send it the printer.
     * When the printing has been completed successfully call  {@link #onTaskCompletedSuccessfully}.
     * Otherwise if there is an error call {@link #onPrintingFailed} or {@link #onFatalError} instead.
     *
     * @param printPayload the printer payload that is be printed.
     */
    protected abstract void executePrintPayloadTask(@NonNull PrintPayload printPayload);

    /**
     * This method is called is called after the printer connection has been made and when there is
     * a printer action to be carried by the printer.
     *
     * The method {@link #onTaskCompletedSuccessfully} should be called on completion or
     * call {@link #onFatalError} or {@link #onActionFailed} when there has been an error.
     *
     * @param printAction The print action command to be carried out by the printer (e.g. open the cash drawer)
     */
    protected abstract void executePrintActionTask(@NonNull String printAction);

    /**
     * This method must be called after making a successful connection to the printer and the printer is ready for the next task.
     */
    public void onPrinterConnected() {
        Log.d(TAG, "Connected to printer " + printerInfo.getPrinterId());
        connectedToPrinter.set(true);
        executePrinterTasks();
    }

    /**
     * This method must be called after the connection to the printer has been closed.
     * Calling {@link #onFatalError} instead will also mark the connection as closed.
     */
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

    /**
     * It is intended that this method is only called from the {@link  com.aevi.print.driver.common.service.CommonPrinterDriverService} class.
     * and so it should not be necessary to call this method directly.
     *
     * @param printPayload The payload to print
     * @return An observable stream of PrintJob data which indicates the status of the printout
     */
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

    /**
     * It is intended that this method is only called from the {@link  com.aevi.print.driver.common.service.CommonPrinterActionService} class.
     * and so it should not be necessary to call this method directly.
     *
     * @param printAction The printer action to perform
     */
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

    /**
     * This method must be called when a task (see {@link #executePrintPayloadTask} or {@link #executePrintActionTask}) has been completed successfully.
     * If there is error then call {@link #onFatalError} {@link #onPrintingFailed} or {@link #onActionFailed} instead.
     */
    public void onTaskCompletedSuccessfully() {
        Log.d(TAG, "Printing task completed successfully for printer : " + printerInfo.getPrinterId());
        if (hasPrintJobTaskCompleted()) {
            completePrintJob(new PrintJob(PrintJob.State.PRINTED));
        }
        executePrinterTasks();
    }

    /**
     * Called when there has been an error and no other task can continue e.g. when the printer is offline.
     * All other tasks are then are cancelled and {@link #disconnectFromPrinter} will not be called automatically.
     * It is the responsibility of the implementation to ensure everything is closed down <b>before</b> this method is called.
     *
     * @param failedReason      The reason giving the cause of any failure
     * @param diagnosticMessage A diagnostic message to include with the failedReason
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

    /**
     * Called when printing failed but other task can continue
     * (e.g. if printer is out of paper then the cash drawer can still be opened).
     * {@link #disconnectFromPrinter} will then be called automatically when all other tasks have completed.
     *
     * @param failedReason      The reason giving the cause of any failure
     * @param diagnosticMessage A diagnostic message to include with the failedReason
     */
    public void onPrintingFailed(@NonNull String failedReason, String diagnosticMessage) {
        Log.w(TAG, "Printing failed : " + failedReason + " - " + diagnosticMessage);
        completePrintJob(new PrintJob(PrintJob.State.FAILED, failedReason, diagnosticMessage));
        executePrinterTasks();
    }

    /**
     * Called when sending a printer action has failed but other task can continue
     * {@link #disconnectFromPrinter} will then be called automatically when all task have completed.
     *
     * @param failedReason      The reason giving the cause of any failure
     * @param diagnosticMessage A diagnostic message to include with the failedReason
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

    /**
     * Sends a printer status message to any registered observers.
     *
     * @param status the printer status message
     */
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
