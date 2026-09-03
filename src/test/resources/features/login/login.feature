@regression @auth @login @allure.label.epic:Authentication @allure.label.feature:Login
Feature: Feature to test Aramark login

  @smoke
  Scenario: Login with configured username and password
    Given User open the Aramark login page
    Then User is on login page
    And User enters the configured valid credentials
    And User clicks login button
    Then User is navigated to home page

  @smoke
  Scenario: Login with invalid username and password
    Given User open the Aramark login page
    Then User is on login page
    And User enters invalid "[REDACTED_USERNAME]" and "[REDACTED_PASSWORD]"
    And User clicks login button
    Then Browser show invalid username and password
