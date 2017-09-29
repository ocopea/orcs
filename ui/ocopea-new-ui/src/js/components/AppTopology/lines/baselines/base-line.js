// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';
import _ from 'lodash';
import DataStore from '../../controllers/topology-data-controller.js';
import UiStore from '../../controllers/topology-ui-controller.js';

const topologyUiStore =   UiStore;


function getBaseline(){

  $('.Topology__line--base-line').empty();

  let startPoint = {};
  let endPoint = {};
  let baseLine = {
    start:  { x: 0, y: 0 },
    end:    { x: 0, y: 0 }
  };
  const appServicesContainer =    $('.Topology__app-services');
  const dataServicesContainer =   $('.Topology__data-services');
  const lineWidth =               topologyUiStore.baselineWidth;
  const top =                     topologyUiStore.baselineHeight / 2;
  const margin =                  topologyUiStore.elementSize.dependency.width / 2;

  const firstDependency =         dataServicesContainer[0].firstChild;
  const lastDependency =          dataServicesContainer[0].lastChild;
  const firstService =            appServicesContainer[0].firstChild;
  const lastService =             appServicesContainer[0].lastChild;

  if(!firstDependency){
    console.log('no dependencies- base-line.js')
    return null
  }

  const firstDependencyX =        $(firstDependency).position().left  + margin;
  const lastDependenciesX =       $(lastDependency).position().left   + margin;
  const firstServiceX =           $(firstService).position().left     + margin;
  const lastServiceX =            $(lastService).position().left      + margin;

  baseLine.start.x =  _.min([firstServiceX, firstDependencyX]) + topologyUiStore.marginBetweenElements;
  baseLine.end.x =    _.max([lastServiceX, lastDependenciesX]) + topologyUiStore.marginBetweenElements;
  baseLine.start.y =  top + lineWidth;
  baseLine.end.y =    top + lineWidth;

  const dataServicesScrollLeft = $('.Topology__data-services').scrollLeft();
  const appServicesScrollLeft = $('.Topology__app-services').scrollLeft();

  // if container is scrolled content is justified to start
  // therefor size diff between container should be take in account
  const serviceElementWidth =     topologyUiStore.elementSize.service.width
  const dependencyelementWidth =  topologyUiStore.elementSize.dependency.width
  const max = _.max([serviceElementWidth, dependencyelementWidth]);
  const min = _.min([serviceElementWidth, dependencyelementWidth]);
  const diff = max - min;

  const _a = topologyUiStore.marginBetweenElements + diff/2;
  const _b = topologyUiStore.marginBetweenElements;

  if(firstDependencyX >= firstServiceX + (diff/2)){
    baseLine.start.x = firstServiceX + _a - 1.5;
  }else{
    baseLine.start.x = firstDependencyX + _b;
  }
  if(lastServiceX > lastDependenciesX - (diff/2)){
    baseLine.end.x = lastServiceX + _a;
  }

  return baseLine;
}

const setBaselineHeight = (className, height) => {
  $(className).height(height);
}

const Baseline = {
  getBaseline: getBaseline,
  setBaselineHeight: setBaselineHeight
}

export default Baseline;
