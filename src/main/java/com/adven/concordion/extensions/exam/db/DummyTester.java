package com.adven.concordion.extensions.exam.db;

import org.dbunit.JdbcDatabaseTester;

public enum DummyTester {
    H2("org.h2.Driver", "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA", "sa", "");

    private final String driver;
    private final String url;
    private final String user;
    private final String password;

    DummyTester(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public JdbcDatabaseTester instance() throws ClassNotFoundException {
        return new JdbcDatabaseTester(driver, url, user, password);
    }
}
