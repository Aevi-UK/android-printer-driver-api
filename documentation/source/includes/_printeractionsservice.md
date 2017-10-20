# Printer actions service

```java
public class DemoPrinterActionService extends BasePrinterActionService {

    @Override
    protected void action(String clientId, String printerId, String action) {

    }
}

```
The printer actions service should extend the `BasePrinterActionService` class and implement to action method.

String actions can be sent to this service and the driver is responsible for performing the action on the physical printer.

The actions that each printer can perform are given to the user in the `PrinterSettings` object.