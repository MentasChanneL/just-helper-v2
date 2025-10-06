package com.prikolz.justhelper.dev;

public enum VariableType {
    GAME("game"),
    LOCAL("local"),
    SAVE("save");

    public static VariableType getByID(String id) {
        if (id == null) return null;
        for (VariableType type : VariableType.values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }

    public final String id;

    VariableType(String id) {
        this.id = id;
    }
}
