Feature: Campaign - Get Donations
  Verifies rules for getting donations on a campaign

  Scenario: A member of a campaign's organization can get campaign's donations
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
      | Another Company |
    And The following users are registered
      | firstname | email                       | password |
      | Mike      | mike@unnamedcompany.com     | mike     |
      | Sabrina   | sabrina@unnamedcompany.com  | sabrina  |
      | Sinclair  | sinclair@anothercompany.com | sinclair |
    And The following users are members of organizations
      | user     | organization    |
      | Mike     | Unnamed Company |
      | Sabrina  | Unnamed Company |
      | Sinclair | Another Company |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
      | Another Company | Another Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                | amountPerMember | isDistributed | sponsor  | rules                |
      | Unnamed Company | Unnamed Company Pot | 150             | true          | Sabrina  | Unnamed Terms of Use |
      | Another Company | Another Company Pot | 200             | true          | Sinclair | Another Terms of Use |
    And The following accounts are created
      | owner | budget              | amount |
      | Mike  | Unnamed Company Pot | 150    |
      | Mike  | Another Company Pot | 200    |
    And The following campaigns are running
      | title            | leader  | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Sabrina | A_IN_PROGRESS | 2              | 400               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
      | Awesome Campaign | Another Company |
    And The following campaigns uses budgets
      | campaign         | budget              |
      | Awesome Campaign | Unnamed Company Pot |
      | Awesome Campaign | Another Company Pot |
    And The following donations are made
      | budget              | campaign         | contributor | amount |
      | Unnamed Company Pot | Awesome Campaign | Mike        | 100    |
      | Unnamed Company Pot | Awesome Campaign | Sabrina     | 75     |
      | Another Company Pot | Awesome Campaign | Sinclair    | 100    |
    And "Mike" is logged in
    When "Mike" gets donations of the "Awesome Campaign" campaign
    Then Last HTTP code was "200"
    And It returns following donations
      | budget              | campaign         | contributor | amount |
      | Unnamed Company Pot | Awesome Campaign | Mike        | 100    |
      | Unnamed Company Pot | Awesome Campaign | Sabrina     | 75     |
      | Another Company Pot | Awesome Campaign | Sinclair    | 100    |

  Scenario: A non-member of any campaign's organizations cannot get campaign's donations
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
      | Another Company |
    And The following users are registered
      | firstname | email                       | password |
      | Nicolas   | nicolas@anonymous.com       | nicolas  |
      | Mike      | mike@unnamedcompany.com     | mike     |
      | Sabrina   | sabrina@unnamedcompany.com  | sabrina  |
      | Sinclair  | sinclair@anothercompany.com | sinclair |
    And The following users are members of organizations
      | user     | organization    |
      | Mike     | Unnamed Company |
      | Sabrina  | Unnamed Company |
      | Sinclair | Another Company |
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
      | Another Company | Another Terms of Use | Blablabla |
    And The following budgets are available
      | organization    | name                | amountPerMember | isDistributed | sponsor  | rules                |
      | Unnamed Company | Unnamed Company Pot | 150             | true          | Sabrina  | Unnamed Terms of Use |
      | Another Company | Another Company Pot | 200             | true          | Sinclair | Another Terms of Use |
    And The following accounts are created
      | owner | budget              | amount |
      | Mike  | Unnamed Company Pot | 150    |
      | Mike  | Another Company Pot | 200    |
    And The following campaigns are running
      | title            | leader  | status        | peopleRequired | donationsRequired |
      | Awesome Campaign | Sabrina | A_IN_PROGRESS | 2              | 400               |
    And The following campaigns are associated to organizations
      | campaign         | organization    |
      | Awesome Campaign | Unnamed Company |
      | Awesome Campaign | Another Company |
    And The following campaigns uses budgets
      | campaign         | budget              |
      | Awesome Campaign | Unnamed Company Pot |
      | Awesome Campaign | Another Company Pot |
    And The following donations are made
      | budget              | campaign         | contributor | amount |
      | Unnamed Company Pot | Awesome Campaign | Mike        | 100    |
      | Unnamed Company Pot | Awesome Campaign | Sabrina     | 75     |
      | Another Company Pot | Awesome Campaign | Sinclair    | 100    |
    And "Nicolas" is logged in
    When "Nicolas" gets donations of the "Awesome Campaign" campaign
    Then Last HTTP code was "403"