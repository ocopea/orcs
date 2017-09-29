// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import uuid from 'uuid/v4';
import componentTypes from '../../data/componentTypes.json';

class instanceStateConverter {

  convert(instance) {
    this.instance = instance;
    this.converted = {
      appServices: {},
      dataServices: {}
    };
    // interate all app services
    instance.appServices.forEach(appService=>{
      const appServiceId = appService.imageName;
      this.converted.appServices[appServiceId] = this.convertService(
                                                  appService, appServiceId
                                                );
    });
    // interate all data services
    instance.dataServices.forEach(dataService=>{
      const dataServiceId = dataService.serviceId;
      this.converted.dataServices[dataServiceId] = this.convertDataService(
                                                    dataService, dataServiceId
                                                  );
    });
    // convert service binding from data service name to data service id
    this.converted.appServices = this.convertServiceBindings(
                                    this.converted.appServices
                                 );

    return this.converted;
  }

  convertService(appService, appServiceId) {
    return {
      componentType: componentTypes.service,
      id: appServiceId,
      iconSrc: null,
      imgSrc: APISERVER + appService.imageIconUrl,
      name: appService.serviceName,
      serviceBindings: appService.serviceBindings,
      webEntryPointUrl: appService.entryPointUrl,
      state: appService.state,
      isActive: true,
      versions: []
    }
  }

  convertServiceBindings(appServices) {
    _.forEach(appServices, appService => {
      appService.serviceBindings.forEach((binding, i) => {
        const id = this.getDataServiceIdByName(binding);
        appService.serviceBindings[i] = id;
      });
    });
    return appServices;
  }

  getDataServiceIdByName(dataServiceName) {
    const dataService = _.filter(this.converted.dataServices, dataService => {
      return dataServiceName === dataService.name;
    })[0];
    return dataService ? dataService.id : null;
  }

  convertDataService(dataService, dataServiceId) {
    return {
      componentType: componentTypes.dependency,
      id: dataServiceId,
      iconSrc: null,
      imgSrc: APISERVER + dataService.iconUrl,
      name: dataService.bindName,
      state: dataService.state,
      isActive: true,
      selectedPlan: {name: null, service: null}
    }
  }

}

export default new instanceStateConverter();
