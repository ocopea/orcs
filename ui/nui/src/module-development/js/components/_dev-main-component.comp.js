// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import Config from '../../../module-production/js/config.js';

//actions
import SharedActions from '../../../shared-actions.js';
import DevWizardActions from '../actions/dev-wizard-actions.js';
import DevActions from '../actions/dev-actions.js';

//components
import DevMainScreen from './main-screen/dev-main-screen.comp.js';
import Dashboard from './dashboard/_dashboard.comp.js';
import DevLeftMenu from './left-menu.comp.js';
import DevHeader from './dev-header.comp.js';
import Wizard from './wizard/_wizard.comp.js';
import QuotasList from './main-screen/quotas-list.comp.js';
import DialogCreateImage from './dashboard/dialog.createImage.comp.js';
import CopyDetails from './wizard/image/savedImage.copyDetails.comp';
import ErrorDialog from '../../../module-production/js/components/errorDialog.comp.js';
import ImageOriginList from './dashboard/image-origin-list.comp.js';
import ConfirmDisposeImageDialog from './dashboard/confirm-dispose-image-dialog.comp.js';
import SavedImages from './saved-images/saved-images.comp.js';
import ShareImageDialog from './share-image-dialog.comp.js';
import DeployingProgress from './deploying-progress/deploying-progress.comp.js';
import DialogSavedSuccessfuly from './deploying-progress/deploying-progress.saved-successfully.comp.js';
import SharedImage from './shared-image/shared-image.comp.js';
import Settings from './settings/settings.comp.js';
import ConfigIntegrationDialog from './settings/integrations-config/config-integration-dialog.comp.js';
import JiraCredentialsDialog from './settings/integrations-config/jira/credentials-dialog.comp.js';
import SiteConfig from './site-config/_site-config.comp.js';
import AddArtifactRegistry from './site-config/addArtifact/main.comp.js';
import ConfirRemoveArtifact from './site-config/dialog-confirm-remove.comp.js';
import DsbDetailsDialog from './site-config/data-service-broker/details-dialog.comp.js';
import AddDsbDialog from './site-config/data-service-broker/add-dsb-dialog.comp.js';
import ConfirmRemoveDsbDialog from './site-config/data-service-broker/confirm-delete-dsb.comp.js';
import AddCrbDialog from './site-config/copy-repo-broker/addCrbDialog.comp.js';
import AddCrDialog from './site-config/copy-repo-broker/addCrDialog.comp.js';
import ConfirmRemoveCrbDialog from './site-config/copy-repo-broker/confirm-delete-crb-dialog.comp.js';


import LoadingGif from '../../../module-production/assets/gif/component-loading.gif';

import $ from 'jquery';


