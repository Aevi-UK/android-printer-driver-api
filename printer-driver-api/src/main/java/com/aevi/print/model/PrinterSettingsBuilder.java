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
package com.aevi.print.model;

import java.util.Map;

public class PrinterSettingsBuilder {

    private String printerId;
    private int paperWidth;
    private int printerResolution;
    private PaperKind paperKind;
    private int[] codePages;
    private String[] commands;
    private Map<String, String> options;
    private boolean doesReportStatus = false;
    private boolean canHandleCommands = false;
    private boolean doesSupportCodepages = false;

    public PrinterSettingsBuilder withPrinterId(String printerId) {
        this.printerId = printerId;
        return this;
    }

    public PrinterSettingsBuilder withPaperWidth(int paperWidth) {
        this.paperWidth = paperWidth;
        return this;
    }

    public PrinterSettingsBuilder withPrinterResolution(int printerResolution) {
        this.printerResolution = printerResolution;
        return this;
    }

    public PrinterSettingsBuilder withPaperKind(PaperKind paperKind) {
        this.paperKind = paperKind;
        return this;
    }

    public PrinterSettingsBuilder withCodePages(int[] codePages) {
        this.codePages = codePages;
        return this;
    }

    public PrinterSettingsBuilder withDoesSupportCodepages(boolean doesSupportCodepages) {
        this.doesSupportCodepages = doesSupportCodepages;
        return this;
    }

    public PrinterSettingsBuilder withCommands(String[] commands) {
        this.commands = commands;
        return this;
    }

    public PrinterSettingsBuilder withOptions(Map<String, String> options) {
        this.options = options;
        return this;
    }

    public PrinterSettingsBuilder withCanHandleCommands(boolean canHandleCommands) {
        this.canHandleCommands = canHandleCommands;
        return this;
    }

    public PrinterSettingsBuilder withDoesReportStatus(boolean doesReportStatus) {
        this.doesReportStatus = doesReportStatus;
        return this;
    }

    public PrinterSettings build() {
        return new PrinterSettings(printerId, paperWidth, printerResolution, paperKind, canHandleCommands, commands, doesReportStatus, codePages, doesSupportCodepages, options);
    }
}
