# Printer statuses service

```java
public class DemoPrinterStatusService extends BasePrinterStatusService {
    // There is nothing to do here
}
```

This service should extend the `BasePrinterStatusService` class. No implementation is required, this class serves merely as a way to expose the service in your Android manifest.

```java

PrinterStatusStream.emitStatus("printerId", PrinterMessages.ERROR_PRINT_FAILED);

```

Statuses can be sent to the user using the `PrinterStatusStream.emitStatus()` method. This method requires you pass in the id of the printer the status is for and a String status message. Various default messages are provided in the `PrinterMessages` class.

