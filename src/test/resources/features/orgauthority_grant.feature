Feature: Organization Authorities - Grant
  Verifies rules for granting organization authorities

  Scenario: An admin can grant an organization member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email            | password |
      | Abby      | abby@example.com | abby     |
    And The following users are granted with authorities
      | firstname | authority  |
      | Abby      | ROLE_ADMIN |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And "Abby" is logged in
    When "Abby" grants following users with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization    | authorities |
      | Sabrina   | Unnamed Company | 1           |
    And Verify that following users are granted with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |

  Scenario: An organization owner can grant a member
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                      | password |
      | Mike      | mike@unnamedcompany.com    | mike     |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
      | Martin    | martin@unnamedcompany.com  | martin   |
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
      | firstname | organization    | authorities |
      | Sabrina   | Unnamed Company | 1           |
      | Martin    | Unnamed Company | 1           |
      | Olivia    | Unnamed Company | 1           |
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
      | Martin    | martin@unnamedcompany.com  | martin   |
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
      | firstname | organization    | authorities |
      | Sabrina   | Unnamed Company | 0           |
      | Martin    | Unnamed Company | 1           |
      | Olivia    | Unnamed Company | 1           |
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
      | Martin    | martin@unnamedcompany.com  | martin   |
      | Olivia    | olivia@unnamedcompany.com  | olivia   |
    And "Mike" is logged in
    When "Mike" grants following users with organization authorities
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization    | authorities |
      | Sabrina   | Unnamed Company | 0           |

  Scenario: An organization owner cannot grant if organization or member is not existing
    Given Empty database
    And The following users are not registered
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following organizations are not registered
      | name                |
      | Nonexistent Company |
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                     | password |
      | Mike      | mike@unnamedcompany.com   | mike     |
      | Martin    | martin@unnamedcompany.com | martin   |
    And The following users are granted with organization authorities
      | firstname | organization    | authority  |
      | Mike      | Unnamed Company | ROLE_OWNER |
    And "Mike" is logged in
    When "Mike" withdraw organization authorities to following users
      | firstname | organization        | authority    |
      | Sabrina   | Unnamed Company     | ROLE_SPONSOR |
      | Martin    | Nonexistent Company | ROLE_MANAGER |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization        | authorities |
      | Sabrina   | Unnamed Company     | 0           |
      | Martin    | Nonexistent Company | 0           |

  Scenario: An organization owner cannot grant a user not member of organization
    Given Empty database
    And The following organizations are registered
      | name            |
      | Unnamed Company |
    And The following users are registered
      | firstname | email                      | password |
      | Sabrina   | sabrina@unnamedcompany.com | sabrina  |
    And The following users are members of organization "Unnamed Company"
      | firstname | email                   | password |
      | Mike      | mike@unnamedcompany.com | mike     |
    And The following users are granted with organization authorities
      | firstname | organization    | authority  |
      | Mike      | Unnamed Company | ROLE_OWNER |
    And "Mike" is logged in
    When "Mike" withdraw organization authorities to following users
      | firstname | organization    | authority    |
      | Sabrina   | Unnamed Company | ROLE_SPONSOR |
    Then Verify that following users have the correct number of organization authorities
      | firstname | organization    | authorities |
      | Sabrina   | Unnamed Company | 0           |
