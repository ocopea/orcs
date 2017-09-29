// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';

var Actions = Reflux.createActions([

    "userClickOnAppTopologyService",
    "userClickOnAppTopologyDependency",
	  "setDependenciesContainerTransition",
    "setServicesContainerTransition",
    "setDependenciesTranslate",
    "setServicesTranslate",
    "initialAppTopology",
    "setSelectedApp",
    "setSelectedElementActiveState",
    "setSelectedElementVersion",
    "setSelectedElementPlan",
    "updateStateIndicators",
    "initializeSelectedElement"

]);

export default Actions;
