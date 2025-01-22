package com.artisoft.fitbuddy.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {

    private String username;

    public UserNotFoundException(String username) {
        super("User with username '" + username + "' not found");
        this.username = username;
    }

    public UserNotFoundException(String username, Throwable cause) {
        super("User with username '" + username + "' not found", cause);
        this.username = username;
    }

}
