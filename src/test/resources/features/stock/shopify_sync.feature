@regression @stock @allure.label.epic:Stock @allure.label.feature:Stock @authenticated @serial @mutation
Feature: Feature to test Shopify sync

  Background: User has logged in and opened Stock Products
    Given User is on Stock Products Page

  Scenario: Shopify sync successfully
    When User clicks Shopify sync button
    Then Show Shopify sync successful
