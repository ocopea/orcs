// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, autorun, observable, computed, mobx } from 'mobx';
import MockParsedData from '../mockData/topology-mock-parsed-data.json';
import { Request } from '../../../../utils/transportLayer.js';
import componentTypes from '../data/componentTypes.json';
import TopologyDataStore from './topology-data-controller.js';


class UiStore {

  constructor() {
    this.containerHeight += this.cssMargin * 2;
  }

  @observable selectedElement;

  isLoading = null;

  // BASIC SETTINGS- can be adjusted by component
  //                 via setters actions below
  showState = true;

  showLogoCircle = true;

  showIconCircle = false;

  showAlerts = true;

  @observable showLines = true;

  highlightRelatedElements = true;

  showPlanSelectionMenu = false;

  marginBetweenElements = 20;
  containerWidth = 548;
  containerHeight = 350;

  elementSize = {
    service: { width: 134, height: 134 },
    dependency: { width: 110, height: 110 }
  };
  // =========================================

  @observable tooltip = {
    isRender: false,
    position: { x: 0, y: 0 }
  };

  scroll = {
    service: false,
    dependency: false
  }

  // DO NOT MODIFY
  baselineHeight = 10;
  baselineWidth = 2;
  cssMargin = 26;

  @computed get margin(){
    const serviceWidth =      this.elementSize.service.width;
    const dependencyWidth =   this.elementSize.dependency.width
    return serviceWidth     > dependencyWidth  ?
           (serviceWidth    - dependencyWidth) / 2 :
           (dependencyWidth - serviceWidth)    / 2;
  }

  getScrolledContainers(){

    const containerWidth =         this.containerWidth;
    const dataStore =              TopologyDataStore;
    const servicesLen =            dataStore.services.length;
    const dependenciesLen =        dataStore.dependencies.length;
    const serviceWidth =           this.elementSize[componentTypes.service].width;
    const dependencyWidth =        this.elementSize[componentTypes.dependency].width;
    const margin =                 this.marginBetweenElements;
    const totalServiceWidth =      (serviceWidth + margin + this.cssMargin) * servicesLen;
    const totalDependenciesWidth = (dependencyWidth + margin + this.cssMargin) * dependenciesLen;
    const isServicesScroll =       totalServiceWidth >= containerWidth;
    const isDependenciesScroll =   totalDependenciesWidth >= containerWidth;
    this.scroll.service =          isServicesScroll;
    this.scroll.dependency =       isDependenciesScroll;
  }

  // components controllers
  @action setContainerWidth(width) {
    if(width && width >= 300) this.containerWidth = width;
    else console.log('width should be a valid int and larger that 500 '+
                     '- topology ui store setContainerWidth()');
  }

  @action setContainerHeight(height) {
    if(height && height >= 300) this.containerHeight = height;
    else console.log('height should be a valid int and larger that 350 '+
                     '- topology ui store setContainerHeight()');
  }

  @action setShowIconCircle(bool) {
    this.showIconCircle = bool;
  }

  @action setShowLogoCircle(bool) {
    this.showLogoCircle = bool;
  }

  @action setShowState(bool) {
    this.showState = bool;
  }

  @action setShowAlerts(bool) {
    this.showAlerts = bool;
  }

  @action setShowLines(bool) {
    this.showLines = bool;
  }

  @action setHighlightRelatedElements(bool) {
    this.highlightRelatedElements = bool;
  }

  @action setElementSize(size) {
    const isValid = size &&
                    size.service &&
                    size.dependency &&
                    size.service.width && size.service.height &&
                    size.dependency.width && size.dependency.height;

    if(isValid){
      this.elementSize = size;
    }else{
      console.log('invalid size object provided to setElementSize in topology ui store')
    }
  }

  @action setShowPlanSelectionMenu(bool) {
    this.showPlanSelectionMenu = bool;
  }

  @action renderPlansMenu(bool) {
    this.tooltip.isRender = bool;
  }
  // ======================

  @action setTooltipPosition(position) {
    this.tooltip.position = position;
  }

  @action setSelectedElement(element){
    this.selectedElement = {};
    if(element){
      this.selectedElement['element'] = element;
      this.getRelatedElements(element)
    }else{
      throw new Error('invalid object supplied to setSelectedElement in topology-ui-controller.js')
    }
  }

  initiateSelectedElement() {
    this.selectedElement = {};
  }

  getRelatedElements(element){
    switch (element.componentType) {
      case componentTypes.service:
        let relatedDependencies = this.getRelatedDependencies(element)
        this.selectedElement.relatedElements = relatedDependencies;
        break;
      case componentTypes.dependency:
        let relatedServices = this.getRelatedServices(element);
        this.selectedElement.relatedElements = relatedServices;
        break;
    }
  }

  getRelatedServices(dependency){
    const dataStore = TopologyDataStore;
    const appServices = dataStore.parsedData.appServices;
    let relatedservices = {};
    _.map(appServices, service=>{
      let serviceBindings = service.serviceBindings;
       if(serviceBindings.indexOf(dependency.id) > -1){
         relatedservices[service.id] = service;
       }
    });
    return relatedservices;
  }

  getRelatedDependencies(service){
    const dataServices = TopologyDataStore.parsedData.dataServices;
    const serviceBindings = service.serviceBindings;
    let relatedDependencies = {};
    serviceBindings.map(binding=>{
      relatedDependencies[binding] = TopologyDataStore.parsedData.dataServices[binding]
    });
    return relatedDependencies;
  }

}

export default new UiStore();

autorun((e) => {
  // console.log(e)
});
