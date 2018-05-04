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
package com.aevi.demoprinterdriver.framework.model;

import com.aevi.print.model.BasePrinterInfo;
import com.aevi.print.model.PaperKind;
import com.aevi.print.model.PrinterMessages;
import com.aevi.print.model.PrinterSettings;
import com.aevi.print.model.PrinterSettingsBuilder;

public class DemoPrinterInfo implements BasePrinterInfo {
    private final String printerId;

    public DemoPrinterInfo(String printerId) {

        this.printerId = printerId;
    }

    @Override
    public String getPrinterId() {
        return printerId;
    }

    private static final String[] SUPPORTED_ACTION_COMMANDS = new String[]{
            PrinterMessages.ACTION_OPEN_CASH_DRAWER
    };

    public PrinterSettings getPrinterSettings() {
        return new PrinterSettingsBuilder(printerId, 80, 75, 7.68f)
                .withPaperKind(PaperKind.STANDARD)
                .withCanHandleCommands(true)
                .withDoesReportStatus(true)
                .withCommands(SUPPORTED_ACTION_COMMANDS)
                .build();
    }
}
