// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Config from '../../../module-production/js/config.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import TopologyParser from '../../../shared-components/js/topology-data-parser.js';
import _ from 'lodash';

//stores
import DevWizardStore from './dev-wizard-store.js';
import DevStore from './dev-store.js';
import ProdWizardStore from '../../../module-production/js/stores/wizard/_wizard-main-store.js';
import SharedStore from '../../../shared-store.js';

//actions
import DevWizardActions from '../actions/dev-wizard-actions.js';
import AppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import SharedImageActions from '../actions/dev-shared-image-actions.js';
import DevActions from '../actions/dev-actions.js';
import SharedActions from '../../../shared-actions.js';

// mock data
import MockData from '../data/shared-image-mock-selects-data.js';

var SharedImageStore = Reflux.createStore({

  listenables: [DevDashboardActions, DevActions, AppTopologyActions, SharedImageActions],

  state: {
    sharedImage: {},
    imageDetails: {},
    users: {},
    sharedByUserID: "",
    sharedByUser: {},
    isMockSelects: false,
    projectName: "",
    sites: {
      options: [],
      selected: {}
    },
    purposes: {
      options: [],
      selected: {}
    },
    spaces: {
      options: [],
      selected: {}
    },
    validation: {
      msg: "",
      isValid: false
    }
  },

  getInitialState: function(){
    return this.state;
  },

  init: function(){
      this.onGoToSharedImage(Config.getCurrentHash().subLocation);
      this.populateSelects();
  },

  onDeploySharedImage: function(){
    var projectName = this.state.projectName,
        instance = this.parseInstance();

    var isValid = this.validate();
    if(isValid){
      // console.log(this.state.imageDetails)
      DevWizardActions.deploySavedImage(instance);
    }else{
      console.log(this.state.validation.msg)
    }
  },

  validate: function(){
    var isProjectNameEntered = this.projectNameValidation();
    return isProjectNameEntered;
  },

  projectNameValidation: function(){
    if(this.state.projectName.trim().length === 0){
      this.state.validation.isValid = false;
      this.state.validation.msg = "please enter project name";
      return false;
    }else{
      this.state.validation.isValid = true;
      this.state.validation.msg = "";
      return true;
    }
    this.trigger(this.state);
  },

  parseInstance: function(instance){
    return this.state.imageDetails;
  },

  onChangeProjectName: function(projectName){
    this.state.projectName = projectName;
    this.validate();
    this.trigger(this.state);
    DevWizardActions.userChangedAppInstanceName(projectName)
  },

  onSetSelectedPurpose: function(selectedPurpose){
    this.state.purposes.selected = selectedPurpose;
    this.trigger(this.state);
  },

  onSetSelectedSpace: function(selectedSpace){
    this.state.spaces.selected = selectedSpace;
    this.trigger(this.state);
  },

  onReceiveSites: function(sites){
    this.state.sites.selected = sites[0];
    DevWizardActions.setSelectedSite(sites[0].id);
    this.state.sites.options = sites;

    var parsedSpaces = _.map(this.state.sites.selected.spaces, space=>{return {text:space}});
    this.state.spaces.options = parsedSpaces;
    this.trigger(this.state);
  },

  populateSelects: function(){
    if(this.state.isMockSelects){
      this.state.sites.options = MockData.sites;
      this.state.purposes.options = MockData.purposes;
      this.state.spaces.options = MockData.spaces;
    }else{
      // fetch data by image id
      // this.state.sites.options = MockData.sites;
      this.state.purposes.options = MockData.purposes;
      this.state.spaces.options = MockData.spaces;
    }
  },

  onGoToSharedImage: function(id, sharedByUserID){

    var that = this;

    if(Config.getCurrentHash().module ===
          DevNavigationOptions.module &&
       Config.getCurrentHash().location ===
          DevNavigationOptions.sharedImage.location.substring(1))
    {

      this.state.sharedByUserID = sharedByUserID;
      var options = {
        url: APISERVER+"/hub-web-api/test-dev/saved-app-images",
        method: 'GET'
      }

      Config.request(options, function(response){
        var sharedImage = _.filter(response, o=>{
          return o.id === id;
        });
        that.state.sharedImage = sharedImage[0];

        that.getImageDetails(id, sharedByUserID);
        that.trigger(that.state);

      }, function(error){})
       this.trigger(this.state);
     }
  },

  onReceiveUsers: function(users){
    this.state.sharedImage.sharedByUser = users[this.state.sharedByUserID];
    this.state.appTemplates = ProdWizardStore.state.allApplications;

    this.trigger(this.state);
  },

  getImageDetails: function(id, sharedByUserID){

    var that = this;
    var options = {
      url: APISERVER+`/hub-web-api/test-dev/saved-app-images/${id}/details`,
      method: 'GET'
    }

    Config.request(options, function(response){
      var app = TopologyParser.parse(response);
      app.id = response.appTemplateId;
      DevWizardActions.userSelectedApp(app);
      that.state.imageDetails = TopologyParser.parse(response);

      AppTopologyActions.setSelectedApp(that.state.imageDetails);
      DevWizardActions.setTopologyActiveElements(that.state.imageDetails);
      DevWizardActions.setSelectedSite(DevWizardStore.state.config.sitesArray[0].id);
      that.trigger(that.state);
    },function(error){})
  }


});

export default SharedImageStore;
