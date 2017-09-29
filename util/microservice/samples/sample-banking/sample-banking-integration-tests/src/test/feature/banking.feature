Feature: Sample Bank Integration Test

Basic integration tests for the sample bank

	Scenario: Single deposit into new account
		Given a bank account called "My Secret Switzerland account" with balance of 0
		When 1000 is deposited
		Then bank balance is 1000
		
	Scenario: Multiple discount into new account
		Given a bank account called "Zurich Account" with balance of 0
		When 10 is deposited
		And 10 is deposited
		And 10 is deposited
		And 10 is deposited
		Then bank balance is 40
		