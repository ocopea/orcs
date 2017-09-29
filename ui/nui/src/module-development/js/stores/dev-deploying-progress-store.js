// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import DevActions from '../actions/dev-actions.js';
import AppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import AppTopologyStore from '../../../module-production/js/stores/wizard/appTopologyStore.js';
import DeployingProgressActions from '../actions/deploying-progress-actions.js';
import TopologyParser from '../../../shared-components/js/topology-data-parser.js';
import Config from '../../../module-production/js/config.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import stateOptions from '../data/deploying-state-options.js';
import MockLogsFilters from '../data/mock-logs-filters.js';
import _ from 'lodash';
import $ from 'jquery';


var DeployingProgressStore = Reflux.createStore({

  listenables:[DevActions, DeployingProgressActions],

  state: {
    selectedInstance: {},
    instanceState: "",
    instanceStatus: {
      allElements: [],
      deployedElements: [],
      failedElements: [],
      stateMessage: ""
    },
    logs: {
      isMock: true,
      allFilters: [],
      selectedFilters: [],
      allLogs: [],
      filteredLogs: [],
      keyword: '',
      data: {}
    },
    dialogSavedSuccessfuly: {
      isRender: false
    },
    dialogFailCreating: {
      isRender: false
    }

  },

  getInitialState: function(){
    return this.state;
  },

  init: function(){
    if(this.state.logs.isMock){
      this.state.logs.filters = MockLogsFilters;
      this.trigger(this.state);
    }
  },

  /********
   * LOGS *
   ********/
  onSetAllLogsFilters: function(allFilters){
    this.state.logs.allFilters = [...allFilters];
    allFilters.forEach(filter=>{
      this.state.logs.filters[filter] = filter;
    });
    this.state.logs.selectedFilters = allFilters;
    this.trigger(this.state);
  },

  onSeeAllLogsFilters: function(){
    this.onSetAllLogsFilters(this.state.logs.allFilters);
    this.state.logs.filteredLogs = this.getFilterLogs();
    this.trigger(this.state);
  },

  onReceiveLog: function(log){
    this.state.logs.allLogs.push(log);
    this.state.logs.filteredLogs = this.getFilterLogs();
    this.trigger(this.state);
  },

  onToggleLogsFilter: function(filter){
    let selectedFilters = this.state.logs.selectedFilters;
    let hasFilter = selectedFilters.indexOf(filter) > -1;

    if(hasFilter){
      _.remove(selectedFilters, f=>{return f===filter})
      this.state.logs.selectedFilters = selectedFilters;
    }else{
      selectedFilters.push(filter);
      this.state.logs.selectedFilters = selectedFilters;
    }
    this.state.logs.filteredLogs = this.getFilterLogs();
    this.trigger(this.state);
  },

  onAddFilter: function(filter){
    if(this.state.logs.selectedFilters.indexOf(filter) === -1){
      this.state.logs.selectedFilters.push(filter);
      this.trigger(this.state);
    }
  },

  getFilterLogs: function(){
    let filteredLogs = [];
    this.state.logs.allLogs.filter(log=>{
      if(this.shouldPushLog(log)){
        let handledLog = this.objectifyLog(log, null);
        filteredLogs.push(handledLog);
      }
    });
    return filteredLogs;
  },

  onUserSearchLogs: function(text){
    this.state.logs.keyword = text;
    this.state.logs.filteredLogs = this.getFilterLogs();
    this.highlightKeyWord(text);
    this.trigger(this.state);
  },

  highlightKeyWord: function(text){
    let filteredLogs = this.state.logs.filteredLogs;
    filteredLogs.forEach(log=>{
      log = this.objectifyLog(log, text);
    });
  },

  objectifyLog: function(log){
    let logObj = {};
    let text = this.state.logs.keyword.toUpperCase();
    let msg = log.message.toUpperCase();

    if(text && msg.indexOf(text) > -1){
      let startIndex = msg.indexOf(text);
      let endIndex = startIndex + text.length;
      let beginning = msg.substring(0, startIndex);
      let heighlight = msg.substring(startIndex, endIndex);
      let ending = msg.substring(endIndex);

      let obj = {
        beginning: log.message.substring(0, startIndex),
        heighlight: log.message.substring(startIndex, endIndex),
        ending: log.message.substring(endIndex),
        status:'success'
      };

      logObj = {...log, filter :obj};
    }else {
      var obj = {
        beginning: log.message,
        heighlight: '',
        ending: '',
        status:'success'
      };
      logObj = {...log, filter: obj};
    }
    return logObj;
  },

  /****************
   * LOGS- FILTERS *
   ****************/
  logsFilterText: function(logMsg){
    if(logMsg){
      let cleanMsg = logMsg.toUpperCase();
      let cleanKeyWord = this.state.logs.keyword.toUpperCase();
      return cleanMsg.indexOf(cleanKeyWord) > -1;
    }
    return false;
  },

  logsFilterTag: function(tags){
    let logs = [];
    let isValid = false;
    if(tags){
      tags.forEach(tag=>{
        if(this.state.logs.selectedFilters.indexOf(tag) > -1){
          isValid = true;
        }
      });
    }
    return isValid;
  },

  // END LOGS- FILTERS
  // =====================================

  shouldPushLog: function(log){

    return log.tags.length === 0 ||
           this.logsFilterTag(log.tags) &&
           this.logsFilterText(log.message);
  },

  onSetAllFilters: function(websockets){
    let uniqTags = [];
    _.forEach(websockets, (socket, index)=>{
      if(index === 0){
        uniqTags = [...new Set(socket.tags)];
      }else{
        uniqTags = [...new Set(uniqTags.concat(socket.tags))]
      }
    });
    this.onSetAllLogsFilters(uniqTags);
  },

  onClearLogs: function(){
    this.state.logs.allLogs = [];
    this.state.logs.filteredLogs = [];
    this.state.logs.keyword = '';
    this.state.logs.allFilters = [];
    this.state.logs.selectedFilters = [];
    this.trigger(this.state);
  },

  // END LOGS
  // =====================================

  onGoToDeployingProgress: function(selectedInstance){
    this.state.selectedInstance = selectedInstance;
    this.state.isRender = true;
    this.trigger(this.state);
  },

  onInitializeSelectedInstance(){
    this.state.selectedInstance = {};
    this.trigger(this.state);
  },

  onCheckState: function(id){

    var that = this;
    this.state.appInstanceId = id;
    this.state.instanceState = undefined;

    timer = setInterval(function(){
      that.requestState(id);
      that.handleStateChange(that.state.instanceState);
    }, 1000)
  },

  requestState: function(id){
    var that = this;

    var options = {
      url : APISERVER+`/hub-web-api/app-instance/${id}/state`,
      method: 'GET'
    }

    Config.request(options, function(response){
      that.onInitateInstanceStatus();
      that.populateInstanceStatus(response.appServices, response.dataServices);

      var app = {
        appCopyDetails: response.appServices,
        serviceCopyDetails: response.dataServices,
        ...response
      };

      that.updateStateIndicators(app);
      that.state.instanceState = response.state;
      that.state.instanceStatus.stateMessage = response.stateMessage;
      that.state.selectedInstance = response;
      that.trigger(that.state);

      let topologySelectedApp = AppTopologyStore.state.appTopology.application.selectedApp;


      // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! //
      if(_.isEmpty(topologySelectedApp)){
        //AppTopologyActions.setSelectedApp(TopologyParser.parse(response));
      }
      // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! //

    }, function(error){
      clearInterval(timer);
      return
    });
  },

  updateAllIndicators: function(app){

    var fromServer = {services: app.appCopyDetails,
                      dependencies: app.serviceCopyDetails};

    _.forEach(fromServer.services, service=>{
      AppTopologyActions.updateStateIndicators(service);
    });
    _.forEach(fromServer.dependencies, dependency=>{
      AppTopologyActions.updateStateIndicators(dependency);
    });

  },

  updateStateIndicators: function(app){

    var fromServer = {services: app.appCopyDetails,
                      dependencies: app.serviceCopyDetails}
    var fromClient = {services: this.state.selectedInstance.appServices,
                      dependencies: this.state.selectedInstance.dataServices}
    var result = {};

    for( var i = 0; i < fromServer.services.length; i++){
      var clientServices = fromClient.services || [];
      var clientService = clientServices.length > 0 ? clientServices[i] : {};
      var serverServices = fromServer.services || [];
      var serverService = serverServices.length > 0 ? serverServices[i] : {};

      if(serverService && clientService){
        if(serverService.state !== clientService.state){
          AppTopologyActions.updateStateIndicators(serverService);
          return true;
        }
      }else{return false}
    }

    for( var i = 0; fromServer.dependencies.length; i++){
      var clientDependencies = fromClient.dependencies || [];
      var clientDependency = clientDependencies.length > 0 ? clientDependencies[i] : {};
      var serverDependencies = fromServer.dependencies || [];
      var serverDependency = serverDependencies.length > 0 ? serverDependencies[i] : {};

      if(serverDependency && clientDependency){
        if(serverDependency.state !== clientDependency.state){
          AppTopologyActions.updateStateIndicators(serverDependency);
          return true;
        }
      }else{return false}
    }
    return false;
  },

  appendComponentType: function(componentsArray, type){
    _.forEach(componentsArray, o=>{o.type = type})
  },

  populateInstanceStatus: function(services, dependencies){
    var that = this;
    this.appendComponentType(services, 'service');
    this.appendComponentType(dependencies, 'dependency');

    this.state.instanceStatus.allElements = this.getAllElements(services, dependencies);

    this.state.instanceStatus.allElements.forEach(element=>{

      if(element.componentType === 'service'){
        if(element.state.toUpperCase() === stateOptions.deployed.toUpperCase()){
          that.state.instanceStatus.deployedElements.push(element)
        }else{
          that.state.instanceStatus.failedElements.push(element)
        }
      }else{
        if(element.componentType === 'dependency'){
          if(element.state.toUpperCase() === stateOptions.bound.toUpperCase()){
            that.state.instanceStatus.deployedElements.push(element)
          }else{
            that.state.instanceStatus.failedElements.push(element)
          }
        }
      }
    });

  },

  getAllElements: function(services, dependencies){

    var elements = [];
    services.forEach(service => {
      service.componentType = 'service';
      elements.push(service);
    });
    dependencies.forEach(dependency=>{
      dependency.componentType = 'dependency';
      elements.push(dependency);
    });

    return elements;
  },

  getInstancebyId: function(id){
    var that = this;
    var options = {
      url: `${APISERVER}/hub-web-api/app-instance/${id}`,
      method: 'GET'
    }
    Config.request(options, function(response){

      if(response.webEntryPointURL){
        DevActions.getAppInstances();
        that.showDialogSavedSuccessfuly();
        clearInterval(timer);
      }
    }, function(error){})
  },

  handleStateChange: function(state){
    var selectedInstance = this.state.selectedInstance;
    var app = {
      appCopyDetails: selectedInstance.appServices,
      serviceCopyDetails: selectedInstance.dataServices,
      ...selectedInstance
    };

    switch (state ? state.toUpperCase() : '') {
      case stateOptions.running.toUpperCase():
        this.getInstancebyId(this.state.selectedInstance.id);
        break;
      case stateOptions.errorStopping.toUpperCase():
      case stateOptions.error.toUpperCase():
        var isDone = this.checkDependenciesState();
        if(isDone){
          clearInterval(timer);
        }
        break;
      case stateOptions.deploying.toUpperCase():
        this.state.dialogSavedSuccessfuly.isRender = false;
        break;
    }
    this.state.instanceState = state !== undefined  ? state : null;
    this.trigger(this.state);
    this.updateAllIndicators(app);

  },

  checkDependenciesState: function(){
    var dataServices = this.state.selectedInstance.dataServices;
    var doneCheck = [];
    var that = this;
    dataServices.forEach(dataService=>{
      var dataServiceState = dataService.state.toUpperCase();
      if(that.isDone(dataServiceState)){
          doneCheck.push(dataService);
      }
    });
    return doneCheck.length === dataServices.length;
  },

  isDone: function(dataServiceState){
    return dataServiceState === stateOptions.bound.toUpperCase() ||
           dataServiceState === stateOptions.errorbinding.toUpperCase() ||
           dataServiceState === stateOptions.errorCreateing.toUpperCase()
  },

  onHideDialogSavedSuccessfuly: function(){
    this.state.dialogSavedSuccessfuly.isRender = false;
    this.trigger(this.state);
  },

  onInitateInstanceStatus: function(){
    this.state.instanceState = "";
    this.state.instanceStatus = {
      allElements: [],
      deployedElements: [],
      failedElements: [],
      stateMessage: ""
    }
  },

  showDialogSavedSuccessfuly: function(){
    this.state.dialogSavedSuccessfuly.isRender = true;
    this.trigger(this.state);
  }

});
var timer;
export default DeployingProgressStore;
