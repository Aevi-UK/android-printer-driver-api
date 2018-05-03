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

import java.util.HashMap;
import java.util.Map;

public abstract class PrinterDriverFactory {

    private final Map<String, PrinterDriverBase> printerDrivers = new HashMap<>();

    public PrinterDriverBase getPrinterDriver(final BasePrinterInfo printerInfo) {
        String printerId = printerInfo.getPrinterId();
        synchronized (printerDrivers) {
            PrinterDriverBase printerDriver = printerDrivers.get(printerId);
            if (printerDriver == null) {
                printerDriver = createPrinterDriver(printerInfo);
                printerDrivers.put(printerId, printerDriver);
            }
            return printerDriver;
        }
    }

    public void deletePrinterDriver(String printerId) {
        synchronized (printerDrivers) {
            printerDrivers.remove(printerId);
        }
    }

    protected abstract PrinterDriverBase createPrinterDriver(BasePrinterInfo printerInfo);
}
