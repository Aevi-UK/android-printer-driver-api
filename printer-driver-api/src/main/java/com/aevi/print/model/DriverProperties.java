package com.aevi.print.model;

import com.aevi.print.json.JsonConverter;
import com.aevi.print.json.Jsonable;

public class DriverProperties implements Jsonable {

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
