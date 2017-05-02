package com.example.asypereddi.goingsomewhere;

import java.util.ArrayList;
import java.util.List;

/**
 * Trip objects contain facts about the specific trips and additionally the two lists of items which
 * the group and the individual user use. These objects are stored in the Firebase Database under the
 * name "trips".
 */

public class Trip {
    private String name;
    private String date;
    private String location;
    private List<String> userIds;

    public Trip(String name, String date, String location, String userId) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.userIds = new ArrayList<>();
        userIds.add(userId);
    }

    public Trip() {
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void removeUserId(String userId) {
        userIds.remove(userId);
    }
}
