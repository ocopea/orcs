// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/actions.js';
import SharedActions from '../../../shared-actions.js';
import Config from '../config.js';

//components
import MainScreen from './main-screen/_mainScreen.comp.js';
import Dashboard from './dashboard/_dashboard.comp.js';
import Wizard from '../components/wizard/_wizard.comp.js';
import DeployingProgress from './deploying-progress/prod-deploying-progress.comp.js';
import LeftMenu from './menuLeft.comp.js';
import RightMenu from './menuRight.comp.js';
import LoadingGif from '../../../module-development/assets/loader.gif';
import ProdNavigationOptions from '../data/prodNavigationOptions.js';


var MainPane = React.createClass({

    componentDidMount: function(){
      Actions.openLeftMenu();
    },

    mainPaneClass: function(){

        var mainPaneState = null;

        var isLeftMenuShown = this.props.leftMenu.isRender;
        var isRightMenuShown = this.props.rightMenu.isRender;

        if(isLeftMenuShown && isRightMenuShown)
            mainPaneState = "minimized-full";
        else if(isLeftMenuShown && !isRightMenuShown)
            mainPaneState = "minimized-left";
        else if(!isLeftMenuShown && isRightMenuShown)
            mainPaneState = "minimized-right";
        else if(!isLeftMenuShown && !isRightMenuShown)
            mainPaneState = "maximized";

        if(this.props.navigation == "main"){
            mainPaneState += " main-screen"
        }

        return mainPaneState;
    },

    setLoadingGifClass: function(){
        if(this.props.isLoading){
            return "show";
        }else{
            return "hide";
        }
    },

    setLoadingClass: function(className){

        if(this.props.isLoading)
            return className + "-loading";
        else
            return className;
    },

    setWrapperClass: function(){
        if(this.props.navigation == "main"){
            return "wrapper main-screen";
        }else{
            return "wrapper";
        }
    },


   render: function(){


      let topologyData = {
        appTopologyState: this.props.appTopologyState,
        appTopologySelectedService: this.props.appTopologySelectedService,
        appTopologySelectedDependency: this.props.appTopologySelectedDependency,
        appTopologyServicesTranslate: this.props.appTopologyServicesTranslate,
        appTopologyDependenciesTranslate: this.props.appTopologyDependenciesTranslate,
        appTopologyAllServices: this.props.appTopologyAllServices
      }
      
       return (

		   	<div id={this.setLoadingClass.bind(this, "wrapper")()} className={this.setWrapperClass()}>

               <LeftMenu
                  leftMenu={this.props.leftMenu}
                  main={this.props.wizardState.main}
                  stepsOrder={this.props.stepsOrder}
                  steps={this.props.wizardState.steps}
                  navigation={this.props.navigation}
                  selectedApp={this.props.wizardState.selectedApp}
      						allAppInstances={this.props.allAppInstances}
                  applications={this.props.wizardState.allApplications}
      						selectedAppInstance={this.props.dashboardState.appInstance}
      						hash={this.props.hash}/>

               <RightMenu
			            rightMenu={this.props.rightMenu}
                  selectedApp={this.props.wizardState.selectedApp}/>

               <div id="main-pane" className={this.mainPaneClass()}>

                  <div className="inside">

                       <div id="loader-gif" className={this.setLoadingGifClass()}>
                            <img src={LoadingGif}/>
                            <span>Deploying...</span>
                       </div>

                        {

                            this.props.navigation == 'wizard' ?
              							   <Wizard
              									 steps={this.props.wizardState.steps}
              									 appInstanceName={this.props.dashboardState.appInstance.appInstanceName}
              									 isLoading={this.props.isLoading}
              									 main={this.props.wizardState.main}
              									 selectedInfrastructureService={this.props.wizardState.selectedInfrastructureService}
              									 siteSetupTypes={this.props.wizardState.steps.siteSetup.setupTypes}
              									 selectedSiteSetup={this.props.wizardState.steps.siteSetup.selectedSetupType}
              									 selectedBusinessContinuitySettings={this.props.wizardState.selectedBusinessContinuitySettings}
              									 isWizard={this.props.isWizard}
              									 selectedApp={this.props.wizardState.selectedApp}
                                 sites={this.props.sites}
                                 config={this.props.config}

                                 appTopologyState={this.props.appTopologyState}
                                 appTopologySelectedService={this.props.appTopologySelectedService}
                                 appTopologySelectedDependency={this.props.appTopologySelectedDependency}
                                 appTopologyServicesTranslate={this.props.appTopologyServicesTranslate}
                                 appTopologyDependenciesTranslate={this.props.appTopologyDependenciesTranslate}
				                         appTopologyAllServices={this.props.appTopologyAllServices}/>

                         : this.props.navigation == 'dashboard' ?
                             <Dashboard
                                 hash={this.props.hash}
				                         appInstanceId={this.props.dashboardState.appInstance.id}
                                 isOnline={this.props.isOnline}
                                 cards={this.props.dashboardState.dashboard.main.cards}
                                 isDashboardInlineErrorRender={this.props.dashboardState.dashboard.main.copyHistory.inlineError.isRender}
                                 tooltip={this.props.dashboardState.tooltip}
                                 restorePopup={this.props.dashboardState.restorePopup}
                                 dashboardStatisticsAppCopies={this.props.dashboardState.dashboard.main.appCopiesSummary}
                                 dashboardCopyHistory={this.props.dashboardState.dashboard.main.copyHistory}
                                 dashboardCopyHistoryRange={this.props.dashboardState.dashboardCopyHistoryRange}
                                 dashboardAppDataDistribution={this.props.dashboardState.dashboard.main.appDataDistribution}
                								 isAvailabilityZoneLoading={this.props.dashboardState.dashboard.main.cards.availabilityZones.isLoading}
                								 sankey={this.props.dashboardState.dashboard.main.sankey}/>

                         : this.props.navigation == 'main' ?
                             <MainScreen
                								 allApps={this.props.wizardState.steps.appMarket.data}
                								 allAppInstances={this.props.allAppInstances}
                								 currentUser={this.props.currentUser}/>

                               :
                               this.props.navigation === ProdNavigationOptions.deployingProgress.location.substring(1) ?

                            <DeployingProgress
                              topologyData={topologyData}
                              instanceState={this.props.deployingProgressState.instanceState}
                              instanceStatus={this.props.deployingProgressState.instanceStatus}
                              deployingProgressState={this.props.deployingProgressState}
                              websockets={this.props.websockets}
                              selectedInstance={this.props.deployingProgressState.selectedInstance}
                              allServices={this.props.appTopologyState.appTopology.application.allServices}
                              selectedElement={this.props.appTopologyState.appTopology.selectedElement}
                              selectedInstance={this.props.selectedInstance}
                              allDependencies={this.props.appTopologyState.appTopology.application.allDependencies}/>
                         :

                         null
                       }
                  </div>
               </div>
           </div>
       )
   }
});

export default MainPane;
