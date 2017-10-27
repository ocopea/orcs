// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-wizard-deploy-prod.scss';
import Locations from '../../../locations.json';
import { hashHistory } from 'react-router';
import { AppMarket, AppTopologyContainer,
         SiteSetup, BusinessSetup, Configuration } from '../../';
import TopologyDataController from '../../../components/AppTopology/controllers/topology-data-controller';
import AppTemplateConfigurationHandler from '../../../models/AppTemplateConfiguration/appTemplate-configuration-handler';
import AppTemplateHandler from '../../../models/AppTemplate/appTemplate-handler';
import _ from 'lodash';
import Helper from '../../../utils/helper';
import WizardDeployError from './errorHandler';


@inject(["stores"])
@observer
export default class WizardDeployProd extends React.Component{


  constructor(props){
    super(props);
    props.stores.ui.showMainMenu(true);
    const steps = Locations.production.wizardDeploy.steps;
    const keys = _.keys(steps);
    const currentLocation = props.stores.ui.currentLocation.pathname;
    const currentStep = Helper.getLocationByPathname(currentLocation);

    this.state = {
      steps: steps,
      keys: keys,
      currentStep: currentStep,
      selectedAppTemplate: {},
      siteSetup: {},
      instanceName: "",
      businessPlan: {},
      selectedSite: {},
      purpose: {},
      space: {},
      error: ""
    }

  }

