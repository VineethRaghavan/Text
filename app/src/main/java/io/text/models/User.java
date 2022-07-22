package io.text.models;

public class User {

    private String uid;
    private String name;
    private String publicKey;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String name, String publicKey) {
        this.uid = uid;
        this.name = name;
        this.publicKey = publicKey;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
