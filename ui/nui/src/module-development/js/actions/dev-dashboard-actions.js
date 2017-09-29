// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';


var DevDashboardActions = Reflux.createActions([
	"userClickOnMainScreenCard",
	"showCreateImageDialog",
	"hideCreateImageDialog",
	"createImage",
	"createImageAddTag",
	"createImageRemoveTag",
	"userChangedCreateImageName",
	"userChangedCreateImageTags",
	"initiateImageTags",
	"userChangedCreateImageComment",
	"setSelectedInstance",
	"mockImageOrigin",
	"showImageOriginList",
	"hideImageOriginList",
	"sortImageOriginByUserName",
	"sortImageOrignByImageName",
	"sortImageOriginByDate",
	"showConfirmDisposeImageDialog",
	"hideConfirmDisposeImageDialog",
	"receiveUsers",
	"receiveAppTemplates",
	"stopFetchingState",
	"stopApp",
	"setLoadingImage"
]);

export default DevDashboardActions;
