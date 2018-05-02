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

import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrintPayload;

public class PrinterDriverImplementation extends PrinterDriverBase<FakePrinterInfo> {

    private static final String DIAGNOSTIC_MESSAGE = "DIAGNOSTIC_MESSAGE";
    public int connectToPrinterCounter;
    public int executePrintActionTaskCounter;
    public int executePrintPayloadTaskCounter;
    public int disconnectFromPrinterCounter;
    public PrintPayload executePrintPayload;
    public String executePrintAction;
    private boolean automaticOnPrinterConnected;
    private boolean automaticOnTaskCompleted;
    private String automaticOnActionFailed;
    private String automaticOnPrintingFailed;
    private String automaticOnFatalError;

    public PrinterDriverImplementation(BasePrinterInfo printerInfo) {
        super(printerInfo);
    }

    @Override
    protected void connectToPrinter() {
        connectToPrinterCounter++;
        if (automaticOnPrinterConnected) {
            onPrinterConnected();
        } else if (automaticOnFatalError != null) {
            onFatalError(automaticOnFatalError, DIAGNOSTIC_MESSAGE);
        }
    }

    @Override
    protected void disconnectFromPrinter() {
        disconnectFromPrinterCounter++;
        if (automaticOnPrinterConnected) {
            onPrinterDisconnected();
        }
    }

    @Override
    protected void executePrintPayloadTask(@NonNull PrintPayload printPayload) {
        executePrintPayloadTaskCounter++;
        executePrintPayload = printPayload;
        if (automaticOnTaskCompleted) {
            onTaskCompletedSuccessfully();
        } else if (automaticOnPrintingFailed != null) {
            onPrintingFailed(automaticOnPrintingFailed, DIAGNOSTIC_MESSAGE);
        }
    }

    @Override
    protected void executePrintActionTask(@NonNull String printAction) {
        executePrintActionTaskCounter++;
        executePrintAction = printAction;
        if (automaticOnTaskCompleted) {
            onTaskCompletedSuccessfully();
        } else if (automaticOnActionFailed != null) {
            onActionFailed(automaticOnActionFailed, DIAGNOSTIC_MESSAGE);
        }
    }

    public void setAutomaticOnPrinterConnected() {
        automaticOnPrinterConnected = true;
    }
    public void setAutomaticOnTaskCompleted() {
        automaticOnTaskCompleted = true;
    }

    public void setAutomaticOnFatalError(String failedReason) {
        automaticOnFatalError = failedReason;
    }

    public void setAutomaticOnPrintingFailed(String failedReason) {
        automaticOnPrintingFailed = failedReason;
    }

    public void setAutomaticOnActionFailed(String failedReason) {
        automaticOnActionFailed = failedReason;
    }
}
