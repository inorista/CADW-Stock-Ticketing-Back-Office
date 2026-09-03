@allure.label.epic:Stock @allure.label.feature:Stock @authenticated @serial @mutation @smoke @add_order
Feature: Feature to test add Order

  Background: User has logged in and opened Stock Products
    Given User is on Stock Products Page

  @smoke
  Scenario Outline: User Add a valid Order
    When User click Stock dropdown
    Then Stock dropdown show menu
    When User click Orders
    Then User is on Stock Orders Page
    When User click Add Order button
    Then User is on Stock Orders Add Page
    When User add Order comments "<Order comment>"
    And User search for supplier "<Supplier name>"
    And User select suppliers "<Supplier name>"
    And User click Next
    Then User is on Stock Add Order Details Page
    And User click Save
    Then User go back to Stock Order Page

    Examples:
      | Order comment | Supplier name    |
      | Test comments | Default Supplier |
