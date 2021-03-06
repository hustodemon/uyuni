# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ubuntu2004_minion
Feature: Adding the Ubuntu 20.04 distribution custom repositories

  Scenario: Add a child channel for Ubuntu Focal main repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for ubuntu-focal-main" as "Channel Name"
    And I enter "ubuntu-focal-main" as "Channel Label"
    And I select the parent channel for the "ubuntu2004_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for ubuntu-focal-main created" text

  Scenario: Add a child channel for Ubuntu Focal main updates repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    When I enter "Custom Channel for ubuntu-focal-main-updates" as "Channel Name"
    And I enter "ubuntu-focal-main-updates" as "Channel Label"
    And I select the parent channel for the "ubuntu2004_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for ubuntu-focal-main-updates created" text

  Scenario: Add the Ubuntu Focal main repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "ubuntu-focal-main" as "label"
    And I enter "http://archive.ubuntu.com/ubuntu/dists/focal/main/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Add the Ubuntu Focal main updates repositories
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "ubuntu-focal-main-updates" as "label"
    And I enter "http://archive.ubuntu.com/ubuntu/dists/focal-updates/main/binary-amd64/" as "url"
    And I select "deb" from "contenttype"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Add the repository to the custom channel for ubuntu-focal-main
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-focal-main"
    And I follow "Repositories" in the content area
    And I select the "ubuntu-focal-main" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Add the repository to the custom channel for ubuntu-focal-main-updates
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-focal-main-updates"
    And I follow "Repositories" in the content area
    And I select the "ubuntu-focal-main-updates" repo
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repository in the custom channel for ubuntu-focal-main
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-focal-main"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text

  Scenario: Synchronize the repository in the custom channel for ubuntu-focal-main-updates
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Custom Channel for ubuntu-focal-main-updates"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text
