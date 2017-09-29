// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';
import _ from 'lodash';
import Baseline from './base-line.js';
import componentTypes from '../../data/componentTypes.json';
import TopologyUiStore from '../../controllers/topology-ui-controller.js';
const scrollHeight = 25;
const margin = 15;


function GetLineElementToBase(elementId, componentType, baselineHeight){

  const element =           $(`#${elementId}`);

  if(!_.isEmpty(element)){

    let uiStore =           TopologyUiStore;
    let scrollabels =       uiStore.scroll;
    const cssMargin =       uiStore.cssMargin;
    const baselineWidth =   uiStore.baselineWidth;
    const containerHeight = uiStore.containerHeight;
    const isService =       componentType === componentTypes.service;
    const isDependency =    componentType === componentTypes.dependency;
    const type =            {isService: isService, isDependency: isDependency}
    const elementSize =     getElementSize(element);
    const elementPosition = getElementPosition(element);
    // const baseline =        Baseline.getBaseline();
    const pointStart =      getStartingPoint(containerHeight,elementPosition, elementSize, type,
                                              baselineWidth, cssMargin, scrollabels,
                                              componentType, uiStore.marginBetweenElements);
    const pointEnd =        getEndPoint(pointStart, type, elementSize, baselineHeight,
                                        baselineWidth, cssMargin, scrollabels, componentType);

// console.log({ start: pointStart, end: pointEnd })
    return { start: pointStart, end: pointEnd };

  } else { return null }
}

function getEndPoint(pointstart, type, elementSize,
    baselineHeight, baselineWidth, cssMargin, scrollabels, componentType){

  return {
    x: pointstart.x,
    y: componentType === componentTypes.service ? cssMargin : 0
  }
}

function getStartingPoint(containerHeight,elementPosition, elementSize,
    type, baselineWidth, cssMargin, scrollabels,
    componentType, marginBetweenElements){

  return {
    x: getPoint(componentType, scrollabels[componentType], cssMargin, marginBetweenElements).x,
    y: getPoint(componentType, scrollabels[componentType], cssMargin, marginBetweenElements).y
  }

  function getPoint(componentType, isScroll, cssMargin, marginBetweenElements){
    let x, y;
    switch (componentType) {
      case componentTypes.service:
        y = containerHeight;
        break;
      case componentTypes.dependency:
        y = containerHeight / 2 - 50;
        break;
    }
    x = elementSize.width / 2 + marginBetweenElements;
    return {x: x, y: y }
  }
}

function getElementSize(element){
  return {
    width:  $(element).width(),
    height: $(element).height()
  }
}

function getElementPosition(element){
  return {
    x: $(element).position().left,
    y: $(element).position().top
  }
}

export default GetLineElementToBase;
