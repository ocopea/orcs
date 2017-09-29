// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const size = {
  service: {width: 100, height: 100},
  dependency: {width: 100, height: 100}
};

export default const init = () => {
  TopologyUiController.setElementSize(size);
  const isScroll = converted ? _.size(converted.dataServices) >= 3 : true;
  TopologyUiController.setContainerWidth(isScroll ? 548 : 300);
  TopologyUiController.setContainerHeight(300);
  TopologyUiController.setShowIconCircle(true);
  TopologyUiController.setShowLogoCircle(false);
  TopologyUiController.setShowState(false);
  TopologyUiController.setShowAlerts(true);
  TopologyUiController.setShowLines(true);
  TopologyUiController.setHighlightRelatedElements(true);
  TopologyUiController.setShowPlanSelectionMenu(true);
  TopologyDataController.init(converted);
}
