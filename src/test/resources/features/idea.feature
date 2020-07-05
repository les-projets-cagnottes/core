Feature: Budget
  Verifies rules for using budgets

  Scenario: A member can create an idea
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                   | password |
      | Mike      | mike@unnamedcompany.com | mike     |
    And The following users are members of organizations
      | user | organization    |
      | Mike | Unnamed Company |
    And "Mike" is logged in
    When "Mike" submit the following ideas
      | organization    | shortDescription | longDescription | tags |
      | Unnamed Company | Blabla           | Blablabla       |      |
    Then Last HTTP code was "201"