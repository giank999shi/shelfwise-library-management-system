package com.school.library.model;

public enum LoanStatus {
    BORROWED, RETURNED;

    public String getLabel() {
        String s = name().toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
