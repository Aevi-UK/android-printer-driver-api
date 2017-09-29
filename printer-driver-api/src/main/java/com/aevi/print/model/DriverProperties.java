package com.aevi.print.model;

import com.aevi.android.rxmessenger.JsonConverter;
import com.aevi.android.rxmessenger.SendableId;

public class DriverProperties extends SendableId {

    private boolean editableSettings;

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static DriverProperties fromJson(String json) {
        return JsonConverter.deserialize(json, DriverProperties.class);
    }

    public void setEditableSettings(boolean enable) {
        this.editableSettings = enable;
    }

    public boolean editableSettings() {
        return editableSettings;
    }

}
