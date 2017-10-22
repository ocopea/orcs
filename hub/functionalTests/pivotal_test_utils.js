// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
"use strict";
var frisby = require('/usr/local/lib/node_modules/frisby');
var fs = require('fs');
var httpStatusCode = require('/usr/local/lib/node_modules/http-status-codes');
var formData = require('/usr/local/lib/node_modules/form-data');
var path = require('/usr/local/lib/node_modules/path');

// Pivotal tracker variables
exports.url;
exports.projectId;
exports.issueTypeId;
exports.token;
exports.name;
exports.description;

// Used to test typo when adding pivotal tracker integration
exports.projectIdOverwriteTypo;

exports.url2ndAdd;
exports.projectId2ndAdd;
exports.issueTypeId2ndAdd;
exports.token2ndAdd;

// Setup HUB urls
var huburls = { baseUrl: "", baseUrlWrongPassword: "", listIntegrationUrl: "", addPivotalTrackerInt: "",
                addBugUrl: "" }

exports.setPivotalUrls = function (hubIP) {
    huburls.baseUrl = 'http://admin:nazgul@' + hubIP;
    huburls.baseUrlWrongPassword = 'http://admin:frodo@' + hubIP;
    huburls.listIntegrationUrl = huburls.baseUrl + '/share-image-integrations';
    huburls.addPivotalTrackerInt = huburls.baseUrl + '/commands/add-pivotal-tracker-integration' ;
    huburls.addBugUrl = huburls.baseUrl + '/test-dev/pivotal-tracker-add';
}

// set Pivotal Tracker defaults
var ptUrlDefault = "https://www.pivotaltracker.com/services/v5";
var ptIssuetypeDefault = "bug";
var ptProjectIdDefault = "2016043";
var ptTokenDefault = "aa227be6b98dda557113593c3b8f69e4";
var ptIntegrationNameDefault = "pivotal-tracker";
var ptProjectId2ndAdd = "2016057";

var ptExternalGetStoryUrl = ptUrlDefault + "/projects/" + ptProjectIdDefault + "/stories/";
var ptProjectIdCompare = 2016043;

// Set frisby defaults
frisby.globalSetup({
    timeout : 10000
})

