// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
"use strict";

var httpStatusCode = require('/usr/local/lib/node_modules/http-status-codes');
var pivotalTestUtils = require('./pivotal_test_utils');

// Test Cases
exports.setPivotalTrackerIntegrationToDefaults = function (callback) {
    var testcase = 'cleanupIntegration : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Set pivotal tracker integration to default")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify pivotal tracker integration")
            callback();
        });
    });
}

exports.testAddPivotalTrackerIntegration = function (callback) {
    var testcase = 'testAddPivotalTrackerIntegration : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Set pivotal tracker integration to default")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify pivotal tracker integration")
            callback();
        });
    });
}

exports.testAddSamePivotalTrackerIntegration = function (callback) {
    var testcase = 'testAddSamePivotalTrackerIntegration : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Add default pivotal tracker integration")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify default pivotal tracker integration")
            pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                              pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
                console.log(testcase + "Writing the same pivotal tracker integration")
                pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
                    console.log(testcase + "Verify pivotal tracker integration")
                    callback();
     
                });
            });
        });
    });
}

exports.testAdd2ndPivotalTrackerIntegration = function (callback) {
    var testcase = 'testAdd2ndPivotalTrackerIntegration : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Add default pivotal tracker integration")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify default pivotal tracker integration")
            pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url2ndAdd, pivotalTestUtils.projectId2ndAdd,
                                                              pivotalTestUtils.issueTypeId2ndAdd, pivotalTestUtils.token2ndAdd, function () {
                console.log(testcase + "Overwrite with different pivotal tracker integration")
                pivotalTestUtils.testList2ndPivotalTrackerIntegration(testcase, function () {
                    console.log(testcase + "Verify 2nd pivotal tracker integration information")
                    callback();

                });
            });
        });
    });
}

exports.testOverwritePivotalTrackerWithTypo = function (callback) {
    var testcase = 'testOverwritePivotalTrackerWithTypo : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify pivotal tracker integration")
            pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectIdOverwrite,
                                                              pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
                console.log(testcase + "Overwriting pivotal tracker integration")
                pivotalTestUtils.testListPivotalTrackerIntegrationOverwriteTypo(testcase, function () {
                    console.log(testcase + "Verify pivotal tracker overwrite")
                    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
                        console.log(testcase + "Resetting pivotal tracker integration back to default")
                        callback();
                    });
                });
            });
        });
    });
}

exports.testAddPivotalIntegrationNoToken = function (callback) {
    var testcase = 'testAddPivotalTrackerIntegrationNoToken : '
    pivotalTestUtils.testAddPivotalTrackerIntegrationMissingToken(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, function () {
        console.log(testcase + "Added pivotal tracker integration with missing token")
        callback();
    });
}

exports.testAddPivotalIntegrationNoUrl = function (callback) {
    var testcase = 'testAddPivotalTrackerIntegrationNoUrl : '
    pivotalTestUtils.testAddPivotalTrackerIntegrationMissingUrl(testcase, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration with missing url")
        callback();
    });
}

exports.testAddPivotalIntegrationNoProjectId = function (callback) {
    var testcase = 'testAddPivotalTrackerIntegrationNoProjectId : '
    pivotalTestUtils.testAddPivotalTrackerIntegrationMissingProjectId(testcase, pivotalTestUtils.url,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration with missing projectId")
        callback();
    });
}

exports.testAddPivotalIntegrationNoIssueTypeId = function (callback) {
    var testcase = 'testAddPivotalTrackerIntegrationNoIssueTypeId : '
    pivotalTestUtils.testAddPivotalTrackerIntegrationMissingIssueTypeId(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration with missing issueTypeId")
        callback();
    });
}

exports.testAddBug = function (callback) {
    var testcase = 'testAddBug : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify pivotal tracker integration")
            pivotalTestUtils.testAddBug(testcase, pivotalTestUtils.name, pivotalTestUtils.description, function () {
                console.log(testcase + "Add and verify response from new bug")
                callback();
            });
        });
    });
}

exports.testAddBugNoDescription = function (callback) {
    var testcase = 'testAddBugMissingDescription : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify pivotal tracker integration")
            pivotalTestUtils.testAddBugMissingDescription(testcase, pivotalTestUtils.name, function () {
                console.log(testcase + "Add bug failed because missing description field")
                callback();
            });
        });
    });
}

exports.testAddBugNoName = function (callback) {
    var testcase = 'testAddBugMissingName : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
        console.log(testcase + "Added pivotal tracker integration")
        pivotalTestUtils.testListPivotalTrackerIntegration(testcase, function () {
            console.log(testcase + "Verify pivotal tracker integration")
            pivotalTestUtils.testAddBugMissingName(testcase, pivotalTestUtils.description, function () {
                console.log(testcase + "Add bug failed because missing name field")
                callback();
            });
        });
    });
}

exports.testAddBugWithBadToken = function (callback) {
    var testcase = 'testAddBugBadToken : '
    pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                      pivotalTestUtils.issueTypeId, "123456789", function () {
        console.log(testcase + "Added pivotal tracker integration with bad token")
        pivotalTestUtils.testPivotalTrackerIntegrationWithBadToken(testcase, function () {
            console.log(testcase + "Verify bad token in integration")
            pivotalTestUtils.testAddBugBadToken(testcase, pivotalTestUtils.name, pivotalTestUtils.description, function () {
                console.log(testcase + "Add Bug failed")
                pivotalTestUtils.testAddPivotalTrackerIntegration(testcase, pivotalTestUtils.url, pivotalTestUtils.projectId,
                                                              pivotalTestUtils.issueTypeId, pivotalTestUtils.token, function () {
                    console.log(testcase + "Replace pivotal tracker integration with good token")
                    callback();
                });
            });
        });
    });
}
