Feature: Main
  Verifies transverse features of the app

  Scenario: A visitor can check the health of the app
    When Anyone checks the health of the app
    Then Last HTTP code was "200"