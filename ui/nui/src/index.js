// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ReactDOM from 'react-dom';
import Reflux from 'reflux';
import styles from './module-production/css/style.scss';
import Config from './module-production/js/config.js';

//components
import Header from './module-production/js/components/header.comp.js';
import MainPane from './module-production/js/components/mainPane.comp.js';
import RestorePopup from './module-production/js/components/restorePopup.comp.js';
import Redirect from './module-production/js/components/redirect.comp.js';
import ErrorDialog from './module-production/js/components/errorDialog.comp.js';

//stores
import SharedStore from './shared-store.js';
import MainStore from '../src/module-production/js/stores/main-store.js';
import WizardMainStore from './module-production/js/stores/wizard/_wizard-main-store.js';
import AppTopologyStore from './module-production/js/stores/wizard/appTopologyStore.js';
import ProdConfigurationStore from './module-production/js/stores/wizard/prodConfigurationStore.js';
import DashboardMainStore from './module-production/js/stores/dashboard/_dashboard-main-store.js';
import DevMainScreenStore from './module-development/js/stores/dev-mainScreen-store.js';
import DevMainStore from './module-development/js/stores/dev-store.js';
import DevWizardStore from './module-development/js/stores/dev-wizard-store.js';
import DevDashboardMainStore from './module-development/js/stores/dev-dashboard-main-store.js';
import DevDashboardCreateImageStore from './module-development/js/stores/dev-dashboard-create-image-store.js';
import DevImageOriginStore from './module-development/js/stores/dev-image-origin-store.js';
import DevDashboardTopologyStore from './module-development/js/stores/dev-dashboard-topology-store.js';
import DevDeployingProgressStore from './module-development/js/stores/dev-deploying-progress-store.js';
import SharedImageStore from './module-development/js/stores/dev-shared-image-store.js';
import ShareWithJiraStore from './module-development/js/stores/share-with-jira-store.js';
import ShareWithPivotarTrackerStore from './module-development/js/stores/share-with-pivotal-tracker-store.js';
import SettingsStore from './module-development/js/stores/settings-store.js';
import SiteConfigStore from './module-development/js/stores/site-config-store.js';

// actions
import Actions from './module-production/js/actions/actions.js';
import SharedActions from './shared-actions.js';
import DevActions from './module-development/js/actions/dev-actions.js';

//sankey static files
require('file?name=sankey/[name].[ext]!./module-production/js/sankey/sankey.css');
require('file?name=sankey/[name].[ext]!./module-production/js/sankey/tooltip.css');
require('file?name=sankey/[name].[ext]!./module-production/js/sankey/sankey.js');
require('file?name=sankey/[name].[ext]!./module-production/js/sankey/autoloader.js');

//dashboard app availbility zone map static files
require('file?name=appAvailabilityZone/[name].[ext]!./module-production/js/data/world-110m.jsn');
require('file?name=appAvailabilityZone/[name].[ext]!./module-production/js/data/world-country-names.tsv');
require('file?name=appAvailabilityZone/[name].[ext]!./module-production/js/data/world.geojson');

//external scripts
import BrowserDetector from './module-production/js/browserDetection.js';
import setBodyClassByDevice from './module-production/js/setBodyClassByDevice.js';

//specific browsers fixes
import FireFoxFix from './module-production/js/browsers_fixes/firefox_fix.js';
import ExplorerFix from './module-production/js/browsers_fixes/explorer_fix.js';

//Development module
import DevStyle from './module-development/css/style.scss';
import DevelopmentModule from './module-development/js/components/_dev-main-component.comp.js';

//shared components style
require('./shared-components/css/style.scss')

//navigations options
import DevNavigationOptions from './module-development/js/data/devNavigationOptions.js';
import ProdNavigationOptions from './module-production/js/data/prodNavigationOptions.js';

//scroll stylesheet
// require('gemini-scrollbar/gemini-scrollbar.css');


