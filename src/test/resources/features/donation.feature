Feature: Donation Management
  Verifies rules for create and delete donations

  Scenario: A member can contribute on a campaign in progress whose deadline has not been reached
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname   | email                     | password |
      | Mike        | mike@unnamedcompany.com   | mike     |
      | Steven      | steven@unnamedcompany.com | steven   |
    And The following contents are saved in the organization "Unnamed Company"
      | name         | value     |
      | Terms of Use | Blablabla |
    And The following budgets are available in the organization "Unnamed Company"
      | name               | amountPerMember | sponsor | rules        |
      | Annual Company Pot | 150             | Steven  | Terms of Use |
    And The following campaigns are running
      | title           | leader | status        | peopleRequired | donationsRequired |
      | Awesome Project | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to the "Unnamed Company" organization
      | title           |
      | Awesome Project |
    And The following campaigns uses the "Annual Company Pot" budget
      | title           |
      | Awesome Project |
    And "Mike" is logged in
    When "Mike" submit the following donations on the project "Awesome Project"
      | budget             | amount |
      | Annual Company Pot | 50     |
    Then "Mike" have "1" donation on the "Annual Company Pot" budget
    And "Mike" has the following amounts left on corresponding budgets
      | budget             | amount |
      | Annual Company Pot | 100    |

  Scenario: A member cannot contribute on a nonexistent campaign
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname   | email                     | password |
      | Mike        | mike@unnamedcompany.com   | mike     |
      | Steven      | steven@unnamedcompany.com | steven   |
    And The following contents are saved in the organization "Unnamed Company"
      | name         | value     |
      | Terms of Use | Blablabla |
    And The following budgets are available in the organization "Unnamed Company"
      | name               | amountPerMember | sponsor | rules        |
      | Annual Company Pot | 150             | Steven  | Terms of Use |
    And "Mike" is logged in
    When "Mike" submit the following donations on a non-existing project
      | budget             | amount |
      | Annual Company Pot | 50     |
    Then "Mike" have "0" donation on the "Annual Company Pot" budget
    And "Mike" has the following amounts left on corresponding budgets
      | budget             | amount |
      | Annual Company Pot | 150    |

  Scenario: A member cannot contribute on a campaign in progress whose deadline has been reached
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname   | email                     | password |
      | Mike        | mike@unnamedcompany.com   | mike     |
      | Steven      | steven@unnamedcompany.com | steven   |
    And The following contents are saved in the organization "Unnamed Company"
      | name         | value     |
      | Terms of Use | Blablabla |
    And The following budgets are available in the organization "Unnamed Company"
      | name               | amountPerMember | sponsor | rules        |
      | Annual Company Pot | 150             | Steven  | Terms of Use |
    And The following campaigns have a deadline reached
      | title           | leader | status        | peopleRequired | donationsRequired |
      | Awesome Project | Mike   | A_IN_PROGRESS | 2              | 200               |
    And The following campaigns are associated to the "Unnamed Company" organization
      | title           |
      | Awesome Project |
    And The following campaigns uses the "Annual Company Pot" budget
      | title           |
      | Awesome Project |
    And "Mike" is logged in
    When "Mike" submit the following donations on the project "Awesome Project"
      | budget             | amount |
      | Annual Company Pot | 50     |
    Then "Mike" have "0" donation on the "Annual Company Pot" budget
    And "Mike" has the following amounts left on corresponding budgets
      | budget             | amount |
      | Annual Company Pot | 150    |
