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
import com.aevi.print.model.PrintingContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowLog;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class PrinterStatusStreamTest {

    @Rule
    public final TrampolineSchedulerRule trampolineSchedulerRule = new TrampolineSchedulerRule();

    @Mock
    private BasePrinterStatusService mockPrinterStatusService;

    private PrinterStatusStream printerStatusStream;

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        initMocks(this);
        printerStatusStream = new PrinterStatusStream();
    }

    @Test
    public void canSubscribeToPrinterStatuses() {
        PrintingContext printingContext = Mockito.mock(PrintingContext.class);

        printerStatusStream.subscribeToStatus(printingContext, "123456");

        PrinterStatusStream.emitStatus("123456", "Hello");

        verifyStatusWasSent(printingContext, "Hello");
    }

    @Test
    public void checkWontGetStatusFromOtherPrinterId() {
        PrintingContext printingContext = Mockito.mock(PrintingContext.class);

        printerStatusStream.subscribeToStatus(printingContext, "765431");

        PrinterStatusStream.emitStatus("123456", "Hello");

        verifyStatusWasNotSent(printingContext);
    }

    @Test
    public void checkMultipleSubscriptions() {
        PrintingContext printingContext = Mockito.mock(PrintingContext.class);
        PrintingContext printingContext2 = Mockito.mock(PrintingContext.class);

        printerStatusStream.subscribeToStatus(printingContext, "123456");
        printerStatusStream.subscribeToStatus(printingContext2, "123456");

        PrinterStatusStream.emitStatus("123456", "Hello");

        verifyStatusWasSent(printingContext, "Hello");
        verifyStatusWasSent(printingContext2, "Hello");
    }

    @Test
    public void canFinishPrinterStatusStream() {
        PrintingContext printingContext = Mockito.mock(PrintingContext.class);

        printerStatusStream.subscribeToStatus(printingContext, "123456");

        PrinterStatusStream.finishPrinter("123456");

        verifyEndWasSent(printingContext);
    }

    private void verifyEndWasSent(PrintingContext printingContext) {
        verify(printingContext).sendEndStream();
    }

    private void verifyStatusWasSent(PrintingContext printingContext, String status) {
        ArgumentCaptor<String> printerStatusArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(printingContext).send(printerStatusArgumentCaptor.capture());
        assertThat(PrinterStatus.fromJson(printerStatusArgumentCaptor.getValue()).getStatus()).isEqualTo(status);
    }

    private void verifyStatusWasNotSent(PrintingContext printingContext) {
        verify(printingContext, times(0)).send(anyString());
    }
}
