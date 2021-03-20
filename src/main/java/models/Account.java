package models;

import java.io.Serializable;

public class Account implements Serializable {
    private final String username;
    private final String password;
    private final boolean isAdmin;

    public Account(final String username, final String password, final boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