  render(){

    const { t, stores } = this.props;
    const currentStep = this.state.currentStep;
    const isValid = this.isStepValid(currentStep);
    const isStepValid = typeof isValid === "boolean" && isValid;

    return(
      <div className={styles.WizardDeployProd}>
        <div className={styles.container}>

          {
            React.Children.map(this.props.children, child => {
                return React.cloneElement(child, {
                  sites: stores.data.sites.slice(),
                  site: stores.data.sites.slice()[0] || {},
                  appTemplates: stores.data.appTemplates.slice(),
                  selectedAppTemplate: this.state.selectedAppTemplate,
                  onSetSelectedAppTemplate: this.setWizardState.bind(this, 'selectedAppTemplate'),
                  onBusinessPlanSelection: this.setWizardState.bind(this, 'businessPlan'),
                  onSetSelectedSiteSetup: this.setWizardState.bind(this, 'siteSetup'),
                  onSetInstanceName: this.setWizardState.bind(this, 'instanceName'),
                  onSetSelectedSite: this.setWizardState.bind(this, 'selectedSite'),
                  onSetSelectedPurpose: this.setWizardState.bind(this, 'purpose'),
                  onSetSelectedSpace: this.setWizardState.bind(this, 'space'),
                })
            })
          }

          <div className={styles.footer}>
            <div className={styles.inlineError}>{this.state.error}</div>
            <div className={styles.buttons}>
              <button
                className={!this.isFirstStep() ? styles.btnBack : styles.btnBackDisabled}
                onClick={this.onBackClick.bind(this)}>
                  {
                    !this.isFirstStep() ?
                      <span className="icon-arrow-right"></span>
                    : null
                  }
                  <span>back</span>
              </button>
              <button
                className={isStepValid ? styles.btnNext : styles.btnNextDisabled}
                onClick={this.onNextClick.bind(this)}>
                  <span className="icon-arrow-right"></span>
                  {
                    this.state.currentStep.pathname ===
                      Locations.production.wizardDeploy.steps.configuration.pathname ?
                        <span>deploy</span>
                    :
                    <span>next</span>
                  }
              </button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  isStepValid(step) {
    const key = step.key;
    const errorTypes = new WizardDeployError().types;

    switch (key) {
      case Locations.production.wizardDeploy.steps.appMarket.key:
        return !_.isEmpty(this.state.selectedAppTemplate) || new WizardDeployError(errorTypes.missingAppTemplate);
        break;
      case Locations.production.wizardDeploy.steps.appTopology.key:
        return true;
        break;
      case Locations.production.wizardDeploy.steps.siteSetup.key:
        return !_.isEmpty(this.state.siteSetup) || new WizardDeployError(errorTypes.missingSiteSetup);
        break;
      case Locations.production.wizardDeploy.steps.businessSetup.key:
        return !_.isEmpty(this.state.businessPlan) || new WizardDeployError(errorTypes.missingBusinessPlan);
        break;
      case Locations.production.wizardDeploy.steps.configuration.key:
        if(!this.state.instanceName.length) {
          return new WizardDeployError(errorTypes.missingInstanceName);
        } else {
          return true;
        }
        break;
    }
  }

  isFirstStep() {
    return this.state.keys.indexOf(this.state.currentStep.key) === 0;
  }

  isLastStep() {
    return this.state.keys.indexOf(this.state.currentStep.key) ===
      this.state.keys.length - 1;
  }

  setStep(stepKey) {
    const step = this.state.steps[stepKey];
    if(step){
      this.setState({
        currentStep: step
      });
      hashHistory.push(step.pathname);
    }
  }

  setWizardState(key, value) {
    if(this.state[key] !== undefined) {
      this.state[key] = value;
      this.state.error = "";
      this.forceUpdate();
    }
  }

  onNextClick(e) {
    const currentStepKey = this.state.currentStep.key;
    const keys = this.state.keys;
    const currentStepIndex = keys.indexOf(currentStepKey);
    const nextStep = keys[currentStepIndex + 1];
    const currentStep = this.state.currentStep;
    const isValid = this.isStepValid(currentStep);
    if(typeof isValid === "boolean" && isValid) {
      this.setStep(nextStep);
      if(this.isLastStep()) {
        this.deploy();
      }
    }else {
      this.setState({ error: isValid.error });
    }
  }

  onBackClick() {
    const currentStepKey = this.state.currentStep.key;
    const keys = this.state.keys;
    const currentStepIndex = keys.indexOf(currentStepKey);
    const nextStep = keys[currentStepIndex - 1];
    this.setStep(nextStep);
  }

  deploy() {
    const dataStore = this.props.stores.data;
    const selectedSite = !_.isEmpty(this.state.selectedSite) ?
                          this.state.selectedSite : dataStore.sites[0];
    const siteId = selectedSite.id;
    const appTemplate = this.state.selectedAppTemplate;
    const appTemplateId = appTemplate.id;
    const appInstanceName = this.state.instanceName;
    const topologyData = TopologyDataController.getData();
    const deploymentPlan = this.getDeploymentPlan(topologyData, selectedSite);

    const data = {
      "appInstanceName": appInstanceName,
      "appTemplateId": appTemplateId,
      "siteId": siteId,
      "deploymentPlan": deploymentPlan,
      "dataProtectionSetup":{
				"policyName": this.state.businessPlan.name,
				"policySettings":{}
			},
			"siteSetup":{
				"policyName": this.state.siteSetup.name,
				"policySettings":{}
			}
    };

    AppTemplateHandler.deployAppTemplate(data)

  }

  getDeploymentPlan(topologyData, site) {
    const configuration = AppTemplateConfigurationHandler.configuration || {};
		const appServices = topologyData.appServices;
		const dataServices = topologyData.dataServices;
		const dataServicesConfig = configuration.dataServiceConfigurations;

		let parsedAppServices = {};
		let parsedDataServices = {};

		// parse deployment plan services
		appServices.forEach(appService => {
			const serviceName = appService.name;
			parsedAppServices[serviceName] = {
				space: !_.isEmpty(this.state.space) ? this.state.space : site.spaces[0],
				enabled: appService.isActive,
				imageVersion: appService.version,
				artifactRegistryName: 'shpanRegistry'
			};
		});

		// parse deployment plan dependencies
		dataServices.forEach(dataService => {
			const configDataService = this.getDataServiceByName(dataService.name, dataServicesConfig);
			const dsbPlans = configDataService.dsbPlans;
			const defaultDsbPlan = dsbPlans[0];
			const defaultPlan = defaultDsbPlan.plans[0];
			const plan = dataService.selectedPlan.name || defaultPlan.name;
			const dsbURN = dataService.selectedPlan.service || defaultDsbPlan.name;
      const selectedPlanProtocols = dataService.selectedPlan.protocols || [];
      const defaultPlanProtocols = defaultDsbPlan.protocols || [];
      const dsbProtocol = selectedPlanProtocols[0] || defaultDsbPlan.plans[0].protocols[0];

			parsedDataServices[dataService.name] = {
				dsbURN: dsbURN,
				enabled: dataService.isActive,
				dsbPlan: plan,
                dsbProtocol: dsbProtocol
			};

		});

		return {appServices: parsedAppServices, dataServices: parsedDataServices};
	}

  getDataServiceByName(name, dataServicesConfig) {
		return dataServicesConfig.filter(dataService => {
			return dataService.dataServiceName === name;
		})[0];
	}
}
