@regression @allure.label.epic:Stock @allure.label.feature:Stock @authenticated @serial @mutation @add_product
Feature: Feature to test Add Product

  Background: User has logged in and opened Stock Products
    Given User is on Stock Products Page

  Scenario Outline: User Add valid Product
    When User click Add button
    Then User navigate to Add Product Page
    And User enter Product Code "<Product Code>"
    And User enter Product name "<Product name>"
    And User enter Product print text "<Product print text>"
    And User select Product group "<Product group>"
    And User select Product Type "<Product Type>"
    And User enter Commission "<Commission>"
    And User enter Order "<Order>"
    And User select Allergen "<Allergen>"
    And User select Active "<Active>"
    And User enter Maximum discount "<Maximum discount>"
    And User select Print options "<Print options>"
    And User select Purchasing VAT "<Purchasing VAT>"
    And User enter Price Type "<Price Type>"
    And User enter Price Level "<Price Level>"
    And User enter Tax Group "<Tax Group>"
    And User enter Price "<Price>"
    And User enter Cost "<Cost>"
    And User select Valid From Date
    And User click Add
    And User Search or select an export code "<Search or select an export code>"
    And User click Auto generate Barcode
    And User click Add Barcode
    And user click Save

    Examples:
      | Product Code | Product name | Product print text | Product group | Product Type | Commission |  | Order | Allergen | Active | Maximum discount | Print options | Configuration options | Purchasing VAT | Price Type | Price Level | Tax Group | Price | Cost | Search or select an export code |
      |  200266 test | Dan Test     |        200266 test | Admissions    | All          |         10 |  |    10 | Fish     | Yes    |               10 | Do not print  | Orderable             | Standard       | Regular    | Standard    | Standard  |    10 |   10 | test                            |
