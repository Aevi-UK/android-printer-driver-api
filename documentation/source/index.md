---
title: AEVI Printer Driver API Reference

language_tabs:
  - java

toc_footers:
  - <a href='https://community.aevi.com'>Join the AEVI community</a>

includes:
  - printerdriverservice
  - printersettingsprovider
  - printerstatusesservice
  - printeractionsservice

search: true
---

# Introduction

The following documents describe how to use the AEVI Printer Driver API to implement a POS style printer driver for Aevi enabled devices. This API allows developers to quickly and easily create a driver for any manufacturers POS style printer which will in turn enable it to be used by value-added-application developers who are developing applications for Aevi enabled devices.

Each printer driver application is responsible for providing an implementation that is capable of connecting to, and handling printing for, one or more specific manufacturers printers. For instance a driver may be implemented to support a range of manufacturer printers.

The printer driver API itself makes extensive use of reactive (Rx) based principles. Therefore in the case of the Java API it makes heavy use of the RxJava library. To read more about Rx principles and the RxJava library itself see [the documentation here](https://github.com/ReactiveX/RxJava). For the remainder of this documentation it is assumed that the reader is familiar with asynchronous and event-based programming using observable streams.

Go register at our amazing developer portal here [developer portal](https://developer.aevi.com/).

# Getting started

Included in the driver repository is a `DemoPrinterDriver` application. This application shows the minimum steps that are required to create an Aevi enabled printer driver. This example uses the standard Android PrintManager to demonstrate the functionality, in your own implementation this is what would be replaced by printing via the manufacturer printer API or SDK.

All printer driver implementations will consist of implementations of three Android services and one Android ContentProvider.

* Driver Service - The main entry point for printing is a service that implements `BasePrinterDriverService` and provides basic printing functionality to all physically connected printers of this manufacturer type.
* Settings Provider - An Android ContentProvider that will expose all the `PrinterSettings` for the printer
* Statuses Service - A service that allows a user to monitor the status of the physical printers connected to by this driver
* Actions Service - A service that allows custom actions to be sent to physical printers e.g. cut paper, open cash drawer.

## Manifest entries

```xml
        <service
            android:name=".DemoPrinterDriverService"
            android:exported="true"
            android:permission="com.aevi.permission.PRINTER_DRIVER_SERVICE">
            <intent-filter>
                <action android:name="com.aevi.printer.driver.SERVICE"/>
            </intent-filter>

            <meta-data
                android:name="configuration-authority"
                android:value="com.aevi.demoprinterdriver.config"/>
            <meta-data
                android:name="printer-driver-name"
                android:value="@string/app_driver_name"/>
            <meta-data
                android:name="action-service"
                android:value="com.aevi.demoprinterdriver.DemoPrinterActionService"/>
            <meta-data
                android:name="status-service"
                android:value="com.aevi.demoprinterdriver.DemoPrinterStatusService"/>
        </service>

        <service
            android:name=".DemoPrinterActionService"
            android:exported="true"
            android:permission="com.aevi.permission.PRINTER_DRIVER_SERVICE">
        </service>

        <service
            android:name=".DemoPrinterStatusService"
            android:exported="true"
            android:permission="com.aevi.permission.PRINTER_DRIVER_SERVICE">
        </service>

        <provider
            android:name="com.aevi.demoprinterdriver.DemoPrinterSettingsProvider"
            android:authorities="com.aevi.demoprinterdriver.config"
            android:exported="true"
            android:permission="com.aevi.permission.PRINTER_DRIVER_SERVICE"/>
```

These four services/provider should be listed in the `AndroidManifest.xml` of your application as shown opposite.

Notice that the main driver service defines mata-data entries which provide the class names of the other three services/provider.

Also note that ALL the services/provider specify the permission `com.aevi.permission.PRINTER_DRIVER_SERVICE`. This is to ensure that only the Aevi printing service can access these services.