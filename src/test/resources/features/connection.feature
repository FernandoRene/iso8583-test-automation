@Connection
Feature: Simulator Connection Management
  Como tester de ISO8583
  Quiero poder gestionar la conexión con el simulador
  Para poder ejecutar pruebas de transacciones

  Background:
    Given the simulator is running

  @Smoke @ConnectionBasics
  Scenario: Connect to simulator successfully
    Given I am not connected to the simulator
    When I connect to the simulator
    Then the connection should be established
    And the connection status should be "connected"

  @Smoke @ConnectionBasics
  Scenario: Disconnect from simulator successfully
    Given I am connected to the simulator
    When I disconnect from the simulator
    Then the connection should be closed
    And the connection status should be "disconnected"

  @ConnectionTest
  Scenario: Test connection with network test message
    Given I am connected to the simulator
    When I test the connection
    Then the connection test should be successful

  @ConnectionStatus
  Scenario: Get connection status
    Given I am connected to the simulator
    When I get the connection status
    Then the response status code should be 200
    And the connection status should be "connected"

  @ConnectionManagement
  Scenario: Clear response buffer
    Given I am connected to the simulator
    When I clear the response buffer
    Then the response buffer should be cleared

  @KeepAlive
  Scenario: Configure keep-alive
    Given I am connected to the simulator
    When I configure keep-alive with 15 minutes interval
    Then the keep-alive should be configured

  @KeepAlive
  Scenario: Disable keep-alive
    Given I am connected to the simulator
    And I configure keep-alive with 10 minutes interval
    When I disable keep-alive
    Then the keep-alive should be disabled

  @ConnectionValidation
  Scenario: Verify connection details
    Given I am connected to the simulator
    When I get the connection status
    Then the connection response should contain:
      | host      | 172.16.1.211 |
      | port      | 5105         |
      | connected | true         |

  @ErrorHandling
  Scenario: Handle connection errors gracefully
    Given I am not connected to the simulator
    When I test the connection
    Then the response status code should be 400
    And the response should contain error message

  @Reconnection @DisconnectAfter
  Scenario: Reconnect after disconnection
    Given I am connected to the simulator
    When I disconnect from the simulator
    Then the connection should be closed
    When I connect to the simulator
    Then the connection should be established

  @Integration @RequiresConnection
  Scenario: Connection persists across multiple operations
    Given I am connected to the simulator
    When I get the connection status
    Then the connection status should be "connected"
    When I test the connection
    Then the connection test should be successful
    When I get the connection status
    Then the connection status should be "connected"

  @ConnectionPool
  Scenario Outline: Test connection with different configurations
    Given I am not connected to the simulator
    When I configure keep-alive with <interval> minutes interval
    And I connect to the simulator
    Then the connection should be established
    And the keep-alive should be configured

    Examples:
      | interval |
      | 5        |
      | 10       |
      | 15       |
      | 30       |

  @ConnectionLifecycle
  Scenario: Complete connection lifecycle
    # Connect
    Given I am not connected to the simulator
    When I connect to the simulator
    Then the connection should be established
    
    # Configure
    When I configure keep-alive with 15 minutes interval
    Then the keep-alive should be configured
    
    # Test
    When I test the connection
    Then the connection test should be successful
    
    # Manage
    When I clear the response buffer
    Then the response buffer should be cleared
    
    # Disconnect
    When I disable keep-alive
    And I disconnect from the simulator
    Then the connection should be closed

  @Robustness
  Scenario: Multiple connection attempts
    Given I am not connected to the simulator
    When I connect to the simulator
    Then the connection should be established
    # Second connect should be idempotent or handle gracefully
    When I connect to the simulator
    Then the connection should be established

  @Robustness
  Scenario: Multiple disconnection attempts
    Given I am connected to the simulator
    When I disconnect from the simulator
    Then the connection should be closed
    # Second disconnect should be idempotent
    When I disconnect from the simulator
    Then the response status code should be 200
