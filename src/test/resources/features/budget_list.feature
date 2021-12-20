Feature: Budget - List
  Verifies rules for listing budgets

  Scenario: A member can list his organization's budgets
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
    When "Mike" get budgets for "Unnamed Company" organization
    Then It returns following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |
      | Unnamed Company | Annual Unnamed Pot #2 | 150             | false         | Sabrina | Unnamed Terms of Use |
      | Unnamed Company | Annual Unnamed Pot #3 | 150             | true          | Sabrina | Unnamed Terms of Use |

  Scenario: A non member of organization cannot list organization's budgets
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
      | Sabrina | Another Company |
    And The following contents are saved
      | organization    | name                 | value     |
      | Another Company | Another Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules                |
      | Another Company | Annual Another Pot | 150             | true          | Sabrina | Another Terms of Use |
    And "Mike" is logged in
    When "Mike" get budgets for "Another Company" organization
    Then Last HTTP code was "403"
