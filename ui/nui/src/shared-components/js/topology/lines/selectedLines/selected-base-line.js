// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';
import _ from 'lodash';
import Baseline from '../baselines/base-line.js';
import TopologyUiStore from '../../controllers/topology-ui-controller.js';
import componentTypes from '../../data/componentTypes.json';


const GetSelectedBaseline = (selectedElement, margin) => {

  const dataServices = $('.Topology__element__dependency');
  if(!dataServices[0]){
    console.log('no related elements- selected-base-line.js')
    return null
  }

  let pointStart, pointEnd;
  const uiStore =               TopologyUiStore;
  const marginBetweenElements = uiStore.marginBetweenElements;
  const allDomElements =        $('.Topology__element');
  const relatedDomElements =    getRelatedDomElements(allDomElements, selectedElement);
  const componentType =         selectedElement.element.componentType;
  const firstDomElement =       _.last(relatedDomElements);
  const lastDomElement =        relatedDomElements[0];
  const selectedDomElement =    getSelectedDomElement(allDomElements, selectedElement.element.id);
  const firstDomElementPoints = getPointsFromDomElement(firstDomElement, componentType, margin);
  const lastDomElementPoints  = getPointsFromDomElement(lastDomElement, componentType, margin);
  lastDomElementPoints.x -=     uiStore.baselineWidth*2;
  const selectedElementPoints = getPointsFromDomElement(
    selectedDomElement, selectedElement.element.componentType, margin
  );

  selectedElementPoints.x = selectedElementPoints.x < firstDomElementPoints.x + margin ?
                            selectedElementPoints.x - uiStore.baselineWidth*2-1 :
                            selectedElementPoints.x;

  let points = getPoints(
    firstDomElementPoints, lastDomElementPoints,
    selectedElementPoints, componentType, margin, uiStore.baselineWidth
  );

  points.start.x += marginBetweenElements;
  points.end.x += marginBetweenElements;
console.log(points.start.x)
  return points;

}

const getPoints = (firstDomElementPoints, lastDomElementPoints,
                   selectedElementPoints, componentType,
                   elementMargin, baselineHeight) => {

  let selectedElementX = componentType === componentTypes.service ?
                          selectedElementPoints.x + elementMargin :
                          selectedElementPoints.x - elementMargin;
  let pointStart = {};
  let pointEnd = {};

  const min = _.min([selectedElementX, firstDomElementPoints.x, lastDomElementPoints.x]);
  const max = _.max([selectedElementX, firstDomElementPoints.x, lastDomElementPoints.x]);

  pointStart.x =  componentType === componentTypes.service ? min : min+1;
  pointEnd.x =    max;
  pointStart.y =  Baseline.getBaseline().start.y;
  pointEnd.y =    Baseline.getBaseline().start.y;


  if(pointStart && pointEnd){
    return {start: pointStart, end: pointEnd}
  }

}

const getSelectedDomElement = (allDomElements, selectedElementId) => {
  return _.filter(allDomElements, o =>{
      return o.id === selectedElementId;
  })[0];
}

const getRelatedDomElements = (allDomElements, selectedElement) => {
  return _.filter(allDomElements, o =>{
      return selectedElement.relatedElements[o.id];
  });
}

const getPointsFromDomElement = (domElement, componentType, m) => {
  const uiStore =     TopologyUiStore;
  const elementSize = uiStore.elementSize;
  const margin =      componentType === componentTypes.service ? -m : m;
  let addToX =        elementSize[componentType].width / 2 + margin + uiStore.baselineWidth;

  return {
    x: $(domElement).position() ? $(domElement).position().left + addToX : 0,
    y: Baseline.getBaseline().start.y
  }
}

export default GetSelectedBaseline;
