// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
"use strict";

var jiraTests = require('./jira_tests');
var jiraTestUtils = require('./jira_test_utils');
var pivotalTests = require('./pivotal_tests');
var pivotalTestUtils = require('./pivotal_test_utils');

var hubString = "-orcs.cf.isus.emc.com/hub-web-api";
var username = process.env.USER;
var hubIP = username + hubString;

// Default pivotal tracker integration values
pivotalTestUtils.url = "https://www.pivotaltracker.com/services/v5";
pivotalTestUtils.projectId = "2016043";
pivotalTestUtils.issueTypeId = "bug";
pivotalTestUtils.token = "aa227be6b98dda557113593c3b8f69e4";
pivotalTestUtils.name = "AutoTest Bug addition";
pivotalTestUtils.description = "New bug from Frisby test";

// Typo value to verify it overwrites good value
pivotalTestUtils.projectIdOverwrite = "5016043";

// Alternate  pivotal tracker integration values
pivotalTestUtils.url2ndAdd = "https://www.pivotaltracker.com/services/v5"
pivotalTestUtils.projectId2ndAdd = "2016057";
pivotalTestUtils.issueTypeId2ndAdd = "bug";
pivotalTestUtils.token2ndAdd = "aa227be6b98dda557113593c3b8f69e4"

// Default jira integration values
jiraTestUtils.url = "https://jira.cec.lab.emc.com:8443";
jiraTestUtils.projectId = "10305";
jiraTestUtils.issueTypeId = "10004";

// Typo values to verify they overwrite good values
jiraTestUtils.projectIdOverwrite = "11111";
jiraTestUtils.issueTypeIdOverwrite = "10001";

// Setting hub-server IP
pivotalTestUtils.setPivotalUrls(hubIP);
jiraTestUtils.setJiraUrls(hubIP);

// define pivotal tests
var tests = [
jiraTests.testListIntegrationsUnauthorized,
jiraTests.testListDefaultJiraIntegration,
jiraTests.testOverwriteJiraIntegration,
pivotalTests.testAddPivotalTrackerIntegration,
pivotalTests.testAddSamePivotalTrackerIntegration,
pivotalTests.testAdd2ndPivotalTrackerIntegration,
pivotalTests.testOverwritePivotalTrackerWithTypo,
pivotalTests.testAddPivotalIntegrationNoToken,
pivotalTests.testAddPivotalIntegrationNoUrl,
pivotalTests.testAddPivotalIntegrationNoProjectId,
pivotalTests.testAddPivotalIntegrationNoIssueTypeId,
pivotalTests.testAddBugNoDescription,
pivotalTests.testAddBugNoName,
pivotalTests.testAddBugWithBadToken,
pivotalTests.testAddBug
];

function run() {
    // Run the first test from the tests array
    if (tests.length > 0) {
        tests[0](function () {            
            doNext();
        });
    }
    else {
        console.log("Reset integrations to default values.")
        jiraTests.setJiraIntegrationToDefaults;
        pivotalTests.setPivotalIntegrationToDefaults;
        console.log("Done with tests.")
    }
}

function doNext() {
    if (tests.length > 0) {
        tests.shift();
        run();
    }
}

run();

