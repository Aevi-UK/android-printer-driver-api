package com.aevi.print.model;

/**
 * An interface that should be implemented by a concrete instance in the driver class. Printer data/settings should at least expose a unique
 * ID.
 */
public interface BasePrinterInfo {

    /**
     * This printer ID should be unique for each physical printer that this driver locates
     * @return A unique printer ID
     */
    String getPrinterId();
}
