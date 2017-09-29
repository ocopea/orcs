// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import MainStore from '../../stores/main-store.js';

//steps components
import AppMarket from './wizard.appMarket.comp.js';
import AppTopologyMain from './appTopology/_appTopologyMain.comp.js';
import SiteSetup from './wizard.siteSetup.comp.js';
import BusinessSetup from './wizard.BusinessSetup.comp.js';
import Summary from './wizard.summary.comp.js';
import Configuration from './configuration/_configuration.js';

//elements components
import Card from './wizard.appMarket.card.comp.js';
import BtnNxt from './wizard.nxtBtn.comp.js';
import BtnPrev from './wizard.prevBtn.comp.js';
import InlineError from './wizard.inlineError.comp.js';

import Config from '../../config.js';
import ProdNavigationOptions from '../../data/prodNavigationOptions.js';


let Wizard = React.createClass({

    wizardClass:function(){
        if(this.props.isWizard){
            return "show";
        }else{
            return "hide";
        }
    },

    getCards: function(){

        var apps = this.props.steps.appMarket.data;

        var applications = apps.map(function(app, index){
			//console.log(app)
            return <Card key={app.id} data={app} isSelected={app.isSelected}/>
        });

        return applications;

    },

    stepClass: function(stepName){
         if(this.props.main.currentStep != undefined){
            var currentStepName = this.props.main.currentStep.name;

            if(currentStepName == stepName)
                return "show";
            else{
                return "hide";
            }
        }else{
            return "show";
        }
    },

    render: function(){
      const subLocation = Config.getCurrentHash().subLocation;
      const configurationPage = ProdNavigationOptions.wizard.summary.subLocation;
      const appTopologyPage = ProdNavigationOptions.wizard.appTopology.subLocation;
      const configuration = this.props.config.configuration;

        return (
            <div id="wizard-container">
                    <AppMarket
                        currentStepName={this.stepClass.bind(this, 'App Market')()}
                        cards={this.getCards()}/>

                      {
                          subLocation === appTopologyPage ?
                            <AppTopologyMain
                               general={this.props.steps.appTopology}
                               state={this.props.appTopologyState.appTopology}
                               selectedService={this.props.appTopologySelectedService}
                               selectedDependency={this.props.appTopologySelectedDependency}
                               currentStepName={this.stepClass.bind(this, 'App Topology')()}
                               servicesTranslate={this.props.appTopologyServicesTranslate}
                               dependenciesTranslate={this.props.appTopologyDependenciesTranslate}
                               selectedApp={this.props.selectedApp}
        			                 allServices={this.props.appTopologyAllServices}/>
                          : null
                      }

                    <SiteSetup
                        currentStepName={this.stepClass.bind(this, 'Site Setup')()}
                        setupTypes={this.props.siteSetupTypes}
                        selectedSetupType={this.props.selectedSiteSetup}/>

                    <BusinessSetup
                        currentStepName={this.stepClass.bind(this, 'Business Setup')()}
                        selectedPlan={this.props.selectedBusinessContinuitySettings}/>

                      {
                          configurationPage === subLocation ?
                            <Configuration
                              sites={this.props.sites}
                              selectedSite={this.props.config.selectedSite}
                              instance={this.props.selectedApp}
                              configuration={configuration}
                              currentStepName={this.stepClass.bind(this, 'Configuration')()} />
                          : null
                      }

                    <hr/>

                    <div id="main-pane-footer">

                         <BtnNxt data={this.props.main.footer.next}
                             currentStep={this.props.main.currentStep.name}
                             selectedPlan={this.props.selectedBusinessContinuitySettings}
                             selectedSetupType={this.props.selectedSiteSetup}/>

                         <BtnPrev data={this.props.main.footer.prev}
                             currentStep={this.props.main.currentStep.name}/>

                         <InlineError error={this.props.main.footer.inlineError}/>

                    </div>
               </div>
        )
    }
});

export default Wizard;

// <Summary
//     isLoading={this.props.isLoading}
//     currentStepName={this.stepClass.bind(this, 'Summary')()}
//     selectedApp={this.props.selectedApp}
//     selectedInfrastructureService={this.props.selectedInfrastructureService}
//     selectedPlan={this.props.selectedBusinessContinuitySettings}
//     selectedSetupType={this.props.selectedSiteSetup}/>
