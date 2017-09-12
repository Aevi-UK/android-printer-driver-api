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
package com.aevi.demoprinterdriver.model;

import android.os.Build;

import com.aevi.print.model.PaperKind;
import com.aevi.print.model.PrinterSettings;
import com.aevi.print.model.PrinterSettingsBuilder;

public class PrinterSettingsHolder {

    private static PrinterSettingsHolder instance;

    public static PrinterSettingsHolder getInstance() {
        if (instance == null) {
            instance = new PrinterSettingsHolder();
        }
        return instance;
    }

    private PrinterSettingsHolder() {

    }

    public PrinterSettings[] getPrinterSettings() {
        return new PrinterSettings[]{
                new PrinterSettingsBuilder("DemoPrinter:" + Build.SERIAL, 80, 75, 7.68f)
                        .withPaperKind(PaperKind.STANDARD)
                        .build()
        };
    }
}
