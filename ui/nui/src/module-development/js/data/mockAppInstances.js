// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import moment from 'moment';
var today = moment();
var oneMonthAgo = moment().subtract(1, 'M');
var twoMonthAgo = moment().subtract(2, 'M');
var threeMonthAgo = moment().subtract(3, 'M');
var fourMonthAgo = moment().subtract(4, 'M');

var appInstances =
[
	{
		"id": "0fb7338b-5e78-41db-b2bf-97f30e199639",
		"name": "hack-prod1",
		"appTemplateId": "d718e5dc-0001-47ef-a492-c56995aa6a49",
		"appTemplateName": "hackathon",
		"creatorUserId": "ad313156-c971-441c-aa8e-0f770754b185",
		"webEntryPointURL": "http://localhost:8083/hack-prod1/hackathon-api/html/nui/index.html",
		"state": "running",
		"stateMessage": null,
		"deploymentType": "test-dev",
		"dateCreated": today,
		"quota": {
			"orgId": "111",
			"psbQuota": 30,
			"dsbQuota": {
				"s3": 3,
				"postgres": 19,
				"mongo": 40
			}
		},
		"numberOfAppServices": 2,
		"numberOfInfraServices": 5
	},
	{
		"id": "0fb7338b-5e78-41db-b2bf-97f30e199639",
		"name": "mock",
		"appTemplateId": "d718e5dc-0001-47ef-a492-c56995aa6a49",
		"appTemplateName": "hackathon-test",
		"creatorUserId": "ad313156-c971-441c-aa8e-0f770754b185",
		"webEntryPointURL": "http://localhost:8083/hack-prod1/hackathon-api/html/nui/index.html",
		"state": "running",
		"stateMessage": null,
		"deploymentType": "test-dev",
		"dateCreated": oneMonthAgo,
		"quota": {
			"orgId": "111",
			"psbQuota": 55,
			"dsbQuota": {
				"s3": 8,
				"postgres": 19,
				"gal": 34
			}
		},
		"numberOfAppServices": 1,
		"numberOfInfraServices": 2
	},
	{
		"id": "0fb7338b-5e78-41db-b2bf-97f30e199639",
		"name": "test-test",
		"appTemplateId": "d718e5dc-0001-47ef-a492-c56995aa6a49",
		"appTemplateName": "hackathon-gal",
		"creatorUserId": "ad313156-c971-441c-aa8e-0f770754b185",
		"webEntryPointURL": "http://localhost:8083/hack-prod1/hackathon-api/html/nui/index.html",
		"state": "running",
		"stateMessage": null,
		"deploymentType": "test-dev",
		"dateCreated": twoMonthAgo,
		"quota": {
			"orgId": "111",
			"psbQuota": 3,
			"dsbQuota": {
				"s3": 12,
				"postgres": 20,
				"test": 45,
				"test-1": 68
			}
		},
		"numberOfAppServices": 1,
		"numberOfInfraServices": 2
	},
	{
		"id": "0fb7338b-5e78-41db-b2bf-97f30e199639",
		"name": "instance",
		"appTemplateId": "d718e5dc-0001-47ef-a492-c56995aa6a49",
		"appTemplateName": "hackathon",
		"creatorUserId": "ad313156-c971-441c-aa8e-0f770754b185",
		"webEntryPointURL": "http://localhost:8083/hack-prod1/hackathon-api/html/nui/index.html",
		"state": "running",
		"stateMessage": null,
		"deploymentType": "test-dev",
		"dateCreated": threeMonthAgo,
		"quota": {
			"orgId": "111",
			"psbQuota": 23,
			"dsbQuota": {
				"postgres": 19
			}
		},
		"numberOfAppServices": 1,
		"numberOfInfraServices": 2
	},
	{
		"id": "0fb7338b-5e78-41db-b2bf-97f30e199639",
		"name": "wordpress",
		"appTemplateId": "d718e5dc-0001-47ef-a492-c56995aa6a49",
		"appTemplateName": "hackathon",
		"creatorUserId": "ad313156-c971-441c-aa8e-0f770754b185",
		"webEntryPointURL": "http://localhost:8083/hack-prod1/hackathon-api/html/nui/index.html",
		"state": "running",
		"stateMessage": null,
		"deploymentType": "test-dev",
		"dateCreated": fourMonthAgo,
		"quota": {
			"orgId": "111",
			"psbQuota": 44,
			"dsbQuota": {
				"s3": 18,
				"postgres": 19
			}
		},
		"numberOfAppServices": 1,
		"numberOfInfraServices": 2
	}

];

export default appInstances;