exports.testListPivotalTrackerIntegration = function (testcase, afterFunction) {
    frisby.create(testcase + "List PivotalTracker Integration")
        .get(huburls.listIntegrationUrl)
        .expectStatus(httpStatusCode.OK)
        .expectJSON('1',{
                   "connectionDetails": {
                     "issueTypeId": ptIssuetypeDefault,
                     "projectId": ptProjectIdDefault,
                     "url": ptUrlDefault,
                     "token": ptTokenDefault
                   },
                   "imageUrl": null,
                   "integrationName": ptIntegrationNameDefault
        })
        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testListPivotalTrackerIntegrationOverwriteTypo = function (testcase, afterFunction) {
    frisby.create(testcase + "List Pivotal Tracker Integration")
        .get(huburls.listIntegrationUrl)
        .expectStatus(httpStatusCode.OK)
        .expectJSON('1',{
                   "connectionDetails": {
                     "issueTypeId": ptIssuetypeDefault,
                     "projectId": "5016043",
                     "url": ptUrlDefault,
                     "token": ptTokenDefault
                   },
                   "imageUrl": null,
                   "integrationName": ptIntegrationNameDefault
        })
        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testList2ndPivotalTrackerIntegration = function (testcase, afterFunction) {
    frisby.create(testcase + "List 2nd Pivotal Tracker Integration Add")
        .get(huburls.listIntegrationUrl)
        .expectStatus(httpStatusCode.OK)
        .expectJSON('1',{
                   "connectionDetails": {
                     "issueTypeId": ptIssuetypeDefault,
                     "projectId": ptProjectId2ndAdd,
                     "url": ptUrlDefault, 
                     "token": ptTokenDefault
                   },
                   "imageUrl": null,
                   "integrationName": ptIntegrationNameDefault
        })
        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testPivotalTrackerIntegrationWithBadToken = function (testcase, afterFunction) {
    frisby.create(testcase + "List PivotalTracker Integration")
        .get(huburls.listIntegrationUrl)
        .expectStatus(httpStatusCode.OK)
        .expectJSON('1',{
                   "connectionDetails": {
                     "issueTypeId":ptIssuetypeDefault,
                     "projectId": ptProjectIdDefault,
                     "url": ptUrlDefault,
                     "token": "123456789"
                   },
                   "imageUrl": null,
                   "integrationName": ptIntegrationNameDefault
        })
        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddPivotalTrackerIntegration = function (testcase, url, projectId, issueTypeId, token, afterFunction) {
    frisby.create(testcase + 'Add Pivotal Tracker Integration')
        .post(huburls.addPivotalTrackerInt,
        { 'url':url, 'projectId':projectId, 'issueTypeId':issueTypeId, 'token': token },
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.NO_CONTENT)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAdd2ndPivotalTrackerIntegration = function (testcase, url, projectId, issueTypeId, token, afterFunction) {
    frisby.create(testcase + 'Add 2nd Pivotal Tracker Integration')
        .post(huburls.addPivotalTrackerInt,
        { 'url':url, 'projectId': projectId, 'issueTypeId': issueTypeId, 'token': token },
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(204)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddPivotalTrackerIntegrationMissingToken = function (testcase, url, projectId, issueTypeId, afterFunction) {
    frisby.create(testcase + 'Add Pivotal Tracker Integration missing token')
        .post(huburls.addPivotalTrackerInt,
        { 'url':url, 'projectId':projectId, 'issueTypeId':issueTypeId},
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.BAD_REQUEST)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddPivotalTrackerIntegrationMissingUrl = function (testcase, projectId, issueTypeId, token, afterFunction) {
    frisby.create(testcase + 'Add Pivotal Tracker Integration missing url')
        .post(huburls.addPivotalTrackerInt,
        { 'projectId':projectId, 'issueTypeId':issueTypeId, 'token':token},
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.BAD_REQUEST)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddPivotalTrackerIntegrationMissingProjectId = function (testcase, url, issueTypeId, token, afterFunction) {
    frisby.create(testcase + 'Add Pivotal Tracker Integration missing projectId')
        .post(huburls.addPivotalTrackerInt,
        { 'url':url, 'issueTypeId':issueTypeId, 'token':token},
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.BAD_REQUEST)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddPivotalTrackerIntegrationMissingIssueTypeId = function (testcase, url, projectId, token, afterFunction) {
    frisby.create(testcase + 'Add Pivotal Tracker Integration missing issueTypeId')
        .post(huburls.addPivotalTrackerInt,
        { 'url':url, 'projectId':projectId, 'token':token},
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})

        .expectStatus(httpStatusCode.BAD_REQUEST)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddBug = function (testcase, name, description, afterFunction) {
    frisby.create(testcase + 'Add New Bug')
        .post(huburls.addBugUrl,
        {'name':name, 'description':description },
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})

        .expectStatus(httpStatusCode.OK)

        .after(function (err, res, body) {
            console.log("Bug report url = " + body)
            frisby.create(testcase + 'Verify Bug with rsp from addBug')
                .get(ptExternalGetStoryUrl,
                { headers: {
                    'Content-Type': 'application/json',
                    'X-TrackerToken': ptTokenDefault }})

                .expectStatus(httpStatusCode.OK)
                .expectJSON('?', {
                           'project_id': ptProjectIdCompare,
                           'url': body,
                           'story_type': 'bug'
                })
                .toss();
        })
        .toss();
}

exports.testAddBugMissingDescription = function (testcase, name, afterFunction) {
    frisby.create(testcase + 'Add Bug Missing Description field')
        .post(huburls.addBugUrl,
        {'name':name },
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.BAD_REQUEST)

        .after(function () {
            afterFunction();
        })
        .toss();
}

exports.testAddBugMissingName = function (testcase, description, afterFunction) {
    frisby.create(testcase + 'Add Bug Missing Name field')
        .post(huburls.addBugUrl,
        {'description':description},
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.BAD_REQUEST)

        .after(function () {
            afterFunction();
        })
        .toss();
}

// Need to add wrong token in integration before calling this
exports.testAddBugBadToken = function (testcase, name, description, afterFunction) {
    frisby.create(testcase + 'Add Bug with Bad Token')
        .post(huburls.addBugUrl,
        { 'name':name, 'description':description },
        { json: true },
        { headers: { 'Content-Type': 'application/json' }})
        .expectStatus(httpStatusCode.FORBIDDEN)

        .after(function () {
            afterFunction();
        })
        .toss();
}
