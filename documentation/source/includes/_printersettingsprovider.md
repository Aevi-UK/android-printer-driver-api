# Printer settings provider

```java
public class DemoPrinterSettingsProvider extends BasePrinterSettingsProvider {

    @Override
    protected PrinterSettings[] getPrintersSettings() {
        return PrinterSettingsHolder.getInstance().getPrinterSettings();
    }
}

```

The printer settings provider implementation should extend the `BasePrinterSettingsProvider` class and implement the `getPrintersSettings` method.

This method is responsible for returning a graph of objects indicating the current settings and capabilities of the printer(s) that this driver can connect to.

A `PrinterSettings` object will be returned for each known printer this driver can access.

The `PrinterSettings` object contains information about:
* paperwidth - The paper width supported by this printer in mm
* printableWidth - The maximum width this printer can print on the paper size given above in mm
* dotsPerMm - The number of dots this printer can support per mm (to convert to dpi multiply by 25.4)
* paperkind - The type of paper supported by this printer e.g. THERMAL, NORMAL
* codepages - The character codepages a printer will accept
* actions - The actions that can be sent to this printer to perform a task e.g. "cutPaper"
* options - A `Map` of String key value pairs that describe any printer specific options this printer exposes
* fonts - A list of fonts the printer supports
* languages - A list of language code supported by the printer. This allows text characters to be sent in different languages if the printer supports it.

```java

    private static final PrinterFont FONT_A = new PrinterFontBuilder()
            .withId(Printer.FONT_A)
            .withName("Font A")
            .withSupportedFontStyles(FontStyle.values())
            .withHeight(24)
            .withWidth(12)
            .withLineHeight(32)
            .withIsDefault(true)
            .withNumColumns(48)
            .build();

    private static final PrinterFont FONT_B = new PrinterFontBuilder()
            .withId(Printer.FONT_B)
            .withName("Font B")
            .withSupportedFontStyles(FontStyle.values())
            .withHeight(17)
            .withWidth(9)
            .withLineHeight(25)
            .withIsDefault(false)
            .withNumColumns(64)
            .build();

    private static final PrinterFont[] DEFAULT_FONTS = new PrinterFont[]{
            FONT_A,
            FONT_B
    };

    public PrinterSettings getSettings() {
        return new PrinterSettingsBuilder(getPrinterId(), PAPER_WIDTH, PRINTABLE_WIDTH, PAPER_DOTS_PER_MM)
                .withPrinterFonts(DEFAULT_FONTS)
                .withPaperKind(PaperKind.THERMAL)
                .withDoesReportStatus(true)
                .withCanHandleCommands(true)
                .withDoesSupportCodepages(false)
                .withSupportedLanguages(SUPPORTED_LANGUAGES)
                .build();
    }
```

To assist with building the `PrinterSettings` a builders are provided in this api. The example opposite shows how this may be constructed.

In particular the driver should ensure that it returns information about the Fonts available on the printer and that the sizes given are accurate. These values are used to create the Print Preview for the user when requested, so in order to provide an accurate indication of the final printout this information should be given as accuractely as possible.

> Note that sizes are in mm and/or printer dots, see the Javadoc for details.