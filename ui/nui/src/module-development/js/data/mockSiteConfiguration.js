// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var mockDataService1 = {
  dataServiceName: 'test',
  dsbPlans: {
    test: {
      description: "test description",
      name: "TEST PLAN",
      plans: [
        {description: "test plan 1 description", name: "plan 1"},
        {description: "test plan 2 description", name: "plan 2"},
        {description: "test plan 3 description", name: "plan 3"}
      ]
    }
  }
};

var mockDataService2 = {
  dataServiceName: 'test1',
  dsbPlans: {
    test1: {
      description: "test description",
      name: "TEST1 PLAN",
      plans: [
        {description: "test1 plan 1 description", name: "plan a"},
        {description: "test1 plan 2 description", name: "plan b"},
        {description: "test1 plan 3 description", name: "plan c"}
      ]
    },
    test2: {
      description: "test description",
      name: "TEST2 PLAN",
      plans: [
        {description: "test2 plan 1 description", name: "plan test2 a"},
        {description: "test2 plan 2 description", name: "plan test2 b"},
        {description: "test2 plan 3 description", name: "plan test2 c"}
      ]
    },
  }
};

var MockConfiguration = {
  appServiceConfigurations: [
    {dataServiceName: "Mock", supportedVersions: {'shpanRegistry': ['0.1', '0.2']}}
  ],
  dataServiceConfigurations: [
    mockDataService1,
    mockDataService2
  ]
};

export default MockConfiguration;
