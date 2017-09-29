// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var DashboardTopologyMockData = {
  "serviceCopyDetails": {
    "dep-hack-docs": {
      "copyRepoName": "DDVE",
      "sizeInBytes": 190,
      "img": "../img/infra-svc-s3.png",
      "restoreTimeInSeconds": 500,
      "dsbName": "shpanblob-dsb",
      "facility": "shpanCopy",
      "type": "gal-a"
    },
    "dep-hackathon-db": {
      "copyRepoName": "DDVE",
      "img": "../img/infra-svc-postgres.png",
      "sizeInBytes": 1348,
      "restoreTimeInSeconds": 500,
      "dsbName": "h2-dsb",
      "facility": "shpanCopy",
      "type": "gal-b"
    },
    "dep-mock-db": {
      "copyRepoName": "DDVE",
      "img": "../img/infra-svc-postgres.png",
      "sizeInBytes": 1348,
      "restoreTimeInSeconds": 500,
      "dsbName": "h2-dsb",
      "facility": "shpanCopy",
      "type": "gal-c"
    },
    "dep-mock-zaain-db": {
      "copyRepoName": "DDVE",
      "img": "../img/infra-svc-postgres.png",
      "sizeInBytes": 1348,
      "restoreTimeInSeconds": 500,
      "dsbName": "h2-dsb",
      "facility": "shpanCopy",
      "type": "gal-d"
    },
    "dep-test-db": {
      "copyRepoName": "DDVE",
      "img": "../img/infra-svc-postgres.png",
      "sizeInBytes": 1348,
      "restoreTimeInSeconds": 500,
      "dsbName": "h2-dsb",
      "facility": "shpanCopy",
      "type": "gal-e"
    }
  },
  "appCopyDetails": {
    "service-hackathon": {
      "imageName": "hackathon",
      "imageVersion": "1.0",
      "img": "../img/app-svc-hackathon.png",
      "serviceBindings": [
        "dep-hack-docs",
        "dep-mock-zaain-db"
      ]
    },
    "service-mock": {
      "imageName": "gal mock",
      "imageVersion": "1.0",
      "img": "../img/app-svc-hackathon.png",
      "serviceBindings": [
        "dep-hackathon-db",
        "dep-mock-zaain-db"
      ]
    },
    "service-zaain": {
      "imageName": "zaain",
      "imageVersion": "1.0",
      "img": "../img/app-svc-hackathon.png",
      "serviceBindings": [
        "dep-hack-docs"
      ]
    }
  }
}


export default DashboardTopologyMockData;
