package com.mycompany.charitymanagement;

public final class UserSession {

    private static UserAccount currentUser;

    private UserSession() {
    }

    public static UserAccount getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserAccount user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
