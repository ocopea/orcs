// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';

var SharedImageActions = Reflux.createActions([
  "userSelectedTab",
  "receiveSites",
  "userSelectedSite",
  "addArtifact",
  "closeAddArtifactDialog",
  "setSelectedArtifact",
  "setAddArtifactCurrentStep",
  "changeUserNameMaven",
  "changePasswordMaven",
  "changeNameMaven",
  "changeUrlMaven",
  "removeMavenRegistry",
  "showCofirmRemoveDialog",
  "hideCofirmRemoveDialog",
  "clearValidations",
  "editRegistry",
  "userChangedCustomRegistryUrl",
  "userChangedCustomRegistryName",
  "showDsbDialog",
  "hideDsbDialog",
  "setSelectedDsbPlan",
  "showDsbPlanInfoMenu",
  "hideDsbPlanInfoMenu",
  "showAddDsbDialog",
  "addDsb",
  "deleteDsb",
  "showConfirmRemoveDsbDialog",
  "showAddCrbDialog",
  "showAddCrDialog",
  "addCrb",
  "addCr",
  "showConfirmDeleteCrbDialog",
  "confirmDeleteCrb"
]);

export default SharedImageActions;
