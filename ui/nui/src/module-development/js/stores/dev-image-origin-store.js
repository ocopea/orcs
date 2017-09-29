// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import MockData from '../data/dashboard-mock-image-origin.js';
import DevStore from './dev-store.js';
import DevWizardStore from './dev-wizard-store.js';
import SharedStore from '../../../shared-store.js';
import SharedActions from '../../../shared-actions.js';
import _ from 'lodash';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import DevActions from '../actions/dev-actions.js';


var DevImageOriginStore = Reflux.createStore({

  listenables: [DevDashboardActions, DevActions],

  state: {
    isMock: false,
    images: [],
    filteredImages: [],
    selectedInstance: {},
    isListRender: false,
    selectedImage: {}
  },

  getInitialState: function(){
    return this.state;
  },

  init: function(){
    if(this.state.isMock){
      //mock data arrives from component image-origin action
    }else{
      //fech image origin
    }
  },

  onReceiveSavedImages: function(images){
    var parsedImages = this.appendShareLinkToImage(images);
    this.state.images = parsedImages;
    this.state.filteredImages = parsedImages;
    this.trigger(this.state);
  },

  onGetSavedImageById: function(id){
    var image = this.state.images.filter(image=>{return image.id === id})[0];
    this.state.selectedImage = image;
    this.trigger(this.state);
    return image
  },

  appendShareLinkToImage: function(images){
    var isDev = APISERVER.length > 0;
    return _.forEach(images, image=>{
      image.shareURL = !isDev ?
        `http://${window.location.host}/hub-web-api/html/nui/index.html#development/shared-image/${image.id}/shared-by/${SharedStore.state.loggedInUser.id}`
        :
        `http://${window.location.host}/#development/shared-image/${image.id}/shared-by/${SharedStore.state.loggedInUser.id}`
    });
  },

  onFilterImages: function(imageID){
    var filteredImages = this.state.images.filter(image=>{
      return image.id === imageID
    })
    this.state.filteredImages = filteredImages;
    this.trigger(this.state);
  },

  onInitiateOriginalImages: function(){
    this.state.filteredImages = this.state.images;
    this.trigger(this.state);
  },

  onReceiveUsers: function(users){
    var usersKeys = Object.keys(users);

    if(this.state.isMock){
      usersKeys.forEach((userKey, index)=>{
        MockData[index].createdByUserId = users[userKey].id
      });
      this.state.images = MockData;
    }else{
      this.state.images = DevWizardStore.state.image.savedImageData;
    }
    this.state.filteredImages = this.state.images;

    this.trigger(this.state);
  },

  onReceiveAppTemplates: function(appTemplates){
    var appTemplatesKeys = Object.keys(appTemplates)
    var counter = 0;
    MockData.forEach((image, index)=>{
      if(appTemplatesKeys[counter] == undefined){
        counter = 0;
      }
      image.appTemplateId = appTemplatesKeys[counter];
      counter++;
    })
  },

  onShowImageOriginList: function(){
    this.state.isListRender = true;
    this.trigger(this.state);
  },

  onHideImageOriginList: function(){
    this.state.isListRender = false;
    this.trigger(this.state);
  },

  onSortImageOriginByUserName: function(sortBy){
    this.state.images.sort(function(a, b){
      var paramA = DevStore.state.users[a.createdByUserId].firstName.toUpperCase();
      var paramB = DevStore.state.users[b.createdByUserId].firstName.toUpperCase();
      return (paramA < paramB) ? -1 : (paramA > paramB) ? 1 : 0;
    });
    SharedActions.sortTable(sortBy)
    this.trigger(this.state);
  },

  onSortImageOrignByImageName: function(sortBy){
    this.state.images.sort(function(a, b){
      var paramA = a.name.toUpperCase();
      var paramB = b.name.toUpperCase();
      return (paramA < paramB) ? -1 : (paramA > paramB) ? 1 : 0;
    });
    SharedActions.sortTable(sortBy)
    this.trigger(this.state);
  },

  onSortImageOriginByDate: function(sortBy){
    this.state.images.sort(function(a, b){
      var paramA = a.dateCreated;
      var paramB = b.dateCreated;
      return (paramA < paramB) ? -1 : (paramA > paramB) ? 1 : 0;
    });
    SharedActions.sortTable(sortBy)
    this.trigger(this.state);
  },

  onSetSelectedInstance: function(appInstance){
    this.state.selectedInstance = appInstance;
    this.trigger(this.state);
  },

});

export default DevImageOriginStore;
