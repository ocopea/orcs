// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Config from '../../module-production/js/config.js';
import MockServiceImg from '../../module-production/assets/images/appTopology/service.png';
import MockDependencyImg from '../../module-production/assets/images/appTopology/db.png';
import _ from 'lodash';

import MockData from '../../module-development/js/data/dashboard-topology-mockdata.js';


var TopologyParser = {

  appServiceTemplates: [],
  appCopyDetails: {},
  dependencies: {},
  isMock: false,

  parse: function(imageDetails){

    if(this.isMock){
      this.getAppCopyDetails(MockData.appCopyDetails);
      this.dependencies = MockData.serviceCopyDetails;
    }else{
      this.getAppCopyDetails(imageDetails.appCopyDetails || imageDetails.appServices);
      this.dependencies = imageDetails.serviceCopyDetails || imageDetails.dataServices;
    }
    
    return {
      'appServiceTemplates': this.getServices(this.appCopyDetails, this.dependencies)
    };

  },

  getAppCopyDetails: function(appCopyDetails){
    this.appCopyDetails = appCopyDetails;
  },

  getServiceIcon: function(copyDetails){
    return Config.fixImgUrl(copyDetails.img || copyDetails.imageIconUrl) || MockServiceImg
  },

  getDependencyIcon: function(dependency){
    return Config.fixImgUrl(dependency.img || dependency.iconUrl) || MockDependencyImg
  },

  getServices: function(appCopyDetails, dependencies){
    var that = this;
    var index = 0;
    var services = _.map(appCopyDetails, copyDetails=>{
      return {
        entryPointUrl: copyDetails.entryPointUrl,
        name: copyDetails.imageName,
        img: that.getServiceIcon(copyDetails),
        dependencies: this.getDependencies(
                        copyDetails.serviceBindings, dependencies
                      ),
        version: copyDetails.imageVersion,
        isActive: true,
        state: copyDetails.state
      }
      index++;
    });
    _.forEach(services, (service, index)=>{
      service.serviceId = index;
    });
    return services;
  },

  getDependencies: function(serviceBindings, dependencies){
    var that = this;
    var arr = _.map(dependencies, d=>{return d});
    // reverse dependencies order to fit serviceBindings order
    var dependenciesArray = arr.reverse();
    // iterate service bindings and return dependency object

    return _.map(serviceBindings, (dependencyName, index)=>{
        var dependency = dependenciesArray[index] || dependencies[dependencyName];
        var keys = Object.keys(dependencies).reverse();
        var dependencyName = dependency.bindName || keys[index];

        return{
          name: dependencyName,
          img: that.getDependencyIcon(dependency),
          restoreTimeInSeconds: dependency.restoreTimeInSeconds,
          sizeInBytes: dependency.sizeInBytes,
          isActive: true,
          index: index,
          state: dependency.state,
          plan: dependency.plan,
          type: 'dsb'
        }
    });
  }

}

export default TopologyParser;

//expected structure
// {
//   appCopyDetails: {
//     templateName: {
//       imageName: "",
//       imageVersion: "",
//       serviceBindings: []
//     }
//   }
// }
// {
//   serviceCopyDetails:{
//     infraStructureName:{
//       dsbName: "",
//       restoreTimeInSeconds: 0,
//       sizeInBytes: 0
//     }
//   }
// }
