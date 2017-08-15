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

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class PrinterStatusStream {

    private static final Map<String, PublishSubject<PrinterStatus>> PRINTER_STATUS_STREAM_MAP = new HashMap<>();

    private final BasePrinterStatusService service;

    PrinterStatusStream(BasePrinterStatusService service) {
        this.service = service;
    }

    public static void emitStatus(String printerId, String printerStatus) {
        if (PRINTER_STATUS_STREAM_MAP.containsKey(printerId)) {
            PRINTER_STATUS_STREAM_MAP.get(printerId).onNext(new PrinterStatus(printerStatus));
        }
    }

    public static void finishPrinter(String printerId) {
        if (PRINTER_STATUS_STREAM_MAP.containsKey(printerId)) {
            PublishSubject<PrinterStatus> printerStatusStream = PRINTER_STATUS_STREAM_MAP.get(printerId);
            printerStatusStream.onComplete();
            PRINTER_STATUS_STREAM_MAP.remove(printerId);
        }
    }

    void subscribeToStatus(final PrinterStatusRequest statusRequest) {
        synchronized (PRINTER_STATUS_STREAM_MAP) {
            PublishSubject<PrinterStatus> printerStatusStream;
            if (PRINTER_STATUS_STREAM_MAP.containsKey(statusRequest.getPrinterId())) {
                printerStatusStream = PRINTER_STATUS_STREAM_MAP.get(statusRequest.getPrinterId());
            } else {
                printerStatusStream = PublishSubject.create();
                PRINTER_STATUS_STREAM_MAP.put(statusRequest.getPrinterId(), printerStatusStream);
            }

            printerStatusStream.subscribe(new Observer<PrinterStatus>() {
                Disposable disposable;

                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    this.disposable = d;
                }

                @Override
                public void onNext(@NonNull PrinterStatus printerStatus) {
                    if (!service.sendMessageToClient(statusRequest.getId(), printerStatus)) {
                        disposable.dispose();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    finish();
                }

                @Override
                public void onComplete() {
                    finish();
                }

                private void finish() {
                    service.sendEndStreamMessageToClient(statusRequest.getId());
                    disposable.dispose();
                }
            });
        }
    }
}
