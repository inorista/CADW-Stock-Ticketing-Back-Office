package com.cadw.automation.base;

import com.cadw.automation.support.AuthenticationSupport;
import org.testng.annotations.BeforeMethod;

public abstract class AuthenticatedTest extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void authenticate() {
        AuthenticationSupport.ensureAuthenticated();
    }
}
