@allure.label.epic:Stock @allure.label.feature:Stock @authenticated @serial @smoke @product_search
Feature: Feature to test Product Search

  Background: User has logged in and opened Stock Products
    Given User is on Stock Products Page

  @smoke
  Scenario Outline: Search product with valid data
    When User enter "<Product Name>" to Product name search bar
    And User enter "<Product Code>" to Product code search bar
    And User send Enter key
    Then Product list should show products matching "<Product Name>" and "<Product Code>"

    Examples:
      | Product Name | Product Code |
      | ShopifyTest  | ShopifyTest  |
      | Dan TEst 10  |         1915 |

  @smoke
  Scenario Outline: Search product with invalid data
    When User enter "<Product Name>" to Product name search bar
    And User enter "<Product Code>" to Product code search bar
    And User send Enter key
    Then Page should show 'No data to display here.' message

    Examples:
      | Product Name         | Product Code |
      | Test product 213123  |        12366 |
      | TRest Fialed product |       123572 |
