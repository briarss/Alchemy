package com.cobblemonodyssey.$mod_id$.data;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

public class UserData {
    @BsonProperty("_id")
    public UUID uuid;
    @BsonProperty
    public String example;

    public UserData(UUID uuid) {
        this.uuid = uuid;
        this.example = "";
    }

    public UserData(UUID uuid, String example) {
        this.uuid = uuid;
        this.example = example;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                ", example=" + example +
                '}';
    }
}
