# AEVI Printer Driver API

This API allows developers to quickly and easily create a driver for any manufacturer of POS style printer which will in turn enable it to be used by
value-added-application developers who are developing applications for AEVI enabled devices.

# Prerequisites

This API is an entry point to the AEVI Printer Driver Service. In order to use this API sucessfully on a
device the AEVI printing service application must be installed along with your printer driver application
that will handle the actual print process with the physical devices.

# Binaries

In your main gradle.build you'll need to include our public bintray in your main
repositories section.

```
    repositories {
        maven {
            url "http://dl.bintray.com/aevi/aevi-uk"
        }
    }
```

And then add to your dependencies section

```
implementation 'com.aevi.print:printer-driver-api:1.1.3'

```

# The printer driver framework API

The printer driver framework is an optional framework that simplifies the task of writing 
a new printer driver and sits on top of the printer driver API. 
For full documentation of this framework please see the 
[printer driver Wiki](https://github.com/Aevi-UK/android-printer-driver-api/wiki)


# Printer driver demos
The are two demos of printer drivers that show how to use the lower level printer driver API 
and also the driver framework classes.

## DemoPrinterDriver

The `DemoPrinterDriver` application contained here is an example printer driver implementing this directly API. 
This driver will print to the standard Android
PrintManager, in a real example you would replace the Android PrintManager with calls to the manufacturer specific API/SDK for the physical printer device.

## DemoPrinterFrameworkDriver

The `DemoPrinterFrameworkDriver` application is an example printer driver that uses the 
the printer driver framework to implement the printer driver. 
In order to convert the demo into a functional printer driver then the `DemoPrinterDriver` class
needs updating with calls to the manufacturer specific API/SDK for the physical printer device.


# Full Documentation

* [Wiki](https://github.com/Aevi-UK/android-printer-driver-api/wiki) 
* [Javadoc](https://aevi-uk.github.io/android-printer-driver-api/javadoc/index.html)

The developer API that other developers can use to print via your driver [see the print-api here](https://github.com/Aevi-UK/android-pos-print-api).


# Bugs and Feedback

For bugs, feature requests and discussion please use [GitHub Issues](https://github.com/Aevi-UK/android-printer-driver-api/issues)

# LICENSE

Copyright 2017 AEVI International GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
