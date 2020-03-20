Feature: Donation Management
  This feature verifies the donation creation and deletion

  Scenario: Check that Mike cannot contribute on a non-running campaign
    Given The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname   | email                     | password |
      | Mike        | mike@unnamedcompany.com   | mike     |
      | Steven      | steven@unnamedcompany.com | steven   |
    And The following contents are saved in the organization "Unnamed Company"
      | name         | value     |
      | Terms of Use | Blablabla |
    And The following pots are available in the organization "Unnamed Company"
      | name               | amountPerMember | sponsor | rules        |
      | Annual Company Pot | 150             | Steven  | Terms of Use |
    And "Mike" is logged in
    When "Mike" submit the following donations on a non-existing project
      | budget             | amount |
      | Annual Company Pot | 50     |
    Then "Mike" have "0" donation on the "Annual Company Pot" budget