package com.cadw.automation.config;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EnvironmentConfigTest {

    @Test
    public void testLoadDefaultDevEnvironment() {
        EnvironmentConfig config = EnvironmentConfig.load("dev");
        Assert.assertEquals(config.environment(), "dev");
        Assert.assertNotNull(config.baseUrl());
        Assert.assertFalse(config.baseUrl().isBlank());
        Assert.assertEquals(config.browser(), "chrome");
        Assert.assertEquals(config.explicitWaitSeconds(), 30);
        Assert.assertEquals(config.pageLoadTimeoutSeconds(), 60);
        Assert.assertEquals(config.windowWidth(), 1920);
        Assert.assertEquals(config.windowHeight(), 1080);
    }

    @Test
    public void testLoadStagingEnvironment() {
        EnvironmentConfig config = EnvironmentConfig.load("staging");
        Assert.assertEquals(config.environment(), "staging");
        Assert.assertTrue(config.baseUrl().startsWith("https://"));
        Assert.assertTrue(config.isHeadless());
    }

    @Test
    public void testLoadProdEnvironment() {
        EnvironmentConfig config = EnvironmentConfig.load("prod");
        Assert.assertEquals(config.environment(), "prod");
        Assert.assertTrue(config.baseUrl().startsWith("https://"));
        Assert.assertTrue(config.isHeadless());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnsupportedEnvironmentThrows() {
        EnvironmentConfig.load("invalid-env");
    }

    @Test
    public void testSystemPropertyOverride() {
        String key = "test.custom.override.key";
        String value = "overridden-value";
        System.setProperty(key, value);
        try {
            EnvironmentConfig config = EnvironmentConfig.active();
            Assert.assertEquals(config.getString(key), value);
            Assert.assertEquals(config.optionalString(key).orElse(""), value);
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    public void testGettersWithDefaults() {
        EnvironmentConfig config = EnvironmentConfig.active();
        Assert.assertEquals(config.getString("non.existent.key", "defaultStr"), "defaultStr");
        Assert.assertEquals(config.getInt("non.existent.int", 99), 99);
        Assert.assertTrue(config.getBoolean("non.existent.bool", true));
        Assert.assertFalse(config.getBoolean("non.existent.bool", false));
    }

    @Test
    public void testEnvironmentIncludesExecutionSettings() {
        EnvironmentConfig config = EnvironmentConfig.load("dev");
        Assert.assertEquals(config.getString("execution"), "local");
        Assert.assertEquals(config.implicitWaitSeconds(), 0);
        Assert.assertEquals(config.scriptTimeoutSeconds(), 30);
    }
}
