package io.text.models;

public class Group {

    private String uid;
    private String name;
    private boolean newMember;
    private int memberCount;

    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(Group.class)
    }

    public Group(String uid, String name) {
        this.uid = uid;
        this.name = name;
        this.newMember = false;
        this.memberCount = 1;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public boolean getNewMember() {
        return newMember;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
