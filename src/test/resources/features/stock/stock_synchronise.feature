@regression @allure.label.epic:Stock @allure.label.feature:Stock @authenticated @stock @serial @mutation
Feature: Feature to test Stock Synchronise

  Background: User has logged in and opened Stock Products
    Given User is on Stock Products Page

  Scenario: Stock Synchronise successfully
    When User clicks Stock Synchronise button
    Then Show Ticketing and Stock Synchronized
#      Ticketing: Successfully partially synchronized
#      Stock: Successfully partially synchronized
