package com.adven.concordion.extensions.exam.configurators;

import com.adven.concordion.extensions.exam.ExamExtension;
import org.dbunit.JdbcDatabaseTester;

import java.util.Properties;

public class DbTester {
    private String driver = "org.h2.Driver";
    private String url = "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA";
    private String user = "sa";
    private String schema = null;
    private String password = "";

    private final ExamExtension extension;

    public DbTester(ExamExtension extension) {
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

    public DbTester schema(String schema) {
        this.schema = schema;
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
            extension.dbTester(new JdbcDatabaseTester(driver, url, user, password, schema));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failure while initializing IDatabaseTester", e);
        }
        return extension;
    }
}