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

    // required variables
    private final String printerId;
    private final int paperWidth;
    private final int printableWidth;
    private final float paperDotsPmm;

    // Optional variables
    private PrinterFont[] printerFonts;
    private PaperKind paperKind;
    private int[] codePages;
    private String[] commands;
    private Map<String, String> options;
    private boolean doesReportStatus = false;
    private boolean canHandleCommands = false;
    private boolean doesSupportCodepages = false;
    private String[] supportedLanguages;

    public PrinterSettingsBuilder(String printerId, int paperWidth, int printableWidth, float paperDotsPmm) {
        this.printerId = printerId;
        this.paperWidth = paperWidth;
        this.printableWidth = printableWidth;
        this.paperDotsPmm = paperDotsPmm;
    }

    public PrinterSettingsBuilder withPrinterFonts(PrinterFont[] printerFonts) {
        this.printerFonts = printerFonts;
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

    public PrinterSettingsBuilder withSupportedLanguages(String[] supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
        return this;
    }

    public PrinterSettings build() {
        return new PrinterSettings(printerId, paperWidth, printableWidth, paperDotsPmm, paperKind, printerFonts, canHandleCommands, commands,
                doesReportStatus,
                codePages,
                doesSupportCodepages, options, supportedLanguages);
    }
}
