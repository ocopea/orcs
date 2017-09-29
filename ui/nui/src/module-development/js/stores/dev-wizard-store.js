// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import _ from 'lodash';

// actions
import WizardActions from '../actions/dev-wizard-actions.js';
import SharedActions from '../../../shared-actions.js';
import DevActions from '../actions/dev-actions.js';
import ProdActions from '../../../module-production/js/actions/actions.js';
import AppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import DevDashboardActions from '../actions/dev-dashboard-actions.js';
import SharedImageActions from '../actions/dev-shared-image-actions.js';
import SiteConfigActions from '../actions/site-config-actions.js';

// mock data
import MockSites from '../data/mockSites.js';
import MockConfiguration from '../data/mockSiteConfiguration.js';
import MockSiteCopyDetails from '../data/mockSiteCopyDetails.js';
import SavedImageMockData from '../data/wizard-image-mock-data.js';
import MockBackups from '../data/wizard-image-backup-data.js';

// stores
import ProdWizardStore from '../../../module-production/js/stores/wizard/_wizard-main-store.js';

// helpers
import Config from '../../../module-production/js/config.js';
import TopologyParser from '../../../shared-components/js/topology-data-parser.js';
import stateOptions from '../data/deploying-state-options.js';

import DevNavigationOptions from '../data/devNavigationOptions.js';
import ProdNavigationOptions from '../../../module-production/js/data/prodNavigationOptions.js';
import ErrorDialog from '../../../module-production/js/components/errorDialog.comp.js';

import TopologyDataController from '../../../shared-components/js/topology/controllers/topology-data-controller.js';

