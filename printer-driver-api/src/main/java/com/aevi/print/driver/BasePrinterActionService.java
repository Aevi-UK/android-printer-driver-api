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

import com.aevi.android.rxmessenger.AbstractMessengerService;
import com.aevi.android.rxmessenger.SendableId;
import com.aevi.print.model.PrintAction;

/**
 * This abstract service should be extended to provide a print action handler service implementation
 */
public abstract class BasePrinterActionService extends AbstractMessengerService<PrintAction, SendableId> {

    protected BasePrinterActionService() {
        super(PrintAction.class);
    }

    @Override
    protected void handleRequest(PrintAction action, String packageName) {
        action(action.getPrinterId(), action.getAction());
    }

    protected abstract void action(String printerId, String action);

    protected void actionComplete(PrintAction action) {
        sendEndStreamMessageToClient(action.getId());
    }
}
