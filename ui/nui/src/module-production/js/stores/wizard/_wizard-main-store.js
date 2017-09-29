// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import $ from 'jquery';
import Actions from '../../actions/actions.js';
import SharedActions from '../../../../shared-actions.js';
import AppTopologyActions from '../../actions/appTopologyActions.js';
import DevDashboardActions from '../../../../module-development/js/actions/dev-dashboard-actions.js';
import DevWizardActions from '../../../../module-development/js/actions/dev-wizard-actions.js';
import Config from '../../config.js';
import deployOptions from '../../../../module-development/js/data/deploying-state-options.js';
import MainStore from '../../stores/main-store.js';
import DashboardMainStore from '../../stores/dashboard/_dashboard-main-store.js';
import AppTopologyStore from '../../stores/wizard/appTopologyStore.js';
import DevImageOriginStore from '../../../../module-development/js/stores/dev-image-origin-store.js';

import AllApps from '../../data/allApps.js';
import NavigationOptions from '../../data/prodNavigationOptions.js';

import MockVersionsOptions from '../../../../module-development/js/data/wizard-config-mock-versions.js';

import TopolgoyDataController from '../../../../shared-components/js/topology/controllers/topology-data-controller.js';

require('es6-promise').polyfill();
require('isomorphic-fetch');


