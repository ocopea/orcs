// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const appInstanceApi = {
  appInstance:         `${APISERVER}/hub-web-api/app-instance`,
  appInstanceByID:     id => `${APISERVER}/hub-web-api/app-instance/${id}`,
  testDevInstance:     `${APISERVER}/hub-web-api/test-dev/app-instance`,
  state:               instanceID => `${APISERVER}/hub-web-api/app-instance/${instanceID}/state`,
  logs:                instanceID => `${APISERVER}/hub-web-api/app-instance/${instanceID}/logs`,
  dashboardStats:      instanceID => `${APISERVER}/hub-web-api/app-instance/${instanceID}/dashboard-stats`,
  copyHistory:         (instanceID, interval, range) =>
                       `${APISERVER}/hub-web-api/app-instance/${instanceID}/copy-history?interval=${interval}&period=${range}`,
  createCopy:          `${APISERVER}/hub-web-api/commands/create-app-copy`,
  repurposeCopy:       `${APISERVER}/hub-web-api/commands/repurpose-app`                       
}

export default appInstanceApi;
