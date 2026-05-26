package com.mycompany.charitymanagement;

public class UserAccount {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_VOLUNTEER = "VOLUNTEER";
    public static final String ROLE_SPONSOR = "SPONSOR";

    private final String username;
    private final String password;
    private final String role;
    private final String displayName;
    private final String linkedId;

    public UserAccount(String username, String password, String role, String displayName, String linkedId) {
        this.username = UiText.clean(username);
        this.password = password;
        this.role = UiText.clean(role);
        this.displayName = UiText.clean(displayName);
        this.linkedId = UiText.clean(linkedId);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLinkedId() {
        return linkedId;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public boolean isVolunteer() {
        return ROLE_VOLUNTEER.equals(role);
    }

    public boolean isSponsor() {
        return ROLE_SPONSOR.equals(role);
    }
}
