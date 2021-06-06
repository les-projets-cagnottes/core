Feature: Budget - Create
  Verifies rules for creating budgets

  Scenario: A sponsor can create a budget
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following users are members of organizations
      | user    | organization    |
      | Sabrina | Unnamed Company |
    And The following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
    And "Sabrina" is logged in
    When "Sabrina" submits following budgets on current year
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |
    Then Last HTTP code was "201"
    And Following budgets are registered
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |

  Scenario: A non-member cannot create a budget
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
    And "Sabrina" is logged in
    When "Sabrina" submits following budgets on current year
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |
    Then Last HTTP code was "403"
    And Following budgets are registered
      | organization | name | amountPerMember | isDistributed | sponsor | rules |

  Scenario: A non-sponsor cannot create a budget
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following users are members of organizations
      | user    | organization    |
      | Sabrina | Unnamed Company |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
    And "Sabrina" is logged in
    When "Sabrina" submits following budgets on current year
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina | Unnamed Terms of Use |
    Then Last HTTP code was "403"
    And Following budgets are registered
      | organization | name | amountPerMember | isDistributed | sponsor | rules |

  Scenario: A sponsor cannot create a budget if sponsor, rules or organization are missing
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
    And "Sabrina" is logged in
    When "Sabrina" submits following budgets on current year
      | organization        | name                  | amountPerMember | isDistributed | sponsor             | rules                    |
      | Nonexistent Company | Annual Unnamed Pot #1 | 150             | true          | Sabrina             | Unnamed Terms of Use     |
      | Unnamed Company     | Annual Unnamed Pot #1 | 150             | true          | Nonexistent sponsor | Unnamed Terms of Use     |
      | Unnamed Company     | Annual Unnamed Pot #1 | 150             | true          | Sabrina             | Nonexistent Terms of Use |
    Then Following budgets are registered
      | organization | name | amountPerMember | isDistributed | sponsor | rules |
