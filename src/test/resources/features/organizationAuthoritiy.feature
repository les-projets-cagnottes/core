Feature: Organization Authorities
  Verifies rules for granting organization authorities

  Scenario: An organization owner can grant a member with an organization authority
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
      | firstname  | organization    | authority  |
      | Mike       | Unnamed Company | ROLE_OWNER |
    And "Mike" is logged in
    When "Mike" grants following users with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
      | Martin    | Unnamed Company | ROLE_MANAGER |
      | Olivia    | Unnamed Company | ROLE_OWNER   |
    Then Verify that following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
      | Martin    | Unnamed Company | ROLE_MANAGER |
      | Olivia    | Unnamed Company | ROLE_OWNER   |