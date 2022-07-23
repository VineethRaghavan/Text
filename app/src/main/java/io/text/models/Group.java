package io.text.models;

public class Group {

    private String uid;
    private String name;
    private int memberCount;

    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(Group.class)
    }

    public Group(String uid, String name) {
        this.uid = uid;
        this.name = name;
        this.memberCount = 1;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
