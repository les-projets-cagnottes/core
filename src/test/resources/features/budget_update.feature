Feature: Budget - Update
  Verifies rules for updating budgets

  Scenario: A sponsor can update budgets
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                       | password |
      | Sabrina   | sabrina@unnamedcompany.com  | sabrina  |
      | Sinclair  | sinclair@unnamedcompany.com | sinclair |
    And The following users are members of organizations
      | user     | organization    |
      | Sabrina  | Unnamed Company |
      | Sinclair | Unnamed Company |
    And The following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
      | Sinclair  | Unnamed Company | ROLE_SPONSOR |
    And The following contents are saved
      | organization    | name            | value     |
      | Unnamed Company | Terms of Use #1 | Blablabla |
      | Unnamed Company | Terms of Use #2 | Blablabla |
    And The following budgets are available
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules                |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Unnamed Terms of Use |
      | Unnamed Company | Annual Company Pot #2 | 150             | false         | Sabrina | Unnamed Terms of Use |
      | Unnamed Company | Annual Company Pot #3 | 150             | false         | Sabrina | Unnamed Terms of Use |
    And "Sabrina" is logged in
    When "Sabrina" updates following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor  | rules           |
      | Unnamed Company | Annual Company Pot #1 | 100             | false         | Sabrina  | Terms of Use #1 |
      | Unnamed Company | Annual Company Pot #2 | 150             | false         | Sinclair | Terms of Use #1 |
      | Unnamed Company | Annual Company Pot #3 | 150             | false         | Sabrina  | Terms of Use #2 |
    Then Last HTTP code was "200"
    And Following budgets are registered
      | organization    | name                  | amountPerMember | isDistributed | sponsor  | rules           |
      | Unnamed Company | Annual Company Pot #1 | 100             | false         | Sabrina  | Terms of Use #1 |
      | Unnamed Company | Annual Company Pot #2 | 150             | false         | Sinclair | Terms of Use #1 |
      | Unnamed Company | Annual Company Pot #3 | 150             | false         | Sabrina  | Terms of Use #2 |

  Scenario: A sponsor cannot update budgets if organization, sponsor or rules is missing
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
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Terms of Use |
      | Unnamed Company | Annual Company Pot #2 | 150             | false         | Sabrina | Terms of Use |
      | Unnamed Company | Annual Company Pot #3 | 150             | false         | Sabrina | Terms of Use |
    And "Sabrina" is logged in
    When "Sabrina" updates following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor         | rules        |
      | Unknown Company | Annual Company Pot #1 | 150             | false         | Sabrina         | Terms of Use |
      | Unnamed Company | Annual Company Pot #2 | 150             | false         | Unknown Sponsor | Terms of Use |
      | Unnamed Company | Annual Company Pot #3 | 150             | false         | Sabrina         | Terms of Use |
    Then Last HTTP code was "200"
    And Following budgets are registered
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Terms of Use |
      | Unnamed Company | Annual Company Pot #2 | 150             | false         | Sabrina | Terms of Use |
      | Unnamed Company | Annual Company Pot #3 | 150             | false         | Sabrina | Terms of Use |

  Scenario: A non-member cannot update a budget
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                       | password |
      | Sabrina   | sabrina@unnamedcompany.com  | sabrina  |
      | Sinclair  | sinclair@unnamedcompany.com | sinclair |
    And The following users are members of organizations
      | user     | organization    |
      | Sinclair | Unnamed Company |
    And The following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sinclair  | Unnamed Company | ROLE_SPONSOR |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                  | amountPerMember | isDistributed | sponsor  | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sinclair | Terms of Use |
    And "Sabrina" is logged in
    When "Sabrina" updates following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor  | rules        |
      | Unnamed Company | Annual Company Pot #1 | 100             | false         | Sinclair | Terms of Use |
    Then Last HTTP code was "200"
    And Following budgets are registered
      | organization    | name                  | amountPerMember | isDistributed | sponsor  | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sinclair | Terms of Use |

  Scenario: A non-sponsor cannot update a budget
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
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Terms of Use |
    And "Sabrina" is logged in
    When "Sabrina" updates following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 100             | false         | Sabrina | Terms of Use |
    Then Last HTTP code was "200"
    And Following budgets are registered
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Terms of Use |

  Scenario: A sponsor cannot re-affect a budget to a non-sponsor member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                       | password |
      | Sabrina   | sabrina@unnamedcompany.com  | sabrina  |
      | Sinclair  | sinclair@unnamedcompany.com | sinclair |
    And The following users are members of organizations
      | user     | organization    |
      | Sabrina  | Unnamed Company |
      | Sinclair | Unnamed Company |
    And The following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Terms of Use |
    And "Sabrina" is logged in
    When "Sabrina" updates following budgets
      | organization    | name                  | amountPerMember | isDistributed | sponsor  | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sinclair | Terms of Use |
    Then Last HTTP code was "200"
    And Following budgets are registered
      | organization    | name                  | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot #1 | 150             | false         | Sabrina | Terms of Use |
