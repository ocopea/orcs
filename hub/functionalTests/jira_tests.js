// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
"use strict";

var httpStatusCode = require('/usr/local/lib/node_modules/http-status-codes');
var jiraTestUtils = require('./jira_test_utils');

// Test Cases
exports.testListIntegrationsUnauthorized = function (callback) {
    var testcase = 'testListIntegrationsUnauthorized : '
    jiraTestUtils.testListIntegrationsWrongPassword(testcase, function () {
        console.log(testcase + "Verify list integrations failed due to incorrect password")
        callback();
    });
}

exports.testListDefaultJiraIntegration = function (callback) {
    var testcase = 'testDefaultJiraIntegration : '
    jiraTestUtils.testListJiraIntegration(testcase, function () {
        console.log(testcase + "Verify default jira integration")
        callback();
    });
}

exports.testOverwriteJiraIntegration = function (callback) {
    var testcase = 'testJiraIntegrationOverwrite : '
    jiraTestUtils.testAddJiraIntegration(testcase, jiraTestUtils.url, jiraTestUtils.projectIdOverwrite,
                                         jiraTestUtils.issueTypeIdOverwrite, function () {
        console.log(testcase + "Overwrite jira integration")
        jiraTestUtils.testListJiraIntegrationOverwritten(testcase, function () {
            console.log(testcase + "Verify new jira integration")
            jiraTestUtils.testAddJiraIntegration(testcase, jiraTestUtils.url, jiraTestUtils.projectId,
                                                 jiraTestUtils.issueTypeId, function () {
                console.log(testcase + "Set jira integration back to default")
                callback();
            });
        });
    });
}

exports.setJiraIntegrationToDefaults = function (callback) {
    var testcase = 'cleanupIntegrations : '
    jiraTestUtils.testAddJiraIntegration(testcase, jiraTestUtils.url, jiraTestUtils.projectId,
                                         jiraTestUtils.issueTypeId, function () {
        console.log(testcase + "Set jira integration back to default")
        jiraTestUtils.testListJiraIntegration(testcase, function () {
            console.log(testcase + "Verify default jira integration")
            callback();
        });
    });
}
