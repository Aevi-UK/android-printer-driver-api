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

import com.aevi.print.model.BasePrinterInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.shadows.ShadowLog;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PrinterDriverFactoryTest {

    @Mock
    BasePrinterInfo printerInfo1;

    @Mock
    BasePrinterInfo printerInfo2;

    PrinterDriverFactory printerDriverFactory = new PrinterDriverFactory() {
        @Override
        protected PrinterDriverBase createPrinterDriver(BasePrinterInfo printerInfo) {
            PrinterDriverBase printerDriverBase = mock(PrinterDriverBase.class);
            when(printerDriverBase.getPrinterInfo()).thenReturn(printerInfo);
            return printerDriverBase;
        }
    };

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        initMocks(this);

        when(printerInfo1.getPrinterId()).thenReturn("ID-1");
        when(printerInfo2.getPrinterId()).thenReturn("ID-2");
    }

    @Test
    public void weGetTheCorrectPrinterIdDriver() {
        PrinterDriverBase printerDriverBase1 = printerDriverFactory.getPrinterDriver(printerInfo1);
        assertThat(printerDriverBase1.getPrinterInfo().getPrinterId()).isEqualTo("ID-1");

        PrinterDriverBase printerDriverBase2 = printerDriverFactory.getPrinterDriver(printerInfo2);
        assertThat(printerDriverBase2.getPrinterInfo().getPrinterId()).isEqualTo("ID-2");
    }

    @Test
    public void theSameInstanceOfPrinterDriverBaseIsUsed() {
        when(printerInfo1.sameAddressAndPrinter(any(BasePrinterInfo.class))).thenReturn(true);
        when(printerInfo2.sameAddressAndPrinter(any(BasePrinterInfo.class))).thenReturn(true);
        PrinterDriverBase printerDriverBase1 = printerDriverFactory.getPrinterDriver(printerInfo1);
        PrinterDriverBase printerDriverBase2 = printerDriverFactory.getPrinterDriver(printerInfo1);
        assertThat(printerDriverBase1).isSameAs(printerDriverBase2);
    }

    @Test
    public void theSameInstanceOfPrinterDriverBaseIsNotUsed() {
        when(printerInfo1.sameAddressAndPrinter(any(BasePrinterInfo.class))).thenReturn(false);
        when(printerInfo2.sameAddressAndPrinter(any(BasePrinterInfo.class))).thenReturn(false);
        PrinterDriverBase printerDriverBase1 = printerDriverFactory.getPrinterDriver(printerInfo1);
        PrinterDriverBase printerDriverBase2 = printerDriverFactory.getPrinterDriver(printerInfo1);
        assertThat(printerDriverBase1).isNotSameAs(printerDriverBase2);
    }

    @Test
    public void forDifferentPrintersANewInstanceOfPrinterDriverIsCreated() {
        PrinterDriverBase printerDriverBase1 = printerDriverFactory.getPrinterDriver(printerInfo1);
        PrinterDriverBase printerDriverBase2 = printerDriverFactory.getPrinterDriver(printerInfo2);
        assertThat(printerDriverBase1).isNotSameAs(printerDriverBase2);
    }

    @Test
    public void afterDeletionANewInstanceOfPrinterDriverIsCreated() {
        PrinterDriverBase printerDriverBase1 = printerDriverFactory.getPrinterDriver(printerInfo1);
        printerDriverFactory.deletePrinterDriver("ID-1");
        PrinterDriverBase printerDriverBase2 = printerDriverFactory.getPrinterDriver(printerInfo1);
        assertThat(printerDriverBase1).isNotSameAs(printerDriverBase2);
    }
}
