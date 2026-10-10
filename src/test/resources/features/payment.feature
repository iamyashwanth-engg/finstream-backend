Feature: Payment API End-to-End Tests

  Background:
    * url 'http://localhost:8080/api/v1/payments'

  Scenario: 1. Successful payment settlement
    # Generate a random unique key for this specific run
    * def uniqueKey = java.util.UUID.randomUUID() + ''

    Given request { "sourceAccountId": "ACC-1001", "destinationAccountId": "ACC-1002", "currencyCode": "USD", "amount": 100.00 }
    And header X-Idempotency-Key = uniqueKey
    When method post
    Then status 201
    # Notice the spelling here matches your Java API output exactly
    And match response.transcationId == '#notnull'
    And match response.status == 'SETTLED'

  Scenario: 2. Idempotency blocks double charging
    # Generate a different key just for this duplicate test
    * def dupKey = java.util.UUID.randomUUID() + ''

    # 1st Call: Original Request (Will succeed)
    Given request { "sourceAccountId": "ACC-1001", "destinationAccountId": "ACC-1002", "currencyCode": "USD", "amount": 100.00 }
    And header X-Idempotency-Key = dupKey
    When method post
    Then status 201

    # 2nd Call: Exact same request and key (Will be caught by your API)
    Given request { "sourceAccountId": "ACC-1001", "destinationAccountId": "ACC-1002", "currencyCode": "USD", "amount": 100.00 }
    And header X-Idempotency-Key = dupKey
    When method post
    Then status 201
    And match response.status == 'SETTLED'
    And match response.message == 'Transaction previously processed (Idempotent response)'

  Scenario: 3. Missing currency code fails validation
    * def badKey = java.util.UUID.randomUUID() + ''

    Given request { "sourceAccountId": "ACC-1001", "destinationAccountId": "ACC-1002", "amount": 50.00 }
    And header X-Idempotency-Key = badKey
    When method post
    Then status 400