let App = React.createClass({

    mixins: [
        Reflux.listenTo(WizardMainStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DashboardMainStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(MainStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(AppTopologyStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(SharedStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevMainStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevMainScreenStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevDashboardCreateImageStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevDashboardMainStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevWizardStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevImageOriginStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevDashboardTopologyStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(DevDeployingProgressStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(SharedImageStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(ShareWithJiraStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(ShareWithPivotarTrackerStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(SettingsStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(SiteConfigStore, "onChangeCallback", "initialCallBack"),
        Reflux.listenTo(ProdConfigurationStore, "onChangeCallback", "initialCallBack")
    ],

    onChangeCallback: function(state){
      this.setState(state);
    },

    initialCallback: function(){
      this.setState(state);
    },

	getWrapClass: function(){

		if(DashboardMainStore.state.restorePopup.isRender ||
       MainStore.state.errorDialog.isRender){
	       return "container dimmed";
		}else if(MainStore.state.isLoading){
      return "container loading"
    }
    else{
			return "container"
		}
	},

  componentDidMount: function(){
    window.addEventListener("hashchange", this.populateHash);
  },

	populateHash: function(e){
    var hash = Config.getCurrentHash(),
        module = hash.module,
        newLocation = hash.location,
        subLocation = hash.subLocation == undefined ? "" : "/"+hash.subLocation;

    if(module == "production"){
        Actions.navigateByName(newLocation, subLocation);
    }

    if(hash.additional){
      SharedActions.navigate({
        module: module,
        location: "/"+newLocation,
        subLocation: subLocation,
        additional: "/"+hash.additional || ''
      });
    }else{
      SharedActions.navigate({
        module: module,
        location: "/"+newLocation,
        subLocation: subLocation
      });
    }

    this.closeDialogs();
	},

  closeDialogs: function(){
    DevActions.hideShareImageDialog();
  },

    getInitialState: function(){
        BrowserDetector.redirectIfNotChrome();
        return null;
    },

    render: function(){
      // console.log(DevWizardStore.config.configuration)
      return (

        !MainStore.state.redirect.isRedirect ?

  				SharedStore.state.currentLocation.module == ProdNavigationOptions.module ?

					<div className="wrap">
						<div className={this.getWrapClass()}>

							<Header
								navigation={MainStore.state.navigation}
								isLoading={MainStore.state.isLoading}
								rightMenu={MainStore.state.rightMenu}
								leftMenu={MainStore.state.leftMenu}
								isErrorDialogShown={MainStore.state.errorDialog.isRender}
								selectedApp={WizardMainStore.state.selectedApp}
								selectedAppInstance={DashboardMainStore.state.appInstance}
								currentStep={WizardMainStore.state.main.currentStep}
								dashboardAppInstance={DashboardMainStore.state.appInstance}
								currentUser={MainStore.state.currentUser}
                module={SharedStore.state.currentLocation.module}
								settingsTooltip={MainStore.state.header.settings.tooltip}/>

							<MainPane
									navigation={MainStore.state.navigation}
									hash={MainStore.state.hash}
									isLoading={MainStore.state.isLoading}
									isOnline={MainStore.state.isOnline}
									rightMenu={MainStore.state.rightMenu}
									leftMenu={MainStore.state.leftMenu}
									allAppInstances={MainStore.state.allAppInstances}
									currentUser={MainStore.state.currentUser}

									wizardState={WizardMainStore.state}
									stepsOrder={WizardMainStore.stepsOrder}

									appTopologyState={AppTopologyStore.state}
									appTopologySelectedService={AppTopologyStore.state.appTopology.services.selectedService.service}
									appTopologySelectedDependency={AppTopologyStore.state.appTopology.dependencies.selectedDependency.dependency}
									appTopologyServicesTranslate={AppTopologyStore.state.appTopology.services.translate}
									appTopologyDependenciesTranslate={AppTopologyStore.state.appTopology.dependencies.translate}
									appTopologyAllServices={AppTopologyStore.state.appTopology.application.allServices}

                  websockets={DevDashboardMainStore.state.websockets}
                  dashboardSelectedInstance={DevDashboardMainStore.state.selectedInstance}

                  deployingProgressState={DevDeployingProgressStore.state}
                  selectedInstance={DevDeployingProgressStore.state.selectedInstance}
                  sites={SharedImageStore.state.sites.options}
                  config={DevWizardStore.state.config}
									dashboardState={DashboardMainStore.state}
									allAppInstances={MainStore.state.allAppInstances}/>
						</div>

						<RestorePopup
							data={DashboardMainStore.state.restorePopup}
							originAppInstanceId=
								{DashboardMainStore.state.appInstance.appInstanceId}/>

						<ErrorDialog data={MainStore.state.errorDialog}/>

					</div>

				:

				<DevelopmentModule
            loggedInUser={SharedStore.state.loggedInUser}
            isSelectedInstanceRunning={DevDeployingProgressStore.state.dialogSavedSuccessfuly.isRender}
            sharedImageState={SharedImageStore.state}

            errorDialog={MainStore.state.errorDialog}
            simpleTooltip={SharedStore.state.simpleTooltip}
            settingsStore={SettingsStore.state}
            isUserMenuRender={DevMainStore.state.header.userMenu.isRender}
            allFilters={DevMainStore.state.allFilters}

            mainScreenView={DevMainScreenStore.state.view}

            settingsTooltip={MainStore.state.header.settings.tooltip}
            leftMenu={MainStore.state.leftMenu}
            currentLocation={SharedStore.state.currentLocation}
            currentUser={MainStore.state.currentUser}
            quotas={DevMainScreenStore.state.quota}
            quotasList={DevMainScreenStore.state.quotasList}
            leftMenuFilters={DevMainStore.state.leftMenu.filters}
            filteredInstances={DevMainStore.state.filteredInstances}
            users={MainStore.state.users}
            sortByMenu={DevMainScreenStore.state.sortByMenu}
            instancesSortedBy={DevMainStore.state.instancesSortedBy}
            shareImageDialog={DevMainStore.state.shareImageDialog}

            navigation={MainStore.state.navigation}

						wizardCurrentStep={"/"+Config.getCurrentHash().subLocation}
						wizardSteps={DevWizardStore.state.steps}
            applications={WizardMainStore.state.allApplications}
            selectedApp={DevWizardStore.state.selectedApp}
            wizardImage={DevWizardStore.state.image}
            tagsTooltipScrollTop={DevWizardStore.state.image.tooltip.scrollTop}
            validation={DevWizardStore.state.validation.isValid}
            errorMsg={DevWizardStore.state.validation.error}
            currentUser={MainStore.state.currentUser}
            filteredImages={DevImageOriginStore.state.filteredImages}
            selectedImage={DevImageOriginStore.state.selectedImage}

            websockets={DevDashboardMainStore.state.websockets}
            dashboardSelectedInstance={DevDashboardMainStore.state.selectedInstance}
            dialogCreateImage={DevDashboardCreateImageStore.state.dialog.createImage}
            allImageTags={DevDashboardCreateImageStore.state.allImageTags}
            loadingImages={DevDashboardMainStore.state.loadingImages}
            imageOriginData={DevImageOriginStore.state.images}
            isImageOriginListRender={DevImageOriginStore.state.isListRender}
            dashboardTopologyData={DevDashboardMainStore.state.topologyData}
            isConfirmDisposeImageDialogRender={DevDashboardMainStore.state.confirmDisposeImageDialog.isRender}
            config={DevWizardStore.state.config}
            deployingProgressState={DevDeployingProgressStore.state}
            loadingImage={DevDashboardMainStore.state.loadingImage.image}

            appTopologyState={AppTopologyStore.state}
            appTopologySelectedService={AppTopologyStore.state.appTopology.services.selectedService.service}
            appTopologySelectedDependency={AppTopologyStore.state.appTopology.dependencies.selectedDependency.dependency}
            appTopologyServicesTranslate={AppTopologyStore.state.appTopology.services.translate}
            appTopologyDependenciesTranslate={AppTopologyStore.state.appTopology.dependencies.translate}
            appTopologyAllServices={AppTopologyStore.state.appTopology.application.allServices}

            sortTableBy={SharedStore.state.tableSortBy}
            jiraConfiguration={SettingsStore.state.integrations.jira}

            siteConfig={SiteConfigStore.state}
            />

            :

            <Redirect browserName={MainStore.state.redirect.browser.name}/>
        )
    }

});

ReactDOM.render(<App/>, document.getElementById('app'));
