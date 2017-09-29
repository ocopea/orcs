// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';

var DevWizardActions = Reflux.createActions([
	"userClickOnNext",
	"userClickOnBack",
	"userSelectedApp",
	"setCurrentStep",
	"setImageSelectedTab",
	"setImageBackupSelectedDate",
	"showTagsTooltip",
	"hideTagsTooltip",
	"setTagsToolTipScrollTop",
	"showCopyDetails",
	"hideCopyDetails",
	"userSelectedImage",
	"copyDetailsTopologySelection",
	"userChangedAppInstanceName",
	"showConfigTopologyTooltip",
	"hideConfigTopologyTooltip",
	"toggleSwitch",
	"setTopologyActiveElements",
	"userChangedSelectedAppServiceVersion",
	"userClickOnBlankImage",
	"setSelectedSite",
	"setSelectedSpace",
	"getSites",
	"deploySavedImage",
	"setSelectedImage",
	"initiateSelectedImage",
	"setIsFromSavedImages",
	"invalidate"
]);

export default DevWizardActions;