var DevMainComponent = React.createClass({

  componentDidMount: function(){

    //hide sort by menu on document click
    var that = this;
    $(document).click(function(e){
      if(that.props.sortByMenu.isRender){
        if(!e.target.classList.contains('sort-by-menu')){
            DevActions.closeSortByMenu();
        }
      }
    })

  },

  isDimmed: function(){
    return  this.props.quotasList.isRender ||
            this.props.dialogCreateImage.isRender ||
            this.props.wizardImage.copyDetails.isRender ||
            this.props.errorDialog.isRender ||
            this.props.isImageOriginListRender ||
            this.props.isConfirmDisposeImageDialogRender ||
            this.props.shareImageDialog.isRender ||
            this.props.deployingProgressState.dialogSavedSuccessfuly.isRender ||
            this.props.settingsStore.integrations.dialog.isRender ||
            this.props.currentLocation.subLocation === DevNavigationOptions.siteConfig.subLocation.addArtifact ||
            this.props.siteConfig.dsbDetailsDialog.isRender ||
            this.props.siteConfig.cofirmRemoveDialog.isRender ||
            this.props.siteConfig.addDsbDialog.isRender ||
            this.props.siteConfig.confirmRemoveDsbDialog.isRender ||
            this.props.siteConfig.addCrbDialog.isRender ||
            this.props.siteConfig.confirmRemoveCrbDialog.isRender ||
            this.props.currentLocation.subLocation == DevNavigationOptions.siteConfig.subLocation.addCr;
  },

  isMaximized: function(){
    var currentLocation = this.props.currentLocation.location;
    return currentLocation !== DevNavigationOptions.wizard.location &&
           currentLocation !== DevNavigationOptions.dashboard.location &&
           currentLocation !== DevNavigationOptions.savedImages.location &&
           currentLocation !== DevNavigationOptions.sharedImage.location &&
           currentLocation !== DevNavigationOptions.deployingProgress.location
           this.props.leftMenu.isRender;
  },

  isShowDeployingProgress: function(){
    return Config.getCurrentHash().module == DevNavigationOptions.module &&
           this.props.currentLocation.location == DevNavigationOptions.deployingProgress.location &&
           this.props.dashboardSelectedInstance !== undefined
  },

  isSettings: function(){
    return  Config.getCurrentHash().module === DevNavigationOptions.module &&
            this.props.currentLocation.location === DevNavigationOptions.settings.location;
  },

	render: function(){
    // console.log(this.props.siteConfig.addCrDialog)
		return(

			<div className={this.isDimmed() ? "dimmed" : null}>
				<div className={this.isDimmed() ?
								"development wrapper dimmed" :
								"development wrapper"}>

					<DevHeader
            selectedInstance={this.props.dashboardSelectedInstance}
						settingsTooltip={this.props.settingsTooltip}
						leftMenu={this.props.leftMenu}
						module={this.props.currentLocation.module}
						currentUser={this.props.currentUser}
						isQuoatsListRender={this.props.quotasList.isRender}
            currentLocation={this.props.currentLocation}
            isUserMenuRender={this.props.isUserMenuRender}/>

				{
					this.isMaximized() ?
						<DevLeftMenu
                filters={this.props.leftMenuFilters}
                allFilters={this.props.allFilters}
								currentLocation={this.props.currentLocation}
                users={this.props.users}
                sites={this.props.siteConfig.sites}
                selectedSite={this.props.siteConfig.selectedSite}/>
					:
					null
				}

					<div className={this.isMaximized() ? "inside" : "inside maximized"}>
						{
							this.props.currentLocation.location == DevNavigationOptions.main.location ?
								<DevMainScreen
									quotas={this.props.quotas}
									filteredInstances={this.props.filteredInstances}
									users={this.props.users}
                  isLeftMenuRender={this.props.leftMenu.isRender}
                  allFilters={this.props.allFilters}
                  view={this.props.mainScreenView}
                  applications={this.props.applications}
                  isSortByMenuRender={this.props.sortByMenu.isRender}
                  instancesSortedBy={this.props.instancesSortedBy}
                  sortTableBy={this.props.sortTableBy}/>
							:

							this.props.currentLocation.location == DevNavigationOptions.wizard.location ?
								<Wizard
									currentStep={this.props.wizardCurrentStep}
									steps={this.props.wizardSteps}
									applications={this.props.applications}
                  selectedApp={this.props.selectedApp}
                  image={this.props.wizardImage}
                  validation={this.props.validation}
                  errorMsg={this.props.errorMsg}
                  currentUser={this.props.currentUser}
                  users={this.props.users}
                  config={this.props.config}

                  appTopologyState={this.props.appTopologyState}
                  selectedService={this.props.appTopologySelectedService}
                  selectedDependency={this.props.appTopologySelectedDependency}
                  servicesTranslate={this.props.appTopologyServicesTranslate}
                  dependenciesTranslate={this.props.appTopologyDependenciesTranslate}
                  allServices={this.props.appTopologyAllServices}
                  simpleTooltip={this.props.simpleTooltip}/>
							:

                Config.getCurrentHash().module == DevNavigationOptions.module &&
                this.props.currentLocation.location == DevNavigationOptions.dashboard.location ?

                <Dashboard
                    selectedInstance={this.props.dashboardSelectedInstance}
                    isLeftMenuRender={this.props.leftMenu.isRender}
                    appTemplates={this.props.applications}
                    users={this.props.users}
                    loadingImages={this.props.loadingImages}
                    savedAppImages={this.props.wizardImage.savedImageData}
                    imageOriginData={this.props.imageOriginData}
                    appTopologyData={this.props.dashboardTopologyData}
                    appTopologyState={this.props.appTopologyState}
                    loadingImage={this.props.loadingImage}
                    imageToShare={this.props.selectedImage}/>
              :

                this.isShowDeployingProgress() ?
                  <div>
                    <DeployingProgress
                      websockets={this.props.websockets}
                      selectedInstance={this.props.dashboardSelectedInstance}
                      selectedInstanceState={this.props.deployingProgressState.instanceState}
                      state={this.props.deployingProgressState}
                      appTopologyData={this.props.dashboardTopologyData}
                      topologyState={this.props.appTopologyState}
                      selectedApp={this.props.selectedApp}/>
                  </div>

              :

              Config.getCurrentHash().module == DevNavigationOptions.module &&
              this.props.currentLocation.location == DevNavigationOptions.sharedImage.location ?
                <SharedImage
                  state={this.props.sharedImageState}
                  image={this.props.sharedImageState.sharedImage}
                  appTopologyState={this.props.appTopologyState}
                  tooltip={this.props.config.tooltip}
                  activeElements={this.props.config.activeElements}
                  allServices={this.props.appTopologyAllServices}
                  configuration={this.props.config.configuration}/>

              :

              Config.getCurrentHash().module == DevNavigationOptions.module &&
              this.props.currentLocation.location == DevNavigationOptions.savedImages.location ?
                <SavedImages
                  images={this.props.imageOriginData}
                  users={this.props.users}
                  appTemplates={this.props.applications}
                  sortBy={this.props.sortTableBy}
                  filteredImages={this.props.filteredImages}
                  tooltip={this.props.wizardImage.tooltip}/>
              :

              this.isSettings() ?
                <Settings
                  shareOptions={this.props.shareImageDialog.shareOptions}
                  platformName={this.props.shareImageDialog.config.selectedPlatform}
                  integrations={this.props.settingsStore.integrations}
                  currentLocation={this.props.currentLocation} />
              :
              Config.getCurrentHash().module == DevNavigationOptions.module &&
              this.props.currentLocation.location == DevNavigationOptions.siteConfig.location ?
                <SiteConfig
                  selectedRegistry = {this.props.siteConfig.selectedRegistry}
                  tabs = {this.props.siteConfig.tabNames}
                  selectedTab = {this.props.siteConfig.selectedTab}/>
              :
							null
						}

					</div>

				</div>

        {
          this.props.siteConfig.confirmRemoveCrbDialog.isRender ?
            <ConfirmRemoveCrbDialog
              selectedCrb={this.props.siteConfig.confirmRemoveCrbDialog.selectedCrb}/>
          : null
        }

        {
          this.props.siteConfig.addCrbDialog.isRender ?
            <AddCrbDialog />
          : null
        }

        {
          this.props.currentLocation.subLocation == DevNavigationOptions.siteConfig.subLocation.addCr ?
            <AddCrDialog form={this.props.siteConfig.addCrDialog.form} />
          : null
        }

        {
          this.props.siteConfig.confirmRemoveDsbDialog.isRender ?
            <ConfirmRemoveDsbDialog
              selectedDsb={this.props.siteConfig.confirmRemoveDsbDialog.selectedDsb}/>
          : null
        }

        {
          this.props.siteConfig.addDsbDialog.isRender ?
            <AddDsbDialog />
          : null
        }

        {
          this.props.siteConfig.dsbDetailsDialog.isRender ?
            <DsbDetailsDialog
              infoMenu={this.props.siteConfig.dsbDetailsDialog.infoMenu}
              selectedDsb={this.props.siteConfig.dataServiceBroker.selectedDsb}
              selectedPlan={this.props.siteConfig.dataServiceBroker.selectedPlan}/>
          : null
        }

        {
          this.props.currentLocation.subLocation == DevNavigationOptions.siteConfig.subLocation.addArtifact ?
            <AddArtifactRegistry
              currentStep={this.props.siteConfig.addArtifactRegistry.currentStep}
              steps={this.props.siteConfig.addArtifactRegistry.stepNames}
              types={this.props.siteConfig.addArtifactRegistry.steps.selectArtifact.types}
              selectedArtifact={this.props.siteConfig.addArtifactRegistry.steps.selectArtifact.selectedArtifact}
              error={this.props.siteConfig.addArtifactRegistry.error}
              mavenValidations={this.props.siteConfig.addArtifactRegistry.steps.save.validation} />
          :
          null
        }

        {
          this.props.siteConfig.cofirmRemoveDialog.isRender ?
            <ConfirRemoveArtifact registryName={this.props.siteConfig.cofirmRemoveDialog.registryName}/>
          :
          null
        }

        {
          this.props.settingsStore.integrations.dialog.isRender ?
            <ConfigIntegrationDialog
              integrations={this.props.settingsStore.integrations}
              shareOptions={this.props.shareImageDialog.shareOptions}
              jiraConfiguration={this.props.jiraConfiguration}/>
          : null
        }

        {
          this.props.settingsStore.integrations.jira.credentialsDialog.isRender ?
            <JiraCredentialsDialog
              url={this.props.settingsStore.integrations.jira.credentialsDialog.url}
              cleanUrl={this.props.settingsStore.integrations.jira.credentialsDialog.cleanUrl}
              port={this.props.settingsStore.integrations.jira.credentialsDialog.port}
              isLoading={this.props.settingsStore.integrations.jira.isLoading}/>
          : null
        }

				{
					this.props.quotasList.isRender ?
						<QuotasList
							quotas={this.props.quotas.infrastructureQuotas}
							isLeftMenuRender={this.props.leftMenu.isRender}/>
					:
					null
				}

        {
          this.props.dialogCreateImage.isRender ?
            <DialogCreateImage
              createImageData={this.props.dialogCreateImage}
              tagsSuggestions={this.props.allImageTags}/>
          :
          null
        }

        {
          this.props.wizardImage.copyDetails.imageDetails.isLoading ?
            <img src={LoadingGif} id="loader-gif" />
          :
          this.props.wizardImage.copyDetails.isRender ?
            <CopyDetails
              imageDetails={this.props.wizardImage.copyDetails.imageDetails}
              selectedImage={this.props.wizardImage.selectedImage}
              applications={this.props.applications}
              users={this.props.users}
              selectedStructure={this.props.wizardImage.copyDetails.selection}/>
          :
          null
        }

        {
          this.props.errorDialog.isRender ?
            <ErrorDialog data={this.props.errorDialog}/>
          :
          null
        }

        {
          this.props.isImageOriginListRender ?
            <ImageOriginList
              data={this.props.imageOriginData}
              users={this.props.users}
              sortTableBy={this.props.sortTableBy}
              />
          :
          null
        }

        {
          this.props.isConfirmDisposeImageDialogRender ?
            <ConfirmDisposeImageDialog
              instanceName={this.props.dashboardSelectedInstance.name}
              appInstanceId={this.props.dashboardSelectedInstance.id}/>
          :
          null
        }

        {
          this.props.shareImageDialog.isRender ?
            <ShareImageDialog
              image={this.props.shareImageDialog.imageToShare}
              shareOptions={this.props.shareImageDialog.shareOptions}
              loggedInUser={this.props.loggedInUser}
              jiraConfiguration={this.props.jiraConfiguration}/>
          :
          null
        }

        {
          this.props.isSelectedInstanceRunning ?
            <DialogSavedSuccessfuly
              selectedAppInstanceId={this.props.dashboardSelectedInstance.id}
              instance={this.props.dashboardSelectedInstance}
              webEntryPointURL={this.props.deployingProgressState.selectedInstance.webEntryPointURL}/>
          :
          null
        }


			</div>
		)

	}

});

export default DevMainComponent;
//this.props.currentLocation.subLocation
