// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { action, autorun, observable } from 'mobx';
import componentTypes from '../data/componentTypes.json';
import _ from 'lodash';


class DataStore {

  @observable services = [];
  @observable dependencies = [];
  parsedData = {};

  @action init(data, configuration){
    if(data && !_.isEmpty(data)){
      this.parsedData = data;
      this.services = this.convertToArray(this.parsedData.appServices);
      this.dependencies = this.getDependencies(
        this.services, this.convertToArray(this.parsedData.dataServices)
      );
    }else{
      console.log('data provided to topology data store is not valid: ', data)
    }
  }

  getDependencies(services, dependencies) {
    let bindings = [];
    services.forEach(service=>{
      service.serviceBindings.forEach(binding=>{
        bindings.push(binding);
      });
    });
    return _.filter(dependencies, dependency=>{
      return bindings.indexOf(dependency.id) > -1;
    });
  }

  convertToArray(object){
    return _.map(object, (value, key) => {
      return value;
    });
  }

  setElementVersion(elementID, version) {
    let element = this.getElementById(elementID);
    element.version = version;
  }

  setElementPlan(elementID, plan) {
    let element = this.getElementById(elementID);
    element.selectedPlan.name = plan;
  }

  setSelectedPlanService(elementID, service) {
    let element = this.getElementById(elementID);
    element.selectedPlan.service = service;
  }

  getElementById(id) {
    const service = _.filter(this.services, service=>{return service.id === id})[0];
    const dependency = _.filter(this.dependencies, dependency=>{return dependency.id === id})[0];
    if(service) return service
    else if(dependency) return dependency
    else return null;
  }

  getData() {
    return {
      appServices: this.services.slice(),
      dataServices: this.dependencies.slice()
    }
  }

  toggleElementActivation(elementID) {
    const element = this.getElementById(elementID);
    element.isActive = !element.isActive;
  }

}


const singleton = new DataStore();
export default singleton;

autorun(() => {
  // console.log(singleton.dependencies.slice())
});
