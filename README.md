# AEVI Printer Driver API

This API allows developers to quickly and easily create a driver for any manufacturer of POS style printer which will in turn enable it to be used by
value-added-application developers who are developing applications for AEVI enabled devices.

# Prerequisites

This API is an entry point to the AEVI Printer Driver Service. In order to use this API sucessfully on a
device the AEVI printing service application must be installed along with your printer driver application
that will handle the actual print process with the physical devices.

# Binaries

Currently this API is under development and is therefore only published to our own bintray repository.
When we release v1 of this API we will upload it to jcenter.

Therefore, in your main gradle.build you'll need to include our public bintray in your main
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
compile 'com.aevi.print:printer-driver-api:1.0.0'

```

## DemoPrinterDriver

The `DemoPrinterDriver` application contained here is an example printer driver implementing this API. This driver will print to the standard Android
PrintManager, in a real example you would replace the Android PrintManager with calls to the manufacturer specific API/SDK for the physical printer device.

To read more details about how to use the API and how to create a manufacturer driver for AEVI enabled devices
[see our documentation here](https://aevi-uk.github.io/android-printer-driver-api)

To read more about the developer API that other developers can use to print via your driver [see the print-api here](https://github.com/Aevi-UK/android-pos-print-api)
and the [corresponding documentation here](https://aevi-uk.github.io/android-pos-print-api/)

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
