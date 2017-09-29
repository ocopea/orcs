// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Actions from '../../actions/appTopologyActions.js';
import ActionsMain from '../../actions/actions.js';
import DevWizardActions from '../../../../module-development/js/actions/dev-wizard-actions.js';
import DevWizardStore from '../../../../module-development/js/stores/dev-wizard-store.js';
import DevDashboardActions from '../../../../module-development/js/actions/dev-dashboard-actions.js';
import data from '../../data/appTopologyData.js';
import DevNavigationOptions from '../../../../module-development/js/data/devNavigationOptions.js';
import ProdNavigationOptions from '../../data/prodNavigationOptions.js';
import _ from 'lodash';
import Config from '../../config.js';


var Store = Reflux.createStore({

    listenables: [Actions, ActionsMain, DevWizardActions, DevDashboardActions],

    state: {

        appTopology: {

            application: {
                allApps: [],
                selectedApp: {},
                selectedAppIndex: 4,
                allServices: [],
                allDependencies: []
            },

            services: {
                selectedService: {
                    service: {},
                    relatedDependencies: []
                },
                translate: 0
            },

            dependencies: {
                selectedDependency: {
                    dependency: {},
                    relatedServices: []
                },
                translate: 0
            },

            componentTypes: {
                dependency: "dependency",
                service: "service"
            },

            params: {
                containerWidth: 600,
                containerHeight: 500,
                boxSize: 100,
                space: 50,
                servicesHeight: 0,
                dependenciesHeight: 0,
                baseLineHeight: 0,
                padding: 50,
            },

            isMockData: false,

            selectedElement: {}

        }

    },

    getInitialState: function(){
      	return this.state;
    },

    init: function(){

        var state = this.state.appTopology,
            selectedAppIndex = state.application.selectedAppIndex,
            that = this;

        if(state.isMockData){

            //populate all application
            state.application.allApps = data;

            //populate selected application
            state.application.selectedApp = data[selectedAppIndex];

            //populate all services in selected application
            var allServices = this.getAllServicesByApp(data[selectedAppIndex]);

            state.application.allServices = allServices;

            //populate all dependencies
            state.application.allDependencies = this.getAllDependenciesInServiceArray(allServices);

        }

        //set services and dependencies height params
        state.params.servicesHeight = state.params.containerHeight / 6;
        state.params.dependenciesHeight = state.params.containerHeight / 1.6;
        state.params.baseLineHeight = state.params.dependenciesHeight -
                                        state.params.servicesHeight + 15;

        this.trigger(this.state);
    },

    isDevSharedImageScreen: function(){
      var module = Config.getCurrentHash().module,
          location = Config.getCurrentHash().location;
      if(module === DevNavigationOptions.module &&
         location === DevNavigationOptions.sharedImage.location.substring(1)){
           return true;
         }else{
           return false;
         }
    },

    onInitialAppTopology: function(){
        var state = this.state.appTopology;
        state.services.selectedService.service = {};
        state.services.selectedService.relatedDependencies = [];
        state.dependencies.selectedDependency.dependency = {};
        state.dependencies.selectedDependency.relatedServices = [];
        state.dependencies.translate = 0;
        state.services.translate = 0;
        state.selectedElement = {};
        state.application.selectedApp = {};

        this.trigger(this.state);
    },

    onSetSelectedApp: function(selectedApp){
      var selectedAppId = selectedApp.id;
      var storedAppId = this.state.appTopology.application.selectedApp.id;
      if(!_.isEmpty(selectedApp)){
        this.state.appTopology.application.selectedApp = selectedApp;
        this.setAllServicesAndAllDependencies(selectedApp)
        this.trigger(this.state);
      }
    },

    onUpdateStateIndicators: function(component){
      var state = this.state.appTopology;
      var app = state.application.selectedApp.appServiceTemplates;

      switch (component.componentType) {
        case 'service':
          var service = _.filter(app, (service, i)=>{
            var serviceName = service.name || service.appServiceName;
            service.serviceId = i;
            return serviceName === component.serviceName;
          })[0];

          //===================================//
          if(service)
            state.application.selectedApp.appServiceTemplates[service.serviceId].state = component.state;
          //===================================//
          break;
        case 'dependency':
        var dependency = _.filter(state.application.allDependencies, dependency=>{
          return dependency.name === component.bindName;
        })[0];
        if(dependency){
          var serviceIndex = dependency.relatedServiceId[0];
          var dependencyIndex = dependency.index;
          if(state.application.selectedApp.appServiceTemplates){
            state.application.selectedApp.appServiceTemplates[serviceIndex].dependencies[dependencyIndex].state = component.state;
          }
        }
          break;
        default:

      }
      this.trigger(this.state)
    },

    setAllServicesAndAllDependencies: function(selectedApp){
      var allServices = this.getAllServicesByApp(selectedApp),
          state = this.state.appTopology;

      state.application.allServices = allServices;

      state.application.allDependencies = this.getAllDependenciesInServiceArray(allServices);

      this.trigger(this.state);
    },

    // get all services in a given application
    // and add serviceId object to each service
    getAllServicesByApp: function(selectedApp){

        var state = this.state.appTopology;
        var services = selectedApp.appServiceTemplates.map((service, index)=>{
            service.isActive = true;
            service.serviceId = index;
            service.img = Config.fixImgUrl(service.img);
            service.componentType = state.componentTypes.service;
            service.x = this.getXValue(selectedApp.appServiceTemplates.length, index);
            return service;
        });

        return services;
    },

    //get all dependencies in
    //selected application by allServices
    getAllDependenciesInServiceArray: function(services){

        var state = this.state.appTopology;
        var dependencies = [];
        var filteredDependencies = [];

        //iterate all services and push to dependencies array
        //fix img url
        services.forEach((service, serviceIndex)=>{

            service.dependencies.forEach((dependency, dependencyIndex)=>{
                dependency.isActive = true;
                dependency.index = dependencyIndex;
                dependency.componentType = state.componentTypes.dependency;
                dependency.relatedServiceId = [];
                dependency.relatedServiceId.push(serviceIndex);
		            dependency.img = Config.fixImgUrl(dependency.img);
                dependencies.push(dependency);
            });
        });

        //filter dependencies array by name and type
        //and push to filteredDependencies array
        dependencies.forEach((dependency, i)=>{

            var obj = { name: dependency.name, type: dependency.type }

            if(_.find(filteredDependencies, obj) == undefined){
                filteredDependencies.push(dependency)
            }
        });

        //set dependencies x and index values
        filteredDependencies.forEach((dependency, index)=>{
            dependency.x = this.getXValue(filteredDependencies.length, index);
            dependency.index = index;
        });

        //push related service id to each dependency object
        var handledDependencies = this.populateRelatedServiceId(filteredDependencies, dependencies);

        return handledDependencies;
    },

    populateRelatedServiceId: function(filteredDependencies, dependencies){

        dependencies.forEach((dependency)=>{
            var dependencyObj = {type: dependency.type, name: dependency.name};

            filteredDependencies.forEach((filteredDependency)=>{
                var filteredDependencyObj = {type: filteredDependency.type, name: filteredDependency.name};
                if(_.isEqual(dependencyObj, filteredDependencyObj)){
                  dependency.relatedServiceId.forEach((serviceId)=>{
                      if(filteredDependency.relatedServiceId != serviceId){
                          filteredDependency.relatedServiceId.push(serviceId);
                      }
                  })
                }
            });

        });
        return filteredDependencies;
    },

    onSetSelectedElementActiveState: function(isActive){
      this.state.appTopology.selectedElement.isActive = isActive;
      this.trigger(this.state);
    },

    onSetSelectedElementVersion: function(version){
      this.state.appTopology.selectedElement.version = version;
      this.trigger(this.state);
    },

    onSetSelectedElementPlan: function(plan, serviceIndex, dependencyIndex){
      this.state.appTopology.selectedElement.plan = plan;
      this.trigger(this.state);
    },

    onInitializeSelectedElement(element) {
      const state = this.state.appTopology;
      state.services.selectedService.service = {};
      state.services.selectedService.relatedDependencies = [];
      state.dependencies.selectedDependency.dependency = {};
      state.dependencies.selectedDependency.relatedServices = [];
      this.state.appTopology.selectedElement = {};
      this.trigger(this.state);
    },

    onUserClickOnAppTopologyService: function(e){
        var state = this.state.appTopology;
        state.services.selectedService.service = e;
        var relatedDependencies = this.getRelatedDependenciesByServiceId(e.serviceId);
        state.services.selectedService.relatedDependencies = relatedDependencies;
	      state.dependencies.selectedDependency.relatedServices = [];

        var position = {
          top: state.params.servicesHeight + 550 ,
          left: e.x + 470 - state.services.translate
        }

        if(this.isLocationSharedImages()){
             var position = {
               top: state.params.servicesHeight + 70,
               left: e.x + state.params.boxSize*2 - state.services.translate
             }
           }

        state.selectedElement = e;
        DevWizardActions.showConfigTopologyTooltip(position);
        this.trigger(this.state);
    },

    onUserClickOnAppTopologyDependency: function(e){

        var state = this.state.appTopology;
        state.dependencies.selectedDependency.dependency = e;
        var relatedServices = this.getRelatedServicesByIdList(e.relatedServiceId);

        state.dependencies.selectedDependency.relatedServices = relatedServices;
	      state.services.selectedService.relatedDependencies = [];

        var position = {
          top: state.params.dependenciesHeight + 580,
          left: e.x + 462 - state.dependencies.translate
        }

        if(this.isLocationSharedImages()){
          var position = {
            top: state.params.dependenciesHeight + 90,
            left: e.x + state.params.boxSize*2 + 30 - state.dependencies.translate
          }
        }

        state.selectedElement = e;
        DevWizardActions.showConfigTopologyTooltip(position);
        this.trigger(this.state);
    },

    isLocationSharedImages: function(){
      return Config.getCurrentHash().module === DevNavigationOptions.module &&
             Config.getCurrentHash().location === DevNavigationOptions.sharedImage.location.substring(1)
    },

    //get related dependencies by service id
    getRelatedDependenciesByServiceId: function(selectedServiceId){

        var relatedDependenices = [];

        var dependencies = this.state.appTopology.application.allDependencies.forEach((dependency)=>{

            dependency.relatedServiceId.forEach((serviceId)=>{

                if(serviceId == selectedServiceId){
                    relatedDependenices.push(dependency);
                }
            });

        });

        return relatedDependenices;
    },

    //get related services by id list
    getRelatedServicesByIdList(relatedServicesIds){
        var services = this.state.appTopology.application.allServices;
        var relatedServices = [];
        if(relatedServicesIds){
          services.forEach((service)=>{
              relatedServicesIds.forEach((id)=>{
                  if(service.serviceId == id){
                      relatedServices.push(service);
                  }
              })
          });
        }

        return relatedServices;
    },

	/********************************************
	 * handle dependencies container transition *
	 ********************************************/
    onSetDependenciesContainerTransition: function(dependenciesContainerWidth){
		var state = this.state.appTopology;

        if(dependenciesContainerWidth > state.params.containerWidth){
		      state.dependencies.translate =
                  state.application.allDependencies[0].x - state.params.padding;
        }

		this.trigger(this.state);
    },

    onSetDependenciesTranslate: function(translate){
        var newValue = this.state.appTopology.dependencies.translate + translate,
            state = this.state.appTopology,
            firstDependency = state.application.allDependencies[0],
            lastDependency = _.last(state.application.allDependencies),
            padding = state.params.padding;

        //scroll left boundery
        if(newValue + state.params.padding >= firstDependency.x){
            //scroll right boundery
            if(firstDependency.x - padding + newValue <= 0){
                this.state.appTopology.dependencies.translate = newValue;
                this.trigger(this.state);
            }
        }
    },

	/********************************************
	 * handle services container transition *
	 ********************************************/

    onSetServicesContainerTransition: function(servicesContainerWidth){
		var state = this.state.appTopology;

        if(servicesContainerWidth > state.params.containerWidth){
		      state.services.translate =
                  state.application.allServices[0].x - state.params.padding;
        }

		this.trigger(this.state);
    },

    onSetServicesTranslate: function(translate){
        var newValue = this.state.appTopology.services.translate + translate,
            state = this.state.appTopology,
            firstService = state.application.allServices[0],
            padding = state.params.padding;

        //scroll left boundery
        if(newValue + state.params.padding >= firstService.x){
            //scroll right boundery
            if(firstService.x - padding + newValue <= 0){
                this.state.appTopology.services.translate = newValue;
                this.trigger(this.state);
            }
        }
    },

    //calculate the x value
    //so elements are spreaded evenly
    //from the container's center
    getXValue: function(arraySize, index){

        var state = this.state.appTopology;
        var params = state.params;

        return (params.containerWidth/2 - (params.boxSize * arraySize +
                                                   (arraySize - 1) * params.space) / 2) +
                                                   (index * params.boxSize)+(index * params.space);
    },

});

export default Store;
