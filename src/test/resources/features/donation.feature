Feature: Donation Management
  This feature verifies the donation creation and deletion

  Scenario: Check that a member can contribute on a running and not completed campaign
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

  Scenario: Check that a member cannot contribute on a nonexistent campaign
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