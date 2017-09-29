// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Actions from '../actions/actions.js';
import DevActions from '../../../module-development/js/actions/dev-actions.js';
import SharedActions from '../../../shared-actions.js';
import BrowserDetector from '../browserDetection.js';
import Config from '../config.js';
import $ from 'jquery';

import NavigationOptions from '../data/prodNavigationOptions.js';
import DevNavigationOptions from '../../../module-development/js/data/devNavigationOptions.js';
import SharedStore from '../../../shared-store.js';

//mock data
import allApps from '../data/allApps.js';

require('es6-promise').polyfill();
require('isomorphic-fetch');

var Store = Reflux.createStore({

    listenables: [Actions],

    state:{

  		header: {
  			settings: {
  				tooltip: {
  					isRender: false,
  					modes: {
  						options:[
  							{name: "development"},
  							{name: "production"}
  						],
  						isRender: false
  					}
  				}
  			}
  		},
      navigation: "",
      hash: "",
  		apiServer: APISERVER,
  		sankeyDomain: SANKEYDOMAIN,
      isLoading: false,
      isOnline: "",
      rightMenu: {
          isRender: false
      },
      leftMenu: {
          isRender: true
      },
      redirect: {
          isRedirect: false,
          browser: {
              name: ""
          }
      },
      errorDialog: {
          isRender: false,
          content: ""
      },
      allAppInstances: [],
      users: {},
      selectedUser: {},
  		currentUser: {},
  		allCreators: [],
    },

    blockBrowsersIfNotChrome: false,

    browsersToBlock: [
        BrowserDetector.browserNames().safari,
    ],

	onToggleSettingsTooltipModes: function(){
		this.state.header.settings.tooltip.modes.isRender = !this.state.header.settings.tooltip.modes.isRender;
		this.trigger(this.state);
	},

  onCloseSettingsTooltipModes: function(){
      this.state.header.settings.tooltip.modes.isRender = false;
      this.trigger(this.state);
  },

	onToogleSettingsTooltip: function(){
		this.state.header.settings.tooltip.isRender = !this.state.header.settings.tooltip.isRender;
		this.trigger(this.state);
	},

  onCloseSettingsTooltip: function(){
      this.state.header.settings.tooltip.isRender = false;
      this.trigger(this.state);
  },

  init: function(){

    var hash = Config.getCurrentHash();

    this.getCurrentUser();
    this.getAllUsers();

    var newLocation = hash.location,
	      subLocation = hash.subLocation,
	      module = hash.module;

		if(window.location.hash == ""){

      //init on development mode
      this.state.navigation = DevNavigationOptions.main.location;

			SharedActions.setModule(DevNavigationOptions.module);
      DevActions.getAppInstances();

			SharedActions.navigate({
				module: DevNavigationOptions.module,
				location: DevNavigationOptions.main.location,
				subLocation: ""
			});

      //init on production mode
			// this.state.navigation = NavigationOptions.main.location;
      //
			// SharedActions.setModule(NavigationOptions.module);
      //
			// SharedActions.navigate({
			// 	module: NavigationOptions.module,
			// 	location: NavigationOptions.main.location,
			// 	subLocation: ""
			// })

		}else{
      if(module === NavigationOptions.module && newLocation === NavigationOptions.deployingProgress.location.substring(1)){
        // console.log(AppTopologyStore.state.appTopology.application)
        // const instanceId = Config.getCurrentHash().subLocation;
        //DevActions.checkState(instanceId);

      }
			if(module == NavigationOptions.module && newLocation == 'wizard'){
				Actions.setWizardCurrentStep('appMarket');

				SharedActions.navigate({
					module: NavigationOptions.module,
					location: NavigationOptions.wizard.location,
					subLocation: NavigationOptions.wizard.appMarket.subLocation
				})

			}else if(module == NavigationOptions.module){

				Actions.navigateByName(newLocation, subLocation);

			}else if(module == DevNavigationOptions.module){
				if(newLocation == "wizard"){
					SharedActions.navigate({
						module: DevNavigationOptions.module,
						location: DevNavigationOptions.wizard.location,
						subLocation: DevNavigationOptions.wizard.subLocation.appMarket
					})
				}else if(newLocation == "main"){
					SharedActions.navigate({
						module: DevNavigationOptions.module,
						location: DevNavigationOptions.main.location,
						subLocation: ""
					})
				}
			}

       this.state.navigation = newLocation;

	  }

    this.state.isOnline = navigator.onLine;

    BrowserDetector.blockBrowserByName.bind(this, this.browsersToBlock)();

    this.trigger(this.state);
  },

  getInitialState: function(){
    return this.state;
  },

  onUserClickOnHamburger: function(){
      this.state.leftMenu.isRender = !this.state.leftMenu.isRender;
      this.trigger(this.state);
  },

  onOpenLeftMenu: function(){
    this.state.leftMenu.isRender = true;
    this.trigger(this.state);
  },

  onCloseLeftMenu: function(){
    this.state.leftMenu.isRender = false;
    this.trigger(this.state);
  },

  onUserClickAppPreview: function(webEntryPointURL){
      window.open(webEntryPointURL, '_blank');
      this.trigger(this.state);
  },

  onShowLoadingGif: function(){
      this.state.isLoading = true;
      this.trigger(this.state);
  },

  onHideLoadingGif: function(){
      this.state.isLoading = false;
      this.trigger(this.state);
  },

  onBrowserDetectionRedirect: function(browser){

      if(this.blockBrowsersIfNotChrome){
          this.state.redirect.isRedirect = true;
          this.state.redirect.browser.name = browser;
          this.trigger(this.state);
      }
  },

  onBlockBrowserByName: function(browser){
      this.state.redirect.isRedirect = true;
      this.state.redirect.browser.name = browser;
      this.trigger(this.state);
  },

  onShowErrorDialog: function(err){
      this.state.errorDialog.isRender = true;
      this.state.errorDialog.content = err;
      this.trigger(this.state);
  },

  onHideErrorDialog: function(){
      this.state.errorDialog.isRender = false;
      this.state.errorDialog.content = "";
      this.trigger(this.state);
  },

	onNavigateToDashboard: function(){
		this.state.navigation = "dashboard";
//        Actions.setLeftMenuInitialState(this.state.navigation);
        this.trigger(this.state);
	},

  goToMainScreen: function(){
      this.state.navigation = "main";
//        Actions.setLeftMenuInitialState(this.state.navigation);
      this.trigger(this.state);
  },

  goToWizard: function(){
      this.state.navigation = "wizard";
//        Actions.setLeftMenuInitialState(this.state.navigation);
      this.trigger(this.state);
  },

  onSetLeftMenuInitialState: function(navigation){

      var isShow;
      switch(navigation){
          case "wizard":
              isShow = true;
              break;
          case "dashboard":
              isShow = true;
              break;

          case "main":
              isShow = false;
              break;
      }
	this.state.leftMenu.isRender = isShow;
	this.trigger(this.state);
      return isShow;
  },

  onGetAllAppInstances: function(){

		var that = this;

		var options = {
			url: APISERVER + '/hub-web-api/app-instance',
			method: 'GET',
			contentType: 'application/json'
		}

		Config.request(options, function(response){

			that.state.allAppInstances = response;

			that.state.allAppInstances.map((app)=>{
				var creatorUser = that.getUserById(app.creatorUserId);
				app["creator"] = creatorUser;
			})

			that.trigger(that.state);

		}, function(error){
			Actions.showErrorDialog(error)
		})

  },

	onNavigateByName: function(newLocation, subLocation){

		if(newLocation == 'wizard'){
			Actions.setWizardCurrentStep(subLocation);
		}

		this.state.navigation = newLocation;
		this.trigger(this.state);
	},

	getCurrentUser: function(){

		var that = this;

		var options = {
			url: APISERVER + "/hub-web-api/logged-in-user",
			contentType: "application/json",
			method: 'GET'
		}

		Config.request(options, function(response){

            that.state.currentUser = response;
			that.trigger(that.state);

		}, function(error){
            var error = "failed to get current user";
            Actions.showErrorDialog.bind(this, error)();
		})

	},

	onLogout: function(){
    console.log('logout')
		var options = {
			url: APISERVER + "/hub-web-api/commands/sign-out",
			contentType: "application/json",
			method: 'POST'
		}

		Config.request(options, function(response){

			window.location = APISERVER + "/hub-web-api/html/login/index.html";

		}, function(error){

		})

		this.trigger(this.state);
	},

    getAllUsers: function(){

		var that = this;

		var options = {
			url: APISERVER + "/hub-web-api/user",
			contentType: "application/json",
			method: 'GET'
		}

		Config.request(options, function(response){

      response.forEach((user)=>{
          that.state.users[user.id] = user;
      });

			that.trigger(that.state);

		}, function(error){
            var error = "failed to get current user";
            Actions.showErrorDialog.bind(this, error)();
		})

    },

    getUserById: function(id){
	    return this.state.users[id];
    }

});

export default Store;
