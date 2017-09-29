// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import moment from 'moment';

var today = moment();
var oneMonthAgo = moment().subtract(1, 'M');
var twoMonthAgo = moment().subtract(2, 'M');
var threeMonthAgo = moment().subtract(3, 'M');
var fourMonthAgo = moment().subtract(4, 'M');


var MockImageOriginData = [
  {
    dateCreated: today,
    appTemplateId: "",
    createdByUserId: "",
    description: "description AAA",
    id: "0",
    url: "http://www.mock6.co.il",
    name: "mock 6",
    tags: ['tag', 'another tag', "ffff", "dfdfdfd"]
  },
  {
    dateCreated: oneMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description BBB",
    id: "1",
    url: "http://www.mock2.co.il",
    name: "mock 2",
    tags: ['gat', 'mock tag', "long long long long tag"]
  },
  {
    dateCreated: twoMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description CCC",
    id: "2",
    url: "http://www.mock3.co.il",
    name: "mock 3",
    tags: ['long long long long tag', 'long long long long tag', 'long long long long tag']
  },
  {
    dateCreated: threeMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description DDD",
    id: "3",
    url: "http://www.mock4.co.il",
    name: "mock 4",
    tags: ['asd', 'fff']
  },
  {
    dateCreated: fourMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description EEE",
    id: "4",
    name: "mock 5",
    url: "http://www.mock5.co.il",
    tags: ['aasd', 'fffffff']
  },
  {
    dateCreated: fourMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description FFF",
    id: "5",
    name: "mock 1",
    url: "http://www.mock1.co.il",
    tags: ['aasd', 'fffffff']
  },
  {
    dateCreated: fourMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description GGG",
    id: "6",
    name: "aaa mock 7",
    url: "http://www.aaamock7.co.il",
    tags: ['aasd', 'fffffff']
  },
  {
    dateCreated: fourMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description HHH",
    id: "7",
    name: "mock 8",
    url: "http://www.mock8.co.il",
    tags: ['aasd', 'fffffff']
  },
  {
    dateCreated: fourMonthAgo,
    appTemplateId: "",
    createdByUserId: "",
    description: "description III",
    id: "8",
    name: "mock 9",
    url: "http://www.mock9.co.il",
    tags: ['aasd', 'fffffff']
  }
];

export default MockImageOriginData;
