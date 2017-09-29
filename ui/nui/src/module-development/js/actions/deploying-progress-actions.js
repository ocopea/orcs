// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';

var DeployingProgressActions = Reflux.createActions([
  "setAllLogsFilters",
  "toggleLogsFilter",
  "receiveLog",
  "seeAllLogsFilters",
  "setAllFilters",
  "addFilter",
  "userSearchLogs",
  "clearLogs",
  "initializeSelectedInstance"
]);

export default DeployingProgressActions;