var DevWizardStore = Reflux.createStore({

	listenables: [WizardActions, ProdActions, AppTopologyActions, DevActions],

	state:{

		steps: [
					DevNavigationOptions.wizard.subLocation.appMarket,
					DevNavigationOptions.wizard.subLocation.image,
					DevNavigationOptions.wizard.subLocation.config
			   ],
		currentStep: DevNavigationOptions.wizard.subLocation.appMarket,
		selectedApp: {},
		validation: {
			isValid: false,
			error: ""
		},
		image: {
			tabs: {
				savedImage: "SAVED IMAGE",
				backup: "BACKUP"
			},
			selectedTab: "",
			savedImageData: [],
			selectedImage: {},
			isMockData: false,
			backups:{
				isMock: true,
				copies: [],
				selectedDate: {}
			},
			tooltip: {
				isRender: false,
				tags: [],
				position: {},
				scrollTop: 0
			},
			copyDetails:{
				isRender: false,
				imageDetails:{
					isLoading: false,
					details: {}
				}
			},
		},
		config:{
			appInstanceName: "",
			tooltip:{
				isRender: false,
				position: {}
			},
			activeElements: {
				services: [],
				dependencies: []
			},
			addMockSites: false,
			addMockSpace: false,
			sites: [],
			sitesArray: [],
			selectedSite: {},
			selectedSpace: "",
			configuration: {},
			fromSavedImages: false
		}

	},

	init: function(){

		//set image step initial selected tab
		this.state.image.selectedTab = this.state.image.tabs.savedImage;
		this.trigger(this.state);

		//set image saved image data
		if(this.state.image.isMockData){
			this.state.image.savedImageData = SavedImageMockData;
		}else{
			this.fetchSavedImages();
		}

		//set backups
		if(this.state.image.backups.isMock){
			this.state.image.backups.copies = MockBackups;
			this.trigger(this.state);
		}else{
			console.log("fetch backups data")
		}

		//populate sites
		this.onGetSites();
	},

	onGetSites: function(){
		var that = this;
		var options = {
			url: APISERVER + '/hub-web-api/site-topology',
			method: 'GET'
		};
		Config.request(options, function(response){

			// add mock space
			if(that.state.config.addMockSpace){
				response[0].spaces.push('mock space')
			}

			//add mock sites
			if(that.state.config.addMockSites){
				MockSites.forEach(site=>{response.push(site)});
			}

			that.state.config.sitesArray = response;

			SharedImageActions.receiveSites(response);

			let sitesObj = {};
			response.forEach(site=>{
					that.state.config.sites[site.id] = site;
					sitesObj[site.id] = site;
			});
			// console.log(sitesObj)
			SiteConfigActions.receiveSites(sitesObj);

			that.trigger(that.state);
		}, function(error){})
	},

	onSetSelectedSite: function(id, instance){
		this.state.config.selectedSite = this.state.config.sites[id];
		this.getConfiguration(id, instance);
		var services = this.removePlans(this.state.selectedApp.appServiceTemplates);

		// set site default space
		this.state.config.selectedSpace = this.state.config.selectedSite ?
																				this.state.config.selectedSite.spaces[0] : null;
		this.trigger(this.state);
	},

	onSetSelectedSpace: function(space){
		this.state.config.selectedSpace = space;
		this.trigger(this.state);
	},

	removePlans: function(services){
		return services ? services.map(s=>{
			s.dependencies.forEach(d=>{
				d.plan = undefined;
			});
		}) : null
	},

	getConfiguration: function(selectedSiteId, instance){
		var isMock = selectedSiteId === '0001';
		const _instance = instance ? instance : this.state.selectedApp;

		var that = this;

		if(_instance.id !== undefined){
			if(!isMock){
				var options = {
					url: APISERVER + `/hub-web-api/test-dev/site/${selectedSiteId}/app-template-configuration/${_instance.id}`,
					method: 'GET'
				}
				Config.request(options, function(response){
					that.state.config.configuration = response;
					// no dsbs mock
					// that.state.config.configuration.dataServiceConfigurations[0].dsbPlans = {};
					that.fetchImageDetailsById();
					that.trigger(that.state);
				}, function(error){console.log(error)});
			}else{
				this.state.config.configuration = MockConfiguration;
				that.state.image.copyDetails.imageDetails.details = TopologyParser.parse(MockSiteCopyDetails);
				AppTopologyActions.setSelectedApp(TopologyParser.parse(MockSiteCopyDetails));
				//this.state.selectedApp = TopologyParser.parse(MockSiteCopyDetails);
				this.trigger(this.state);
			}
		}
	},

	onUserClickOnBlankImage: function(){
		this.state.validation.isValid = true;
		this.state.validation.error = "";
		this.state.image.selectedImage = {};
		this.fetchImageDetailsById();
		this.onUserClickOnNext();
	},

	/**
	 * Handle services and dependencies active state
	 * in development module
	 * @params: String elementType (service/dependency) int elementId, Boolian isActive
	 */
	setElementActivation: function(type, id, isActive){

		switch (type) {
			case 'service':
				this.state.image.copyDetails.imageDetails.details.appServiceTemplates[id].isActive = isActive;
				break;
			case 'dependency':
				this.state.image.copyDetails.imageDetails.details.appServiceTemplates.forEach(service=>{
					service.dependencies.forEach(dependency=>{
						if(dependency.index == id){
							dependency.isActive = isActive
						}
					})
				})
				break;
		}

		this.trigger(this.state);
	},

	onUserChangedSelectedAppServiceVersion: function(version, serviceId){
		this.state.selectedApp.appServiceTemplates[serviceId].version = version;
		this.state.image.copyDetails.imageDetails.details.appServiceTemplates[serviceId].version = version;
		AppTopologyActions.setSelectedElementVersion(version);
		this.trigger(this.state);
	},

	onSetSelectedElementPlan: function(plan, serviceIndex, dependencyIndex){
		var selectedApp = this.state.selectedApp,
				copyDetails = this.state.image.copyDetails.imageDetails.details;

		selectedApp.appServiceTemplates[serviceIndex].dependencies.forEach(o=>{
			if(o.index === dependencyIndex){
				o.plan = plan;
			}
		});
		copyDetails.appServiceTemplates[serviceIndex].dependencies.forEach(o=>{
			if(o.index === dependencyIndex){
					o.plan = plan;
			}
		});

		this.trigger(this.state);
	},

	onToggleSwitch: function(options){
		console.log(options)
		this.setElementActivation(options.type, options.id, options.isActive)
		AppTopologyActions.setSelectedElementActiveState(options.isActive);
	},

	onShowConfigTopologyTooltip: function(position){
		this.state.config.tooltip.isRender = true;
		this.state.config.tooltip.position = position;
		this.trigger(this.state);
	},

	onHideConfigTopologyTooltip: function(){
		this.state.config.tooltip.isRender = false;
		this.trigger(this.state);
	},

	onCopyDetailsTopologySelection: function(selection){
		this.state.image.copyDetails.selection = selection;
		this.trigger(this.state);
	},

	onGetSavedAppImages: function(){
		this.fetchSavedImages();
	},

	onUserChangedAppInstanceName: function(appInstanceName){

		if(appInstanceName.trim().length > 0){
			const appServices = this.state.config.configuration.appServiceConfigurations;
			const dataServices = this.state.config.configuration.dataServiceConfigurations;
			const allServicesHasVersions = this.isAllServicesHasVersions(appServices);
			const allDataServicesHavePlans = this.isAllDsbsHavePlans(dataServices);


			if(allServicesHasVersions && allDataServicesHavePlans){
				this.state.validation.isValid = true;
			}else{
				this.state.validation.isValid = false;
			}
			this.state.validation.error = "";
		}else{
			this.state.validation.isValid = false;
		}
		this.state.config.appInstanceName = appInstanceName;
		this.trigger(this.state);
	},

	getInstanceDeploymentPlan(dataFromTopology) {
		const appServices = dataFromTopology.appServices;
		const dataServices = dataFromTopology.dataServices;
		const appServicesConfig = this.state.config.configuration.appServiceConfigurations;
		const dataServicesConfig = this.state.config.configuration.dataServiceConfigurations;

		let parsedAppServices = {};
		let parsedDataServices = {};

		// parse deployment plan services
		appServices.forEach(appService => {
			const serviceName = appService.name;
			parsedAppServices[serviceName] = {
				space: this.state.config.selectedSpace,
				enabled: appService.isActive,
				imageVersion: appService.version,
				artifactRegistryName: 'shpanRegistry'
			};
		});

		// parse deployment plan dependencies
		dataServices.forEach(dataService => {
			const configDataService = this.getDataServiceByName(dataService.name, dataServicesConfig);
			const plans = configDataService.dsbPlans;
			const dsbURNs = Object.keys(plans);
			const defaultPlan = plans[dsbURNs[0]];

			var dsbProtocol = defaultPlan.plans[0].protocols[0];
			parsedDataServices[dataService.name] = {
				dsbURN: dataService.selectedPlan.service || dsbURNs[0],
				enabled: dataService.isActive,
				dsbPlan: dataService.selectedPlan.id || defaultPlan.plans[0].id,
				dsbProtocol: dsbProtocol
			}
		});

		return {appServices: parsedAppServices, dataServices: parsedDataServices};
	},

	getDataServiceByName(name, dataServicesConfig) {
		return dataServicesConfig.filter(dataService => {
			return dataService.dataServiceName === name;
		})[0];
	},

	onDeploySavedImage: function(application, dataFromTopology){

		var deploymentPlan;

		if(!dataFromTopology){
			deploymentPlan = {
					appServices: this.parseDeploymentPlan(application).appServices,
					dataServices: this.parseDeploymentPlan(application).dataServices
			}
		}else{
			deploymentPlan = this.getInstanceDeploymentPlan(dataFromTopology);
		}

		var selectedSpace = this.state.config.selectedSpace;
		const isProdModule = Config.getCurrentHash().module === ProdNavigationOptions.module;
		
		//if image has been selected
		if(!_.isEmpty(this.state.image.selectedImage)){
			var options = {
	      url: APISERVER + "/hub-web-api/commands/deploy-saved-image",
				contentType: "application/json",
	      method: 'POST',
	      data: {
					"savedImageId": this.state.image.selectedImage.id,
					"appInstanceName" : this.state.config.appInstanceName,
					"siteId": this.state.config.selectedSite.id,
					"deploymentPlan": deploymentPlan
				}
	    }
		}else{
			//deploy with blank image
			var options = {
				url: APISERVER + '/hub-web-api/commands/deploy-test-dev-app',
				method: 'POST',
				contentType: "application/json",
				data: {
					"appInstanceName" : this.state.config.appInstanceName,
					"appTemplateId": application.id,
					"siteId": this.state.config.selectedSite.id,
					"deploymentPlan": deploymentPlan
				}
			}
			if(isProdModule){
				options.url = APISERVER + '/hub-web-api/commands/deploy-app',
				options.data.siteSetup = {
					policyName: ProdWizardStore.state.steps.siteSetup.selectedSetupType.name,
					policySettings: {}
				};
				options.data.dataProtectionSetup =  {
					policyName: ProdWizardStore.state.selectedBusinessContinuitySettings.name,
					policySettings: {}
				}
			}
			
		}

		Config.request(options, function(response){
			if(Config.getCurrentHash().module === DevNavigationOptions.module) {
				SharedActions.navigate({
					module: DevNavigationOptions.module,
					location: DevNavigationOptions.deployingProgress.location,
					subLocation: `/${response}`
				});
			}else if(isProdModule){
				SharedActions.navigate({
					module: ProdNavigationOptions.module,
					location: ProdNavigationOptions.deployingProgress.location,
					subLocation: `/${response}`
				});
			}
			DevActions.getAppInstances();
		}, function(error){
			ProdActions.showErrorDialog(error.responseText);
		});

  },

	parseDeploymentPlan: function(application){
		var services = this.parseServices(application.appServiceTemplates);
		var dependencies = this.parseDependencies(application.appServiceTemplates);
		return {appServices: services, dataServices: dependencies}
	},

	parseServices: function(services){

		var configuration = this.state.config.configuration,
				parsedServices = {};

		services.forEach((service, i)=>{

			var supportedVersions = configuration.appServiceConfigurations[i].supportedVersions,
			versionsKeys = Object.keys(supportedVersions);

			var serviceName = service.appServiceName === undefined ?
												service.name : service.appServiceName;

			parsedServices[serviceName] = {
				space: this.state.config.selectedSpace,
				enabled: service.isActive,
				imageVersion: service.version === undefined ?
											service.imageVersion : service.version,
				artifactRegistryName: versionsKeys[0]
			}
		});
		return parsedServices;
	},

	parseDependencies: function(services){

		var dataServiceConfigurations = this.state.config.configuration.dataServiceConfigurations;
		let isImageSelected = !_.isEmpty(this.state.image.selectedImage);
		var parseDependencies= {};

		services.forEach(service=>{

			service.dependencies.forEach((dependency, dependencyIndex)=>{

					var thisDataService = _.filter(this.state.config.configuration.dataServiceConfigurations, c=>{
						return c.dataServiceName === dependency.name;
					})[0] || this.state.config.configuration.dataServiceConfigurations[dependencyIndex];

					var keys = Object.keys(thisDataService.dsbPlans);

					if(dependency.plan === undefined){

						var dsbPlans = thisDataService.dsbPlans[keys[0]],
								plans = !dsbPlans ? [] : dsbPlans.plans;

						dependency.plan = {};
						dependency.plan.service = keys[0];
						dependency.plan.name = dsbPlans.plans[0].name;
						dependency.plan.id = dsbPlans.plans[0].id;
						dependency.plan.protocols = dsbPlans.plans[0].protocols;
					}

					var dsbURN = dependency.plan.service;
				  	var dsbPlan = dependency.plan.id;
					var dsbProtocol = dependency.plan.protocols[0];

					parseDependencies[thisDataService.dataServiceName] = {
						dsbURN: dsbURN,
						enabled: dependency.isActive,
						dsbPlan: dsbPlan,
						dsbProtocol: dsbProtocol
					}

			});
		});

		console.log("muhaha");
		console.log(parseDependencies);
		return parseDependencies;
	},

	fetchSavedImages: function(){
		var that = this;

		var savedImagesOptions = {
			url: APISERVER + "/hub-web-api/test-dev/saved-app-images",
			method: 'GET'
		}
		Config.request(savedImagesOptions, function(response){
				// filter by state 'created'
				var createdImages = _.filter(response, image=>{
					return image.state.toUpperCase() === stateOptions.created.toUpperCase()
				});
				DevActions.receiveSavedImages(createdImages);
				that.state.image.savedImageData = createdImages;
				that.trigger(that.state);
		}, function(error){})
	},

	onShowCopyDetails: function(imageID){
		this.onSetSelectedImage(imageID);
		this.state.image.copyDetails.isRender = true;
		this.trigger(this.state);
	},

	onSetSelectedImage: function(imageID){
		var selectedImage = this.state.image.savedImageData[imageID];
		this.state.image.selectedImage = selectedImage;
		this.fetchImageDetailsById();
		this.getConfiguration(this.state.config.selectedSite.id);
		this.trigger(this.state);
	},

	onInitiateSelectedImage: function(){
		this.state.image.selectedImage = {};
		this.trigger(this.state);
	},

	fetchImageDetailsById: function(){
		var that = this;

		if(!_.isEmpty(this.state.image.selectedImage)){
			var options = {
				url: APISERVER + `/hub-web-api/test-dev/saved-app-images/${this.state.image.selectedImage.id}/details`,
				method: 'GET'
			}
			Config.request(options, function(response){
				that.state.image.copyDetails.imageDetails.details = TopologyParser.parse(response);
				AppTopologyActions.setSelectedApp(TopologyParser.parse(response));
				that.trigger(that.state);
			}, function(error){});
		}else{
			that.state.image.copyDetails.imageDetails.details = this.state.selectedApp;
			AppTopologyActions.setSelectedApp(this.state.image.copyDetails.imageDetails.details);
		}
		this.trigger(this.state);
	},

	onHideCopyDetails: function(){
		this.state.image.copyDetails.isRender = false;
		this.trigger(this.state);
	},

	onSetTagsToolTipScrollTop: function(scrollTop){
		this.state.image.tooltip.position.top -= scrollTop;
		this.trigger(this.state);
	},

	onShowTagsTooltip: function(position, tags){
		this.state.image.tooltip.isRender = true;
		this.state.image.tooltip.position = position;
		this.state.image.tooltip.tags = tags;
		this.trigger(this.state);
	},

	onHideTagsTooltip: function(){
		this.state.image.tooltip.isRender = false;
		this.trigger(this.state);
	},

	onSetImageBackupSelectedDate: function(selectedDate){
		this.state.image.backups.selectedDate = selectedDate;
		this.trigger(this.state);
	},

	onSetImageSelectedTab: function(selectedTab){
		this.state.image.selectedTab = selectedTab;
		this.trigger(this.state);
	},

	onSetCurrentStep: function(step){
		this.state.currentStep = step;
		this.trigger(this.state);
	},

	onUserSelectedApp: function(app){
		this.state.selectedApp=app;
		this.state.image.copyDetails.imageDetails.details = app;
		if(!_.isEmpty(app)){
			this.state.validation.error = "";
			this.state.validation.isValid=true;
		}else{
			this.state.validation.isValid=false;
		}
		this.trigger(this.state);
	},

	onSetTopologyActiveElements: function(selectedApp){

		if(!_.isEmpty(selectedApp) && selectedApp.appServiceTemplates !== undefined){

			var activeServices = selectedApp.appServiceTemplates.filter(service=>{
				return service.isActive;
			});

			var activeDependencies = [];
			selectedApp.appServiceTemplates.forEach(service=>{
				service.dependencies.filter(dependency=>{
					if(dependency.isActive){
							activeDependencies.push(dependency);
					}
				})
			});

			this.state.config.activeElements.services = activeServices;
			this.state.config.activeElements.dependencies = activeDependencies;

			this.trigger(this.state);

		}

	},

	onUserSelectedImage: function(imageID){

		if(imageID != null){
			this.state.image.selectedImage = this.state.image.savedImageData[imageID];
			this.state.image.selectedImage.index = imageID;
			this.fetchImageDetailsById();
			this.state.validation.isValid = true;
			this.state.validation.error = "";
		}else{
			this.state.image.selectedImage = {}
		}
		this.trigger(this.state);
	},

	onSetIsFromSavedImages: function(){
		this.state.config.fromSavedImages = true;
		this.trigger(this.state);
	},

	validationDictionary: function(){
		return {
			appMarket : "please select an application",
			image : "please select an image",
			config : this.wizardConfigValidation()
		}
	},

	wizardConfigValidation() {
		let allServicesHaveVersions = this.isAllServicesHasVersions(this.state.config.configuration.appServiceConfigurations);
		let allDataServicesHavePlans = this.isAllDsbsHavePlans(this.state.config.configuration.dataServiceConfigurations);
		let isAppInstanceNameValid = this.state.config.appInstanceName.trim().length > 0;
		let configError;

		const NO_REGISTRY_FOUND = "no registry found";
		const INVALID_INSTANCE_NAME = "instance name is not valid";
		const NO_DSB_PLANS = "data services unsatisfied"

		if(!allServicesHaveVersions){
			if(!isAppInstanceNameValid && !allDataServicesHavePlans){
				configError = `${NO_REGISTRY_FOUND}, ${INVALID_INSTANCE_NAME}, ${NO_DSB_PLANS}`;
			}else if(isAppInstanceNameValid && !allDataServicesHavePlans){
				configError = `${NO_REGISTRY_FOUND}, ${NO_DSB_PLANS}`;
			}else if(!isAppInstanceNameValid && allDataServicesHavePlans){
				configError = `${INVALID_INSTANCE_NAME}, ${NO_REGISTRY_FOUND}`;
			}else if(isAppInstanceNameValid && allDataServicesHavePlans){
				configError = `${NO_REGISTRY_FOUND}`;
			}
		}else if(!isAppInstanceNameValid && !allDataServicesHavePlans){
				configError = `${INVALID_INSTANCE_NAME}, ${NO_DSB_PLANS}`;
		}else if(isAppInstanceNameValid && !allDataServicesHavePlans){
				configError = `${NO_DSB_PLANS}`;
		}else if(!isAppInstanceNameValid && allDataServicesHavePlans){
				configError = `${INVALID_INSTANCE_NAME}`;
		}
		return configError;
	},

	onInvalidate: function(){
		this.state.validation.isValid = false;
		this.trigger(this.state);
	},

	onUserClickOnNext: function(){
		if(!this.state.validation.isValid){
			switch (this.state.currentStep) {
				case DevNavigationOptions.wizard.subLocation.appMarket:
					this.state.validation.error = this.validationDictionary()['appMarket'];
					break;
				case DevNavigationOptions.wizard.subLocation.image:
					this.state.validation.error = this.validationDictionary()['image'];
					break;
				case DevNavigationOptions.wizard.subLocation.config:
					this.state.validation.error = this.validationDictionary()['config'];
					break;
				}
		}else{
			var nextStepIndex = this.state.steps.indexOf(this.state.currentStep)+1;
			this.navigateWizard(nextStepIndex);
		}
		this.trigger(this.state);
	},

	onUserClickOnBack: function(){
		this.state.validation.error="";
		var nextStepIndex = this.state.steps.indexOf(this.state.currentStep)-1;
		this.navigateWizard(nextStepIndex)
	},

	navigateWizard: function(nextStepIndex){
		if(nextStepIndex < this.state.steps.length && nextStepIndex >= 0){
			this.state.currentStep = this.state.steps[nextStepIndex];
			if(this.state.currentStep){
					//steps are invalid by default
					this.state.validation.isValid = false;
			}
			this.trigger(this.state);
		}else if(nextStepIndex == this.state.steps.length){
			this.wizardComplete();
		}

		SharedActions.navigate({
			module: DevNavigationOptions.module,
			location: DevNavigationOptions.wizard.location,
			subLocation: this.state.currentStep
		});
	},

	wizardComplete: function(){

		if(this.state.validation.isValid){
			const appServices = this.state.config.configuration.appServiceConfigurations;
			const dataServices = this.state.config.configuration.dataServiceConfigurations;
			const allServicesHasVersions = this.isAllServicesHasVersions(appServices);
			const allDataServicesHavePlans = this.isAllDsbsHavePlans(dataServices);

			if(allServicesHasVersions && allDataServicesHavePlans){
				const dataFromTopology = TopologyDataController.getData();
				this.onDeploySavedImage(this.state.image.copyDetails.imageDetails.details, dataFromTopology);
			}
		}
	},

	isAllServicesHasVersions(services){
		let hasVersions = true;
		_.forEach(services, service=>{
			if(_.isEmpty(service.supportedVersions)){
				hasVersions = false;
				return false;
			}
		});
		return hasVersions;
	},

	isAllDsbsHavePlans(dataServices) {
		let hasPlans = true;
		_.forEach(dataServices, service=>{
			if(_.isEmpty(service.dsbPlans)){
				hasPlans = false;
				return hasPlans;
			}
		});
		return hasPlans;
	},

  getInitialState: function(){
    return this.state;
  }

});

export default DevWizardStore;
