Feature: Organization Authorities
  Verifies rules for granting organization authorities

  Scenario: An organization owner can grant a member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
      | Martin    | margin@unnamedcompany.com  | margin   |
      | Olivia    | olivia@unnamedcompany.com  | olivia   |
    And The following users are granted with organization authorities
      | firstname | organization    | authority  |
      | Mike      | Unnamed Company | ROLE_OWNER |
    And "Mike" is logged in
    When "Mike" grants following users with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
      | Martin    | Unnamed Company | ROLE_MANAGER |
      | Olivia    | Unnamed Company | ROLE_OWNER   |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization    | authorities  |
      | Sabrina   | Unnamed Company | 1            |
      | Martin    | Unnamed Company | 1            |
      | Olivia    | Unnamed Company | 1            |
    And Verify that following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
      | Martin    | Unnamed Company | ROLE_MANAGER |
      | Olivia    | Unnamed Company | ROLE_OWNER   |

  Scenario: An organization owner can withdraw rights to a member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
      | Martin    | margin@unnamedcompany.com  | margin   |
      | Olivia    | olivia@unnamedcompany.com  | olivia   |
    And The following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Mike      | Unnamed Company | ROLE_OWNER   |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
      | Martin    | Unnamed Company | ROLE_MANAGER |
      | Olivia    | Unnamed Company | ROLE_OWNER   |
    And "Mike" is logged in
    When "Mike" withdraw organization authorities to following users
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization    | authorities  |
      | Sabrina   | Unnamed Company | 0            |
      | Martin    | Unnamed Company | 1            |
      | Olivia    | Unnamed Company | 1            |
    And Verify that following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Martin    | Unnamed Company | ROLE_MANAGER |
      | Olivia    | Unnamed Company | ROLE_OWNER   |

  Scenario: A member not owner of an organization cannot grant a member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
      | Martin    | margin@unnamedcompany.com  | margin   |
      | Olivia    | olivia@unnamedcompany.com  | olivia   |
    And "Mike" is logged in
    When "Mike" grants following users with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization    | authorities  |
      | Sabrina   | Unnamed Company | 0            |