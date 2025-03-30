package com.karoldm.k_board_api.exceptions;

public class UserNotAuthenticated extends RuntimeException {
    public UserNotAuthenticated(String message) {
        super(message);
    }
}
