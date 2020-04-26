Feature: Donation - Create
  Verifies rules for create donations

  Scenario: A member can contribute on a campaign in progress whose deadline has not been reached
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget             | amount |
      | Mike  | Annual Company Pot | 150    |
    And The following campaigns are running
      | title            | leader | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign         | budget             | contributor | amount |
      | Awesome Campaign | Annual Company Pot | Mike        | 50     |
    Then "Mike" has "1" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user | budget             | amount |
      | Mike | Annual Company Pot | 100    |

  Scenario: A member cannot contribute on a campaign with a nonexistent account, campaign, budget or contributor
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget             | amount |
      | Mike  | Annual Company Pot | 150    |
    And The following campaigns are running
      | title            | leader | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign             | budget                  | contributor | amount |
      | Nonexistent Campaign | Annual Company Pot      | Mike        | 50     |
      | Awesome Campaign     | Nonexistent Company Pot | Mike        | 50     |
      | Awesome Campaign     | Annual Company Pot      | Ned         | 50     |
    Then "Mike" has "0" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user | budget             | amount |
      | Mike | Annual Company Pot | 150    |

  Scenario: A member cannot contribute on a campaign in progress whose deadline has been reached
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget             | amount |
      | Mike  | Annual Company Pot | 150    |
    And The following campaigns have a deadline reached
      | title            | leader | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign         | budget             | contributor | amount |
      | Awesome Campaign | Annual Company Pot | Mike        | 50     |
    Then "Mike" has "0" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user | budget             | amount |
      | Mike | Annual Company Pot | 150    |

  Scenario: A member cannot contribute on a campaign not in progress
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget             | amount |
      | Mike  | Annual Company Pot | 150    |
    And The following campaigns are running
      | title            | leader | status    | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | C_AVORTED | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign         | budget             | contributor | amount |
      | Awesome Campaign | Annual Company Pot | Mike        | 50     |
    Then "Mike" has "0" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user | budget             | amount |
      | Mike | Annual Company Pot | 150    |

  Scenario: A member cannot contribute on a campaign not associated with the budget referenced
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                             | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot Previous Year | 150             | true          | Sabrina | Terms of Use |
      | Unnamed Company | Annual Company Pot               | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget                           | amount |
      | Mike  | Annual Company Pot Previous Year | 150    |
    And The following campaigns are running
      | title            | leader | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign         | budget                           | contributor | amount |
      | Awesome Campaign | Annual Company Pot Previous Year | Mike        | 50     |
    Then "Mike" has "0" donation on the "Annual Company Pot Previous Year" budget
    And "Mike" has "0" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user | budget                           | amount |
      | Mike | Annual Company Pot Previous Year | 150    |
      | Mike | Annual Company Pot               | 150    |

  Scenario: A member cannot contribute behalf of another member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget             | amount |
      | Mike  | Annual Company Pot | 150    |
    And The following campaigns are running
      | title            | leader | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign         | budget             | contributor | amount |
      | Awesome Campaign | Annual Company Pot | Sabrina     | 50     |
    Then "Mike" has "0" donation on the "Annual Company Pot" budget
    And "Sabrina" has "0" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user    | budget             | amount |
      | Mike    | Annual Company Pot | 150    |
      | Sabrina | Annual Company Pot | 150    |

  Scenario: A member cannot contribute on a campaign if he has not enough budget
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following contents are saved
      | organization    | name         | value     |
      | Unnamed Company | Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name               | amountPerMember | isDistributed | sponsor | rules        |
      | Unnamed Company | Annual Company Pot | 150             | true          | Sabrina | Terms of Use |
    And The following accounts are created
      | owner | budget             | amount |
      | Mike  | Annual Company Pot | 150    |
    And The following campaigns are running
      | title            | leader | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
    And The following campaigns uses budgets
      | campaign         | budget             |
      | Awesome Campaign | Annual Company Pot |
    And "Mike" is logged in
    When "Mike" submit the following donations
      | campaign         | budget             | contributor | amount |
      | Awesome Campaign | Annual Company Pot | Mike        | 50     |
      | Awesome Campaign | Annual Company Pot | Mike        | 150    |
    Then "Mike" has "1" donation on the "Annual Company Pot" budget
    And Following users have corresponding amount left on budgets
      | user | budget             | amount |
      | Mike | Annual Company Pot | 100    |
