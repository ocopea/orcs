// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';

var SharedImageActions = Reflux.createActions([
  "setSelectedSite",
  "setSelectedPuspose",
  "setSelectedSpace",
  "deploySharedImage",
  "changeProjectName",
  "receiveSites",
  "createJiraIssue",
  "receiveDetails"
]);

export default SharedImageActions;
