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
      | organization    | shortDescription | longDescription | hasAnonymousCreator | hasLeaderCreator | tags |
      | Unnamed Company | Blabla           | Blablabla       | false               | false            |      |
    Then Last HTTP code was "201"

  Scenario: A member can anonymously create an idea
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
      | organization    | shortDescription | longDescription | hasAnonymousCreator | hasLeaderCreator | tags |
      | Unnamed Company | Blabla           | Blablabla       | true                | false            |      |
    Then Last HTTP code was "201"
    And Last response is anonymized

  Scenario: A member can list organization ideas
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
    And The following ideas are submitted
      | organization    | shortDescription | longDescription | hasAnonymousCreator | hasLeaderCreator | tags |
      | Unnamed Company | Blabla           | Blablabla       | false               | false            |      |
    And "Mike" is logged in
    When "Mike" gets ideas of the "Unnamed Company" organization
    Then Last HTTP code was "200"
    And It returns following ideas
      | organization    | shortDescription | longDescription | hasAnonymousCreator | hasLeaderCreator | tags |
      | Unnamed Company | Blabla           | Blablabla       | false               | false            |      |