@regression
Feature: First two Stock end-to-end test cases

  @authenticated @serial @smoke @e2e @e2e_stk_001
  Scenario: E2E-STK-001 - Authentication to Stock landing and Products
    When the user opens the Stock landing route
    Then the Stock landing route should provide a usable page
    When the user opens the Stock Products page
    Then the Stock Products page should be available

  @authenticated @serial @mutation @e2e @e2e_stk_002
  Scenario: E2E-STK-002 - Complete Product lifecycle
    Given unique data for a Stock product lifecycle
    When the user creates the Stock product with price and barcode
    Then the created Stock product should be searchable
    When the user updates the Stock product name and price
    Then the updated Stock product name and price should be persisted
    When the user deactivates the Stock product
    Then the Stock product should be inactive
    When the user deletes the Stock product
    Then the Stock product should no longer be searchable
