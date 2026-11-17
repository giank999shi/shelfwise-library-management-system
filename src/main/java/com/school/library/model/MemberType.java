package com.school.library.model;

public enum MemberType {
    STUDENT(3, 7), TEACHER(5, 14);

    private final int maxBooks;
    private final int loanDays;

    MemberType(int maxBooks, int loanDays) { this.maxBooks = maxBooks; this.loanDays = loanDays; }
    public int getMaxBooks() { return maxBooks; }
    public int getLoanDays() { return loanDays; }

    public String getLabel() {
        String s = name().toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