let WizardMainStore = Reflux.createStore({

  listenables: [Actions, AppTopologyActions, DevDashboardActions],

	stepsOrder: ['zero', 'appMarket', 'appTopology', 'siteSetup', 'businessSetup', 'summary'],

    currentStepName: 'zero',

    state: {
        header: {},
        allApplications: {},
    		selectedApp: {},
    		steps: {
    			appMarket: {
    				name: NavigationOptions.wizard.appMarket.subLocation,
    				isRender: false,
    				data: [],
    				isComplete: false,
    				id: 1,
    				isSelected: true,
    			},
    			appTopology: {
    				name: NavigationOptions.wizard.appTopology.subLocation,
    				isRender: false,
    				data: [],
    				isComplete: false,
    				id: 2,
    				isSelected: false,
                    editAppInstanceName: {
                        isRender: false,
                        appName: ""
                    }
    			},
    			siteSetup: {
    				name: NavigationOptions.wizard.siteSetup.subLocation,
    				isRender: false,
    				isComplete: false,
    				id: 3,
    				isSelected: false,
    				setupTypes:[
    					{
    						name: "single site",
    						iconPath: "map_SINGLE-SITE.png",
    						points: 1,
    					}
    				],
    				selectedSetupType: {},
    			},
    			businessSetup: {
    				name: NavigationOptions.wizard.businessSetup.subLocation,
    				isRender: false,
    				isComplete: false,
    				selectedPlan: {},
    				id: 4,
    				isSelected: false,
    			},
    			summary: {
    				name: NavigationOptions.wizard.summary.subLocation,
    				isRender: false,
    				isComplete: false,
    				id: 5,
    				isSelected: false,
    			},
    		},
        main: {
            header: {},
            currentStep: {},
            footer: {
                next: {
                    isActive: false
                },
                prev: {
                    isActive: false
                },
                inlineError: ""
            }
        },
        selectedBusinessContinuitySettings:{},
        selectedInfrastructureService:{},
    },

	getInitialState: function(){
      	return this.state;
    },

	//initial settings
	init: function(){

		var that = this;

    DevWizardActions.getSites();

		var options = {
			url: APISERVER + "/hub-web-api/app-template",
			contentType: "application/json",
			method: 'GET'
		}

		Config.request(options, function(response){

			that.state.steps.appMarket.data = response;

			//add ID to each app
			that.state.steps.appMarket.data.forEach(function(app, index){
        that.state.allApplications[app.id] = app;
				app.img = Config.fixImgUrl(app.img);
				app.isSelected = false;
        //add mock version options
        app.appServiceTemplates.forEach(service=>{
          service.versionOptions = MockVersionsOptions;
        });

			});
      if(DevImageOriginStore.state.isMock){
        DevDashboardActions.receiveAppTemplates(that.state.allApplications);
      }

			that.trigger(that.state);

		}, function(error){
			var error = "No applications found";
			Actions.showErrorDialog.bind(this, error)();
		});

		//initial step
		this.state.main.currentStep = this.state.steps['appMarket'];

		if(this.state.navigation == 'wizard'){
			this.applyCurrentStep();
		}

		this.trigger(this.state);

	},

    onEditAppInstanceName: function(appName){
        this.state.steps.appTopology.editAppInstanceName.isRender = true;
        this.state.steps.appTopology.editAppInstanceName.appName = appName;
        this.trigger(this.state);
    },

    onHideEditAppInstanceName: function(){
        this.state.steps.appTopology.editAppInstanceName.isRender = false;
        this.trigger(this.state);
    },

    getSelectedApp: function(){
        return this.state.selectedApp;
    },

    onGetApplicationByName: function(appName){
        console.log(this.state.steps.appMarket.data)
    },

	//steps navigation
	applyCurrentStep: function() {

        var nextStepName = this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName) + 1];
        var prevStepName = this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName) - 1];

        if(nextStepName != undefined){
            this.currentStepName = nextStepName;
            this.state.main.currentStep = this.state.steps[nextStepName];
            this.state.main.currentStep.isRender = true;

            if(this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName) - 1] != "zero"){
                this.state.steps[this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName) - 1]].isRender = false;
                this.state.steps[this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName) - 1]].isComplete = true;
            }

            //console.log(this.currentStepName)

            this.state.steps[nextStepName].isComplete = false;
            this.state.steps[nextStepName].isRender = true;
        }

        this.trigger(this.state);
    },

	//handle next step
    applyPrevStep: function(){

        this.state.main.currentStep.isComplete = false;
        var currentStepName = this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName) - 1];
        var prevStepName = this.stepsOrder[this.stepsOrder.indexOf(this.currentStepName)];

        this.currentStepName = currentStepName;
        this.state.main.currentStep = this.state.steps[currentStepName];
        this.state.main.currentStep.isComplete = false;

        if(currentStepName != undefined){
            var currentStep = this.state.steps[currentStepName];
            var prevStep = this.state.steps[prevStepName];
            currentStep.isComplete = false;
            currentStep.isRender = true;
            prevStep.isRender = false;
            this.trigger(this.state);
        }
        this.trigger(this.state);
    },

	//handle APP MARKET app selection
	onUserClickOnCard: function(cardId){

        this.state.main.currentStep.data.forEach(function(app, index){

            if(app.id == cardId){
                app.isSelected = true;
                WizardMainStore.state.selectedApp = app;
                Actions.setSelectedApp(app);
                Actions.setAppInstanceName(app.name+1)
            }else{
                app.isSelected = false;
            }
	         WizardMainStore.trigger(WizardMainStore.state);
        });

        this.state.main.footer.next.isActive = true;

        if(this.state.main.footer.inlineError != ""){
            this.state.main.footer.inlineError = "";
        }

        this.trigger(this.state);
    },

    onSetAppInstanceName: function(appName){
        this.state.steps.appTopology.editAppInstanceName.appName = appName;
        this.trigger(this.state);
    },

	//handle APP TOPOLOGY infrastructure selection
	onUserClickOnInfrastructure: function(selectedBox){
        this.state.selectedInfrastructureService = selectedBox;
        this.trigger(this.state);
    },

	//handle SITE SETUP selection
	getSiteSetupTypeByName: function(name){
        var selectedSetupType = {};
        this.state.steps.siteSetup.setupTypes.forEach(function(setupType){
            if(setupType.name == name){
                selectedSetupType = setupType;
            }
        });
        return selectedSetupType;
    },

    onUserClickOnSiteSetup: function(typeName){
        this.state.steps.siteSetup.selectedSetupType = this.getSiteSetupTypeByName(typeName);
        this.state.main.footer.inlineError = "";
        this.state.main.footer.next.isActive = true;
        this.trigger(this.state);
    },

	//handle BUSINESS CONTINUITY plan selection
	onUserClickOnBusinessSettingsPlan: function(selectedPlan){
        this.state.selectedBusinessContinuitySettings = selectedPlan;
        this.state.main.footer.next.isActive = true;

        if(this.state.main.footer.inlineError != ""){
            this.state.main.footer.inlineError = "";
        }

        this.trigger(this.state);
    },

	//set wizard current step by name (for browser navigation by hash)
	onSetWizardCurrentStep: function(stepName){

        //this.state.main.currentStep = this.state.steps["appMarket"];
		for(let key in this.state.steps){
			if(this.state.steps[key].name == stepName){
				this.state.main.currentStep = this.state.steps[key];
				this.trigger(this.state);
			}
		}
	},

	getDeployedAppPostOptions: function(){

		DashboardMainStore.state.appInstance.appInstanceName =
			DashboardMainStore.state.appInstance.appInstanceName == undefined ?
			this.getSelectedApp().name + 1 : DashboardMainStore.state.appInstance.appInstanceName;

		var appInstanceName = DashboardMainStore.state.appInstance.appInstanceName;
		var options = {
			url: APISERVER + '/hub-web-api/commands/deploy-app',
			method: 'POST',
			contentType: 'application/json',
			data: {
				"appInstanceName": appInstanceName,
				"appTemplateId": this.state.selectedApp.id,
				"dataProtectionSetup":{
					"policyName": this.state.selectedBusinessContinuitySettings.name,
					"policySettings":{}
				},
				"siteSetup":{
					"policyName": this.state.steps.siteSetup.selectedSetupType.name,
					"policySettings":{}
				}
			}
		}

		return options;
},

	completeWizard: function(){
		Actions.userClickOnNextCompleteWizard();
		WizardMainStore.applyCurrentStep();
		Actions.hideLoadingGif();
	},

	//handle steps navigtaions
    onUserClickOnNextWizard: function(){

    Actions.hideEditAppInstanceName();
		var that = this;

		//deploy app, show loading gif - end wizard - show dashboard
		if(this.currentStepName == "summary"){
      const dataFromTopology = TopolgoyDataController.getData();      
      DevWizardActions.deploySavedImage(this.state.selectedApp, dataFromTopology);
      // if(this.state.main.footer.next.isActive){
      //
			// 	//deploy app post request
			// 	Config.request(this.getDeployedAppPostOptions(), function(response){
      //
      //     // go to deploying progress screen
      //     SharedActions.navigate({
      //       module: NavigationOptions.module,
      //       location: NavigationOptions.deployingProgress.location,
      //       subLocation: `/${response}`
      //     });
      //
			// 		var dashboardStatsOptions = {
			// 			//url: APISERVER + "/hub-web-api/app-instance/" + response + "/dashboard-stats",
      //       url: `${APISERVER}/hub-web-api/app-instance/${response}/state`,
			// 			contentType: "application/json",
			// 			method: 'GET'
			// 		}
      //
			// 		//get deployed app request
			// 		var deployTimer = setInterval(function(){
      //
			// 			Config.request(dashboardStatsOptions, function(response, textStatus, xhr){
      //
			// 				switch(response.state.toUpperCase()){
			// 					case deployOptions.running.toUpperCase():
			// 						WizardMainStore.completeWizard();
      //             Actions.getAllAppInstances();
			// 						clearInterval(deployTimer);
			// 						break;
			// 					case deployOptions.error.toUpperCase():
			// 						clearInterval(deployTimer);
			// 						break;
			// 				}
      //
			// 			}, function(error){
			// 				Actions.showErrorDialog.bind(this, response.responseText)();
      //               		Actions.hideLoadingGif();
			// 			});
      //
			// 		}, 3000);
      //
			// 	}, function(error){
      //       Actions.showErrorDialog.bind(this, error.responseText)();
      //       Actions.hideLoadingGif();
			// 	});
      //
			// 	this.state.main.footer.next.isActive = false;
			// 	this.state.main.footer.prev.isActive = false;
			// 	this.applyCurrentStep();
      // }

		}

		else if(this.currentStepName == "appMarket"){
			if(this.state.selectedApp.name == undefined){
				this.state.main.footer.inlineError = "Please select an application";
				this.state.main.footer.next.isActive = false;
			}
			else{
				this.state.main.footer.inlineError = "";
				this.state.main.footer.next.isActive = true;
				this.state.main.footer.prev.isActive = true;
			}
		}
		else if(this.currentStepName == "businessSetup"){

			if(this.state.selectedBusinessContinuitySettings.name == undefined){
				this.state.main.footer.inlineError = "Please Select A Plan";
				this.state.main.footer.next.isActive = false;
			}else{
				this.state.main.footer.inlineError = "";
				this.state.main.footer.next.isActive = true;
			}
		}
		else if(this.currentStepName == "siteSetup"){
			if(this.state.steps.siteSetup.selectedSetupType.name == undefined){
				this.state.main.footer.inlineError = "Please Select A Site Setup";
				this.state.main.footer.next.isActive = false;
			}else{
				this.state.main.footer.inlineError = "";
				this.state.main.footer.next.isActive = true;
			}
		}

		if(this.state.main.footer.next.isActive == true) {
		   this.applyCurrentStep();
		}

		SharedActions.navigate({
			module: NavigationOptions.module,
			location: NavigationOptions.wizard.location,
			subLocation: this.state.main.currentStep.name
		});

        this.trigger(this.state);

   },

    onUserClickOnBackWizard: function(){

        if(!MainStore.state.isLoading && this.state.main.footer.prev.isActive){
            if(this.currentStepName != 'appMarket'){
                this.state.main.footer.next.isActive = true;
                this.applyPrevStep();
            }

            this.state.main.footer.inlineError = "";

			SharedActions.navigate({
				module: NavigationOptions.module,
				location: NavigationOptions.wizard.location,
				subLocation: this.state.main.currentStep.name
			});

            this.trigger(this.state)
        }

    },

    onShowErrorDialog: function(){
        this.state.main.footer.next.isActive = false;
        this.state.main.footer.prev.isActive = false;
        this.trigger(this.state);
    },

    onHideErrorDialog: function(){
        this.state.main.footer.next.isActive = true;
        this.state.main.footer.prev.isActive = true;
        this.trigger(this.state);
    },

    onInitializeWizardSettings: function(){
        this.deselectAllApps();
        this.currentStepName = "appMarket";
        this.state.main.footer.next.isActive = false;
        this.state.selectedApp = {};

        this.state.main.currentStep = this.state.steps['appMarket'];
        this.initializeLeftMenu();
        this.state.main.footer.inlineError = "";
        this.state.selectedBusinessContinuitySettings = {};
        this.state.steps.siteSetup.selectedSetupType = {};
		this.trigger(this.state);
    },

    deselectAllApps: function(){
        this.state.steps.appMarket.data.forEach(function(app){
            app.isSelected = false;
        });
        this.trigger(this.state);
    },

    initializeLeftMenu: function(){
        for(var key in this.state.steps){
            this.state.steps[key].isComplete = false;
        }
    }

});

export default WizardMainStore;
