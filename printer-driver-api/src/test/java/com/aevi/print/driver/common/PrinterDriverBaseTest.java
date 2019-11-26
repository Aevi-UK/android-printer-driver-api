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

import com.aevi.print.driver.BasePrinterStatusService;
import com.aevi.print.driver.PrinterStatusStream;
import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;
import com.aevi.print.model.PrinterStatus;
import com.aevi.print.model.PrintingContext;
import com.aevi.print.model.TextRow;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PrinterDriverBaseTest {

    @Mock
    BasePrinterInfo printerInfo;

    PrinterDriverImplementation printerDriverImpl;

    @Mock
    PrintingContext mockPrintingContext;

    @Mock
    private BasePrinterStatusService mockPrinterStatusService;
    private MyPrinterStatusStream printerStatusStream;

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        initMocks(this);

        when(printerInfo.getPrinterId()).thenReturn("ID-1");
        printerStatusStream = new MyPrinterStatusStream();
        printerStatusStream.subscribeToStatus(mockPrintingContext, "ID-1");

        printerDriverImpl = new PrinterDriverImplementation(printerInfo);
    }

    @Test
    public void printCausesAConnection() {
        printerDriverImpl.print(new PrintPayload("ID-1")).subscribe();
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(0);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
    }

    @Test
    public void sendPrinterActionCausesAConnection() {

        printerDriverImpl.sendPrinterAction("PRINT_ACTION");
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(0);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
    }

    @Test
    public void printCausesAConnectionAndThenPrinting() {

        printerDriverImpl.setAutomaticOnPrinterConnected();

        PrintPayload printPayload = new PrintPayload("ID-1");
        printPayload.append("TEST text");

        TestObserver<PrintJob> obs = printerDriverImpl.print(printPayload).test();
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayload).isNotNull();
        assertThat(printerDriverImpl.executePrintPayload.getRows().length).isEqualTo(1);
        TextRow textRow = (TextRow) printerDriverImpl.executePrintPayload.getRows()[0];
        assertThat(textRow.getText()).isEqualTo("TEST text");
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(0);

        assertInProgress(obs);
    }

    @Test
    public void printCausesAConnectionAndThenPrintingThenDisconnection() {

        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnTaskCompleted();

        TestObserver<PrintJob> obs = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(1);

        assertCompleteWithState(obs, PrintJob.State.PRINTED);
    }

    @Test
    public void aFailedConnectionDoesNotPrint() {

        printerDriverImpl.setAutomaticOnDriverError("DRIVER-ERROR");

        TestObserver<PrintJob> obs = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);

        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(0);
        assertCompleteAndFailedWithTheReason(obs, "DRIVER-ERROR");
    }

    @Test
    public void onPrintingFailedDisconnectsAutomatically() {

        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnPrintingFailed("Printing-Failed");

        TestObserver<PrintJob> obs = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(1);

        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(1);
        assertCompleteAndFailedWithTheReason(obs, "Printing-Failed");
    }

    @Test
    public void onActionFailedDisconnectsAutomaticallyAndSendsAStatusMessage() {
        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnActionFailed("Action-Failed");

        printerDriverImpl.sendPrinterAction("Printer-Action");
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);

        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(1);

        verifyStatusWasSent(mockPrintingContext, "Action-Failed");
    }

    @Test
    public void onTwoSimultaneousPrintRequestsTheSecondOneReturnsBusy() {
        TestObserver<PrintJob> observer1 = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        TestObserver<PrintJob> observer2 = printerDriverImpl.print(new PrintPayload("ID-1")).test();

        assertCompleteAndFailedWithTheReason(observer2, PrinterMessages.ERROR_BUSY);
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(0);
        printerDriverImpl.setAutomaticOnTaskCompleted();
        printerDriverImpl.onPrinterConnected();
        assertCompleteWithState(observer1, PrintJob.State.PRINTED);
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(1);
    }

    @Test
    public void onTwoOverLappingPrintAndActionRequestGetHandledCorrectly() {
        TestObserver<PrintJob> obs = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        printerDriverImpl.sendPrinterAction("Printer-Action");
        printerDriverImpl.onPrinterConnected();
        printerDriverImpl.onTaskCompletedSuccessfully();
        printerDriverImpl.onTaskCompletedSuccessfully();
        assertCompleteWithState(obs, PrintJob.State.PRINTED);
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(1);
        printerDriverImpl.onPrinterDisconnected();

        // Same test again but do the sendPrinterAction first
        printerDriverImpl.sendPrinterAction("Printer-Action");
        TestObserver<PrintJob> obs2 = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        printerDriverImpl.onPrinterConnected();
        printerDriverImpl.onTaskCompletedSuccessfully();
        printerDriverImpl.onTaskCompletedSuccessfully();
        assertCompleteWithState(obs2, PrintJob.State.PRINTED);
        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(2);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(2);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(2);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(2);
    }

    @Test
    public void multipleSequentialPrintingAndActions() {
        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnTaskCompleted();

        TestObserver<PrintJob> obs = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertCompleteWithState(obs, PrintJob.State.PRINTED);

        TestObserver<PrintJob> obs2 = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertCompleteWithState(obs2, PrintJob.State.PRINTED);

        printerDriverImpl.sendPrinterAction("Printer-Action");

        printerDriverImpl.sendPrinterAction("Printer-Action");

        TestObserver<PrintJob> obs3 = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertCompleteWithState(obs3, PrintJob.State.PRINTED);

        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(5);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(3);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(2);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(5);
    }

    @Test
    public void onDriverErrorMethodCancelsOtherTasks() {

        printerDriverImpl.sendPrinterAction("Printer-Action");
        TestObserver<PrintJob> obs = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        printerDriverImpl.onDriverError("TEST-ERROR", null);
        assertCompleteAndFailedWithTheReason(obs, "TEST-ERROR");

        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);

        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(0);


        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnTaskCompleted();

        TestObserver<PrintJob> obs2 = printerDriverImpl.print(new PrintPayload("ID-1")).test();
        assertCompleteWithState(obs2, PrintJob.State.PRINTED);

        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(2);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(1);
    }

    @Test
    public void onAPrintCommandThatOverlapsTheDisconnectionGetExecuted() {
        printerDriverImpl.sendPrinterAction("Printer-Action");
        printerDriverImpl.onPrinterConnected();
        printerDriverImpl.onTaskCompletedSuccessfully();

        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnTaskCompleted();
        TestObserver<PrintJob> obs2 = printerDriverImpl.print(new PrintPayload("ID-1")).test();

        printerDriverImpl.onPrinterDisconnected();

        assertCompleteWithState(obs2, PrintJob.State.PRINTED);

        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(2);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(1);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(2);
    }

    @Test
    public void onAnActionCommandThatOverlapsTheDisconnectionGetExecuted() {
        printerDriverImpl.sendPrinterAction("Printer-Action");
        printerDriverImpl.onPrinterConnected();
        printerDriverImpl.onTaskCompletedSuccessfully();

        printerDriverImpl.setAutomaticOnPrinterConnected();
        printerDriverImpl.setAutomaticOnTaskCompleted();
        printerDriverImpl.sendPrinterAction("Printer-Action");

        printerDriverImpl.onPrinterDisconnected();

        assertThat(printerDriverImpl.connectToPrinterCounter).isEqualTo(2);
        assertThat(printerDriverImpl.executePrintPayloadTaskCounter).isEqualTo(0);
        assertThat(printerDriverImpl.executePrintActionTaskCounter).isEqualTo(2);
        assertThat(printerDriverImpl.disconnectFromPrinterCounter).isEqualTo(2);
    }

    private void assertInProgress(TestObserver<PrintJob> obs) {
        obs.assertNoErrors();
        obs.assertNotComplete();
        List<PrintJob> values = obs.values();

        assertThat(values.size()).isEqualTo(1);
        assertThat(values.get(values.size() - 1).getPrintJobState()).isEqualTo(PrintJob.State.IN_PROGRESS);
    }

    protected void assertCompleteWithState(TestObserver<PrintJob> obs, PrintJob.State state) {
        obs.assertNoErrors();
        obs.assertComplete();
        List<PrintJob> values = obs.values();

        assertThat(values.size()).isGreaterThanOrEqualTo(1);
        assertThat(values.get(values.size() - 1).getPrintJobState()).isEqualTo(state);
    }

    protected void assertCompleteAndFailedWithTheReason(TestObserver<PrintJob> obs, String failedReason) {
        obs.assertNoErrors();
        obs.assertComplete();
        List<PrintJob> values = obs.values();

        assertThat(values.size()).isGreaterThanOrEqualTo(1);
        PrintJob printJob = values.get(values.size() - 1);
        assertThat(printJob.getPrintJobState()).isEqualTo(PrintJob.State.FAILED);
        assertThat(printJob.getFailedReason()).isEqualTo(failedReason);
    }

    private void verifyStatusWasSent(PrintingContext printingContext, String status) {
        ArgumentCaptor<String> printerStatusArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(printingContext).send(printerStatusArgumentCaptor.capture());
        assertThat(PrinterStatus.fromJson(printerStatusArgumentCaptor.getValue()).getStatus()).isEqualTo(status);
    }

    class MyPrinterStatusStream extends PrinterStatusStream {
        @Override
        public void subscribeToStatus(final PrintingContext printingContext, final String printerId) {
            super.subscribeToStatus(printingContext, printerId);
        }
    }
}
