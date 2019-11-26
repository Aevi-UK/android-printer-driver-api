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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.aevi.print.model.DriverProperties;
import com.aevi.print.model.PrinterSettings;
import com.aevi.print.model.PrinterSettingsList;
import com.aevi.util.json.JsonConverter;

/**
 * ContentProvider base class that should be extended by printer driver providers in order to give information as to the capabilities of printers.
 * The implementing class will need to provide an implementation of the {@link #getPrintersSettings()}}
 * methods that should return the configuration of the printers. If this configuration is dynamic and changes then the implementing class should call
 * {@link #notifyConfigurationChange()} on any changes so that the new configuration can be obtained by the Printer Control Service.
 *
 * The implementation of this class should be added as a provider in the <code>AndroidManifest.xml</code>
 *
 * <pre>
 *     {@code
 *          <provider
 *              android:name=".services.ConfigurationProvider"
 *              android:authorities="com.example.printer.service.config"
 *              android:exported="true"/>
 *     }
 * </pre>
 */
public abstract class BasePrinterSettingsProvider extends ContentProvider {

    public static final String ACTION_BROADCAST_CONFIG_CHANGE = "com.aevi.intent.action.PRINTER_DRIVER_CONFIG_CHANGE";
    public static final String CONFIGURATION_KEY = "configuration";
    public static final String PROPERTIES_KEY = "properties";

    public static final String METHOD_ALL = "all";
    public static final String METHOD_DRIVER_PROPERTIES = "driver-properties";

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Bundle call(String method, String arg, Bundle extras) {
        Bundle b = new Bundle();
        switch (method) {
            case METHOD_ALL:
                b.putString(CONFIGURATION_KEY, JsonConverter.serialize(new PrinterSettingsList(getPrintersSettings())));
                break;
            case METHOD_DRIVER_PROPERTIES:
                b.putString(PROPERTIES_KEY, JsonConverter.serialize(getDriverProperties()));
                break;
        }
        return b;
    }

    protected abstract PrinterSettings[] getPrintersSettings();

    protected abstract DriverProperties getDriverProperties();

    public void notifyConfigurationChange() {
        String pkg = "package:" + getContext().getPackageName();
        Uri pkgUri = Uri.parse(pkg);
        getContext().sendBroadcast(new Intent(ACTION_BROADCAST_CONFIG_CHANGE).setData(pkgUri));
    }
}