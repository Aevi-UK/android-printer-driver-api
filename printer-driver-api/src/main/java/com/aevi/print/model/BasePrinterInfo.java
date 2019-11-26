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

/**
 * An interface that should be implemented by a concrete instance in the driver class. Printer data/settings should at least expose a unique
 * ID.
 */
public interface BasePrinterInfo {

    /**
     * This printer ID should be unique for each physical printer that this driver locates
     *
     * @return A unique printer ID
     */
    String getPrinterId();

    /**
     * Tests if the same printer is located at the same address
     *
     * @param anotherPrinter    the other printer used to make the comparison
     * @return                  true when the same printer is at the same address (often the IP address)
     */
    default boolean sameAddressAndPrinter(BasePrinterInfo anotherPrinter) {

        if (getPrinterId() == null || anotherPrinter == null) {
            return false;
        }

        return getPrinterId().equals(anotherPrinter.getPrinterId());
    }

}
