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
package com.aevi.demoprinterdriver;

import android.content.Intent;
import android.util.Log;

import com.aevi.android.rxmessenger.activity.ObservableActivityHelper;
import com.aevi.demoprinterdriver.model.PrinterSettingsHolder;
import com.aevi.demoprinterdriver.ui.AndroidPrintActivity;
import com.aevi.print.driver.BasePrinterDriverService;
import com.aevi.print.model.PrintJob;
import com.aevi.print.model.PrintPayload;
import com.aevi.print.model.PrinterMessages;
import com.aevi.print.model.PrinterSettings;
import com.aevi.print.model.PrintingContext;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;
import static com.aevi.demoprinterdriver.ui.AndroidPrintActivity.KEY_PAYLOAD;
import static com.aevi.demoprinterdriver.ui.AndroidPrintActivity.KEY_PRINTER_SETTINGS;

public class DemoPrinterDriverService extends BasePrinterDriverService {

    private static final String TAG = DemoPrinterDriverService.class.getSimpleName();

    @Override
    protected void print(final PrintingContext printingContext, final PrintPayload payload) {
        Log.d(TAG, "Got print request: " + payload.getPrinterId());
        PrinterSettings[] printerSettings = PrinterSettingsHolder.getInstance().getPrinterSettings();

        Intent intent = new Intent(getBaseContext(), AndroidPrintActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(KEY_PAYLOAD, payload.toJson());
        intent.putExtra(KEY_PRINTER_SETTINGS, printerSettings[0].toJson());

        ObservableActivityHelper<PrintJob> helper = ObservableActivityHelper.createInstance(this, intent);
        helper.startObservableActivity().subscribe(
                new Consumer<PrintJob>() {
                    @Override
                    public void accept(@NonNull PrintJob printJob) throws Exception {
                        printingContext.send(printJob.toJson());
                        printingContext.sendEndStream();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e(TAG, "Print failed", throwable);
                        printingContext.sendError(PrinterMessages.ERROR_PRINT_FAILED,
                                "Failed to print: " + throwable.getMessage());
                    }
                });
    }
}
