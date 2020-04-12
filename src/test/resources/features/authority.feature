Feature: Authority
  Verifies rules for using authorities

  Scenario: A member can list his global authorities
    Given Empty database
    And The following users are registered
      | firstname | email                    | password |
      | Mike      | mike@unnamedcompany.com  | mike     |
      | Alice     | alice@unnamedcompany.com | alice    |
    And The following users are granted with authorities
      | firstname | authority  |
      | Alice     | ROLE_ADMIN |
    And "Mike" is logged in
    When "Mike" get his authorities
    Then It returns following authorities
      | authority |
      | ROLE_USER |