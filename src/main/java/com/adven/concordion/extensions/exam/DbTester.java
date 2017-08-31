package com.adven.concordion.extensions.exam;

import org.dbunit.JdbcDatabaseTester;

import java.util.Properties;

public class DbTester {
    String driver = "org.h2.Driver";
    String url = "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA";
    String user = "sa";
    String password = "";

    private final ExamExtension extension;

    DbTester(ExamExtension extension) {
        this.extension = extension;
    }

    public DbTester driver(String driver) {
        this.driver = driver;
        return this;
    }

    public DbTester url(String url) {
        this.url = url;
        return this;
    }

    public DbTester user(String user) {
        this.user = user;
        return this;
    }

    public DbTester password(String password) {
        this.password = password;
        return this;
    }

    public DbTester from(Properties props) {
        driver = props.getProperty("hibernate.connection.driver_class");
        url = props.getProperty("connection.url");
        user = props.getProperty("hibernate.connection.username");
        password = props.getProperty("hibernate.connection.password");
        return this;
    }

    public ExamExtension end() {
        try {
            extension.dbTester(new JdbcDatabaseTester(driver, url, user, password));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failure while initializing JdbcDatabaseTester", e);
        }
        return extension;
    }
}