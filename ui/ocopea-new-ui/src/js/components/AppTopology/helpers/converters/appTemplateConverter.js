// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import _ from 'lodash';
import uuid from 'uuid/v4';
import componentTypes from '../../data/componentTypes.json';


class AppTemplateConverter {

  appTemplates;

  constructor() {
    this.appTemplates = [];
  }

  convert(appTemplate) {

    let topologyObject = {
      appServices: {},
      dataServices: {}
    };

    if(appTemplate && !_.isEmpty(appTemplate)){
      // iterate all appServices
      appTemplate.appServiceTemplates.forEach(service=>{

        // create app service object
        const appServiceId = uuid();
        topologyObject.appServices[appServiceId] = new TopologyElement(
          service, componentTypes.service, appServiceId
        ).getObject();

        // iterate all dependencies
        service.dependencies.forEach(dependency=>{

          const exist = _.filter(topologyObject.dataServices, dataService=>{
            return dataService.name === dependency.name &&
                   dataService.type === dependency.type
          })[0];

          if(exist){
            // update serviceBindings
            topologyObject.appServices[appServiceId].serviceBindings.push(exist.id);
          }else{
            // create data service object
            const dataServiceId = uuid();
            topologyObject.appServices[appServiceId].serviceBindings.push(dataServiceId);
            topologyObject.dataServices[dataServiceId] = new TopologyElement(
              dependency, componentTypes.dependency, dataServiceId, appServiceId
            ).getObject();
          }

        });

      });

      return topologyObject
    }else{
      console.log('object provided to '+
                  'appTemplateConverter '+
                  'is not a valid appTemplate object: ', appTemplate)
    }

  }

}


class TopologyElement {
  componentType;
  id;
  iconSrc;
  imgSrc;
  name;
  entryPointUrl;
  state;
  version;
  serviceBindings;
  description;
  appServiceId;
  type;
  selectedPlan;
  isActive;

  constructor(element, componentType, id, appServiceId) {
    this.componentType = componentType;
    this.id = id;
    this.iconSrc = element.iconSrc;
    this.imgSrc = APISERVER.length > 0 &&
                  element.img.indexOf('http://') === -1 ?
                  APISERVER + element.img : element.img;
    this.name = element.appServiceName || element.name;
    this.entryPointUrl = element.entryPointUrl;
    this.state = element.state || '';
    this.version = element.imageVersion;
    this.serviceBindings = element.serviceBindings || [];
    this.description = element.description;
    this.appServiceId = appServiceId;
    this.type = element.type;
    this.selectedPlan = {name: null, service: null};
    this.isActive = true;
  }

  getObject() {
    return {...this};
  }
}

export default new AppTemplateConverter();
