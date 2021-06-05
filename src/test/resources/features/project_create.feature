Feature: Projects management
  Verifies rules for managing projects

  Scenario: A member can create a project
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
    And The following contents are saved
      | organization    | name                 | value     |
      | Unnamed Company | Unnamed Terms of Use | Blablabla |
    And "Mike" is logged in
    When "Mike" creates the following projects
      | organization    | title      | shortDescription | longDescription | peopleRequired |
      | Unnamed Company | Project #1 | Blabla           | Blablabla       | 3              |
    Then Last HTTP code was "201"

