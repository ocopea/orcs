// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import $ from 'jquery';
// actions
import ProdAppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import PropActions from '../../../module-production/js/actions/actions.js';
import DashboardActions from '../actions/dev-dashboard-actions.js';
import SharedActions from '../../../shared-actions.js';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import DevActions from '../actions/dev-actions.js';

// stores
import ProdWizardStore from '../../../module-production/js/stores/wizard/_wizard-main-store.js';
import ProdAppTopologyStore from '../../../module-production/js/stores/wizard/appTopologyStore.js';
import DeployingProgressStore from './dev-deploying-progress-store.js';

// helpers
import Config from '../../../module-production/js/config.js';
import devNavigationOptions from '../data/devNavigationOptions.js';
import TopologyParser from '../../../shared-components/js/topology-data-parser.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import stateOptions from '../data/deploying-state-options.js';

//  assets
import MockLoadingImages from '../data/mockLoadingImages.js';

var timer;

var DevDashboardMainStore = Reflux.createStore({

  listenables: [DashboardActions],

  state: {

    loadingImage: {
      isMock: true,
      image: {}
    },

    selectedInstance: {},

    confirmDisposeImageDialog:{
      isRender: false
    },

    topologyData: [],

    websockets: null,
    fetchWS: true

  },

  getInitialState: function(){
    return this.state;
  },

  init: function(){
     //populate loading images
     if(this.state.loadingImage.isMock){
       this.state.loadingImage.images = MockLoadingImages;
     }else{
       console.log('fetch loading images')
     }

  },

  onSetLoadingImage: function(image){
    this.state.loadingImage.image = image;
    this.trigger(this.state);
  },

  onStopApp: function(appInstanceId){
    var that = this;
    var options = {
      url : APISERVER+"/hub-web-api/commands/stop-app",
      method: 'POST',
      contentType: "application/json",
      data: {
        appInstanceId: appInstanceId
      }
    };
    Config.request(options, function(response){
      console.log(response)
      DevActions.getAppInstances();

      SharedActions.navigate({
        module: DevNavigationOptions.module,
        location: DevNavigationOptions.main.location,
        subLocation: ""
      });

    },function(error){
      PropActions.showErrorDialog(error.statusText)
    });
    that.state.confirmDisposeImageDialog.isRender = false;
  },

  onShowConfirmDisposeImageDialog: function(){
    this.state.confirmDisposeImageDialog.isRender = true;
    this.trigger(this.state);
  },

  onHideConfirmDisposeImageDialog: function(){
    this.state.confirmDisposeImageDialog.isRender = false;
    this.trigger(this.state);
  },

  onUserClickOnMainScreenCard: function(instance){

    var that = this;
    var options = {
      url: APISERVER+`/hub-web-api/app-instance/${instance.id}`,
      method: 'GET'
    }
    Config.request(options, function(response){

      if(response.state.toUpperCase() === stateOptions.running.toUpperCase()){
        that.state.selectedInstance = instance;
        that.trigger(that.state);
        DashboardActions.setSelectedInstance(instance);

				SharedActions.navigate({
					module: DevNavigationOptions.module,
					location: DevNavigationOptions.dashboard.location,
					subLocation: "/"+instance.id
				});

      }else{
        DevActions.goToDeployingProgress(instance);
        SharedActions.navigate({
  				module: DevNavigationOptions.module,
  				location: DevNavigationOptions.deployingProgress.location,
  				subLocation: "/"+instance.id
  			});
        DevDashboardActions.setSelectedInstance(instance)
      }
      that.trigger(that.state);
    },function(error){
      console.log(error)
    });

  },

  onSetSelectedInstance: function(instance){    
    const that = this;
    if(instance !== undefined){
      // get websocket address
      if(this.state.fetchWS){
        (function foo(){
          let timer = setTimeout(foo, 1000);
          let condition = that.state.websockets;
          // let condition = that.state.websockets && that.state.websockets.length > 0;
          that.fetchWSEntry(instance);

          if(condition){
            clearTimeout(timer);
            // console.log('websockets: ', that.state.websockets);
            return
          }
        })();
      }
      // this.requestState(instance);
      // this.state.selectedInstance = instance;
    }

    if(instance !== undefined && this.shouldFetchState(instance.state)){
      this.getInstanceState(instance);
      this.state.selectedInstance = instance;
      this.trigger(this.state);
    }
  },

  getInstanceState: function(instance){
    this.requestState(instance);
    timer = setTimeout(this.requestState.bind(this, instance), 8000);
  },

  fetchWSEntry: function(selectedInstance){
    var that = this;
    var instanceId = selectedInstance.id;
    var options = {
      url: `${APISERVER}/hub-web-api/app-instance/${selectedInstance.id}/logs`,
      method: 'GET'
    }

    Config.request(options, function(response){
      that.state.websockets = response;
      that.trigger(that.state);
    }, function(error){ console.log(error) })

  },

  requestState: function(instance){
    var topologyState = ProdAppTopologyStore.state.appTopology,
        selectedApp = topologyState.application.selectedApp,
        allApps = ProdWizardStore.state.allApplications;

    var instanceStateOptions = {
      url: `${APISERVER}/hub-web-api/app-instance/${instance.id}/state`,
      method: 'GET'
    }
    var firstTime = false;
    var that = this;
    Config.request(instanceStateOptions, function(response){
      var shouldFetch = that.shouldFetchState(instance);
      var parsedApp = that.parseTopologyData(response);
      that.state.topologyData = parsedApp;
      // that.updateAllIndicators(parsedApp);
      if(_.isEmpty(selectedApp)){
        ProdAppTopologyActions.setSelectedApp(
          parsedApp
          // allApps[that.state.selectedInstance.appTemplateId] //parsedApp
        );
      }
      that.trigger(that.state);
      if(!shouldFetch){
        that.onStopFetchingState();
      }
    }, function(error){
      console.log(error)
    });

  },
  // updateAllIndicators: function(app){
  //   var services = app.appServiceTemplates || app.appCopyDetails;
  //   var dependencies = app.serviceCopyDetails ||
  //                      app.appServiceTemplates.map((service, i)=>service.dependencies)[0];
  //
  //   var fromServer = {services: services,
  //                     dependencies: dependencies};
  //   // console.log(fromServer)
  //   _.forEach(fromServer.services, service=>{
  //     ProdAppTopologyActions.updateStateIndicators(service);
  //   });
  //   _.forEach(fromServer.dependencies, dependency=>{
  //     ProdAppTopologyActions.updateStateIndicators(dependency);
  //   });
  //
  // },
  shouldFetchState: function(state){
    switch (state.state ? state.state.toUpperCase() || state.toUpperCase(): null) {
      case stateOptions.running.toUpperCase():
      case stateOptions.error.toUpperCase():
        return false
        break;
      default:
        return true
    }
  },

  onStopFetchingState: function(){
    clearInterval(timer);
  },

  parseTopologyData: function(data){
    // console.log(data)
    var parsedData = {},
        appCopyDetails = {},
        dataServices = {};

    data.appServices.forEach(service=>{
      service.imageName = service.serviceName;
      appCopyDetails[service.imageName] = service;
    });

    data.dataServices.forEach(dataService=>{
      dataService.dsbName = dataService.bindName;
      dataServices[dataService.bindName] = dataService;
    })

    parsedData.appCopyDetails = appCopyDetails;
    parsedData.serviceCopyDetails = dataServices;

    return TopologyParser.parse(parsedData);
  }

});

export default DevDashboardMainStore;
