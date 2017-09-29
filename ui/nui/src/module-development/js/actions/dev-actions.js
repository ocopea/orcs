// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';


var DevActions = Reflux.createActions([

	"showQuotasList",
	"hideQuotasList",
  "userClickOnFilterTitle",
  "userClickOnFilter",
  "getAppInstances",
	"userChangedView",
	"toggleUserMenu",
	"closeUserMenu",
	"toggleSortByMenu",
	"closeSortByMenu",
	"sortInstances",
	"showShareImageDialog",
	"hideShareImageDialog",
	"filterImages",
	"initiateOriginalImages",
	"shareImage",
	"getSavedAppImages",
	"goToDeployingProgress",
	"goToSharedImage",
	"handleLeftMenuOnInit",
	"hideDialogSavedSuccessfuly",
	"checkState",
	"receiveSavedImages",
	"initateInstanceStatus",
	"getSavedImageById",
	"goToSettings",
	"goToIntegrationsHub",
	"goToConfigurePlatform"
]);

export default DevActions;
