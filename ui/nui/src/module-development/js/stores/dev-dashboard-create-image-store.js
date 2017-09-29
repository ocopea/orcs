// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import DevStore from './dev-store.js';
import ProdActions from '../../../module-production/js/actions/actions.js';
import DevActions from '../actions/dev-actions.js';
import Config from '../../../module-production/js/config.js';
import stateOptions from '../data/deploying-state-options.js';
import _ from 'lodash';

var DevDashboardCreateImageStore = Reflux.createStore({

  listenables:[DevDashboardActions, DevActions],

  state:{
    selectedInstance: {},

    dialog:{
      createImage:{
        isRender: false,
        name: "",
        tags: [],
        comment: ""
      }
    },

    savedAppImages: [],

    savedImage: {},

    allImageTags: []
  },

  init: function(){

    //fetch all saved app images
    //and populate all tags
    this.fetchSavedImages();
  },

  onSetSelectedInstance: function(instance){
    this.state.selectedInstance = instance;
    this.trigger(this.state);
  },

  fetchSavedImages: function(){
    var that = this;
    var options = {
      url: APISERVER + "/hub-web-api/test-dev/saved-app-images",
      method: 'GET'
    }

    Config.request(options, function(response){
      that.state.savedAppImages = response;
      DevActions.getSavedAppImages();
      that.getAllTags(response);
      that.trigger(that.state);
    }, function(error){})
  },

  getAllTags: function(images){
    var that = this;
    var tags = images.map(image=>{
      image.tags.forEach(tag=>{
        that.state.allImageTags.push(tag);
        that.trigger(that.state);
      });
    });
  },

  onInitiateImageTags: function(){
    this.state.dialog.createImage.tags = [];
    this.trigger(this.state);
  },

  onCreateImageAddTag: function(tag){
    if(tag !== "" && this.state.dialog.createImage.tags.indexOf(tag) == -1){
      this.state.dialog.createImage.tags.push(tag)
      this.trigger(this.state);
    }
  },

  onCreateImageRemoveTag: function(tag){
    var index = this.state.dialog.createImage.tags.indexOf(tag);
    this.state.dialog.createImage.tags.splice(index, 1);
    this.trigger(this.state);
  },

  onUserChangedCreateImageName: function(name){
    this.state.dialog.createImage.name = name;
    this.trigger(this.state);
  },

  onUserChangedCreateImageTags: function(tags){
    this.state.dialog.createImage.tags = tags;
    this.trigger(this.state);
  },

  onUserChangedCreateImageComment: function(comment){
    this.state.dialog.createImage.comment = comment;
    this.trigger(this.state);
  },

	onCreateImage: function(){

    var appInstanceId = this.state.selectedInstance.id,
        name = this.state.dialog.createImage.name,
        tags = this.state.dialog.createImage.tags,
        comment = this.state.dialog.createImage.comment,
        that = this;

    var data = {
      name: name,
      appInstanceId: appInstanceId,
      tags: tags,
      comment: comment
    }

		var options = {
			url: APISERVER + "/hub-web-api/commands/create-saved-image",
			method: 'POST',
      contentType: 'application/json',
      data: data
		}

		Config.request(options, function(response){
      DevDashboardActions.hideCreateImageDialog();
      that.fetchSavedImages();
      that.getImageState(response);
		}, function(error){
      console.log(error)
      ProdActions.showErrorDialog(error.responseText)
    })
	},

  getImageState: function(savedImageId){
    var options = {
      url: `${APISERVER}/hub-web-api/test-dev/saved-app-images/${savedImageId}`,
      method: 'GET'
    }
    var that = this;
    var timer = setInterval(function(){
      Config.request(options, function(response){
        that.state.savedImage = response;
        that.trigger(that.state);
        DevDashboardActions.setLoadingImage(response);

        if(response.state.toUpperCase() === stateOptions.created.toUpperCase()){
          console.log(response.state.toUpperCase())
          that.fetchSavedImages();
        }
        // console.log(response.state);
        if(response.state.toUpperCase() === stateOptions.created.toUpperCase()){
          clearInterval(timer);
        }
      }, function(error){})
    }, 1000);

  },

  getInitialState: function(){
    return this.state;
  },

  onShowCreateImageDialog: function(){
    this.state.dialog.createImage.isRender = true;
    this.trigger(this.state);
  },

  onHideCreateImageDialog: function(){
    this.state.dialog.createImage.isRender = false;
    this.trigger(this.state);
  }

});

export default DevDashboardCreateImageStore;
