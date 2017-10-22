// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
"use strict";
var frisby = require('/usr/local/lib/node_modules/frisby');
var fs = require('fs');
var httpStatusCode = require('/usr/local/lib/node_modules/http-status-codes');
var formData = require('/usr/local/lib/node_modules/form-data');
var path = require('/usr/local/lib/node_modules/path');

// HUB Repository information
exports.url;
exports.projectId;
exports.issueTypeId;

exports.projectIdOverwrite;
exports.issueTypeIdOverwrite;

// Setup Jira urls
var huburls = { baseUrl: "", baseUrlWrongPassword: "",  listIntegrationUrl: "", jiraAddIntegrationUrl: "" }

exports.setJiraUrls = function (hubIP) {
    huburls.baseUrl = 'http://admin:nazgul@' + hubIP;
    huburls.baseUrlWrongPassword = 'http://frodo:ashnazg@' + hubIP;
    huburls.listIntegrationUrl = huburls.baseUrl + '/share-image-integrations';
    huburls.jiraAddIntegrationUrl = huburls.baseUrl + '/commands/add-jira-integration' ;
}

var jiraIssueTypeIdDefault = "10004";
var jiraProjectIdDefault = "10305";
var jiraUrlDefault = "https://jira.cec.lab.emc.com:8443";
var jiraIntegrationNameDefault = "jira";

// Set frisby defaults
frisby.globalSetup({
    timeout : 10000
})

// HUB Error Messages
exports.NO_REPO_CONNECTION = "Connection to the repository couldn't be established";

//var testFilePath = path.resolve(__dirname, 'testdata')

// Test functions for each API
exports.testListJiraIntegration = function (testcase, afterFunction) {
    frisby.create(testcase + "List Jira Integration")
        .get(huburls.listIntegrationUrl)
        .expectStatus(httpStatusCode.OK)
        .expectJSON('0',{
                   "connectionDetails": {
                     "issueTypeId": jiraIssueTypeIdDefault,
                     "projectId": jiraProjectIdDefault,
                     "url": jiraUrlDefault 
                   },
                   "imageUrl": null,
                   "integrationName": jiraIntegrationNameDefault 
        })
        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testListJiraIntegrationOverwritten = function (testcase, afterFunction) {
    frisby.create(testcase + "List Jira Integration Overwritten")
        .get(huburls.listIntegrationUrl)
        .expectStatus(httpStatusCode.OK)
        .expectJSON('0',{
                   "connectionDetails": {
                     "issueTypeId": "10001",
                     "projectId": "11111",
                     "url": jiraUrlDefault 
                   },
                   "imageUrl": null,
                   "integrationName": jiraIntegrationNameDefault 
        })
        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testListIntegrationsWrongPassword = function (testcase, afterFunction) {
    frisby.create(testcase + "List Integrations failed")
        .get(huburls.baseUrlWrongPassword)
        .expectStatus(httpStatusCode.UNAUTHORIZED)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddJiraIntegration = function (testcase, url, projectId, issueTypeId, afterFunction) {
    frisby.create(testcase + 'Add Jira Integration')
        .post(huburls.jiraAddIntegrationUrl,
        { 'url':url, 'projectId':projectId, 'issueTypeId':issueTypeId },
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.NO_CONTENT)

        .after(function () {
            afterFunction();
        })
        .toss();
}

