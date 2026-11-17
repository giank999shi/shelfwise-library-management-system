package com.school.library.model;

public enum Role {
    ADMIN, LIBRARIAN, TEACHER, STUDENT;

    public String getLabel() {
        String s = name().toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
