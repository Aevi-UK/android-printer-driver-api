package com.aevi.print.model;

/**
 * An interface that should be implemented by a concrete instance in the driver class. Printer data/settings should at least expose a unique
 * ID.
 */
public interface BasePrinterInfo {

    String getPrinterId();
}
