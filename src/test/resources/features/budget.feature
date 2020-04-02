Feature: Budget
  Verifies rules for using budgets

  Scenario: A member can list his usable budgets
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
      | Another Company |
    And The following users are registered
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following users are members of organizations
      | user    | organization    |
      | Mike    | Unnamed Company |
      | Sabrina | Unnamed Company |
      | Sabrina | Another Company |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
      | Another Company | Another Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |
      | Unnamed Company | Annual Unnamed Pot #2 | 150             | false         | Sabrina | Unnamed Terms of Use |
      | Another Company | Annual Another Pot    | 150             | true          | Sabrina | Another Terms of Use |
    And The following budgets are passed
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #3 | 150             | true          | Sabrina | Unnamed Terms of Use |
    And "Mike" is logged in
    When "Mike" get usable budgets
    Then It returns following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |