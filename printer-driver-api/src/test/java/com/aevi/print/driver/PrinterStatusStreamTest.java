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

import com.aevi.print.model.PrinterStatus;
import com.aevi.print.model.PrinterStatusRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.shadows.ShadowLog;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class PrinterStatusStreamTest {

    @Rule
    public final TrampolineSchedulerRule trampolineSchedulerRule = new TrampolineSchedulerRule();

    @Mock
    private BasePrinterStatusService mockPrinterStatusService;

    @Mock
    private PrinterStatusStream printerStatusStream;

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        initMocks(this);
        printerStatusStream = new PrinterStatusStream(mockPrinterStatusService);
    }

    @Test
    public void canSubscribeToPrinterStatuses() {
        PrinterStatusRequest printerStatusRequest = new PrinterStatusRequest("123456");
        printerStatusStream.subscribeToStatus(printerStatusRequest);

        PrinterStatusStream.emitStatus("123456", "Hello");

        verifyStatusWasSent(printerStatusRequest.getId(), "Hello");
    }

    @Test
    public void checkWontGetStatusFromOtherPrinterId() {
        PrinterStatusRequest printerStatusRequest = new PrinterStatusRequest("765431");
        printerStatusStream.subscribeToStatus(printerStatusRequest);

        PrinterStatusStream.emitStatus("123456", "Hello");

        verifyStatusWasNotSent();
    }

    @Test
    public void checkMultipleSubscriptions() {
        PrinterStatusRequest printerStatusRequest1 = new PrinterStatusRequest("123456");
        PrinterStatusRequest printerStatusRequest2 = new PrinterStatusRequest("123456");
        printerStatusStream.subscribeToStatus(printerStatusRequest1);
        printerStatusStream.subscribeToStatus(printerStatusRequest2);

        PrinterStatusStream.emitStatus("123456", "Hello");

        verifyStatusWasSent(printerStatusRequest1.getId(), "Hello");
        verifyStatusWasSent(printerStatusRequest2.getId(), "Hello");
    }

    @Test
    public void canFinishPrinterStatusStream() {
        PrinterStatusRequest printerStatusRequest = new PrinterStatusRequest("123456");
        printerStatusStream.subscribeToStatus(printerStatusRequest);

        PrinterStatusStream.finishPrinter("123456");

        verifyEndWasSent(printerStatusRequest.getId());
    }

    private void verifyEndWasSent(String requestId) {
        verify(mockPrinterStatusService).sendEndStreamMessageToClient(eq(requestId));
    }

    private void verifyStatusWasSent(String requestId, String status) {
        ArgumentCaptor<PrinterStatus> printerStatusArgumentCaptor = ArgumentCaptor.forClass(PrinterStatus.class);
        verify(mockPrinterStatusService).sendMessageToClient(eq(requestId), printerStatusArgumentCaptor.capture());
        assertThat(printerStatusArgumentCaptor.getValue().getStatus()).isEqualTo(status);
    }

    private void verifyStatusWasNotSent() {
        verify(mockPrinterStatusService, times(0)).sendMessageToClient(anyString(), any(PrinterStatus.class));
    }
}
