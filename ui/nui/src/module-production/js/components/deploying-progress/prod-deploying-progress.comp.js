// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevTopology from '../../../../module-development/js/components/dev-topology.comp.js';
import ProdNavigationOptions from '../../data/prodNavigationOptions.js';
import TopologyParser from '../../../../shared-components/js/topology-data-parser.js';
import DevActions from '../../../../module-development/js/actions/dev-actions.js';
import ProgressBarQuantity from '../../../../shared-components/js/progress-bar-quantity.comp.js';
import SharedActions from '../../../../shared-actions.js';
import ProdActions from '../../actions/actions.js';
import AppTopologyActions from '../../actions/appTopologyActions.js';
import Config from '../../config.js';
import DevDashboardActions from '../../../../module-development/js/actions/dev-dashboard-actions.js';
import DeployingProgressActions from '../../../../module-development/js/actions/deploying-progress-actions.js';
import stateOptions from '../../../../module-development/js/data/deploying-state-options.js';
import Loader from '../../../../module-development/assets/loader.gif';
import Logs from '../../../../module-development/js/components/deploying-progress/logs.comp.js';
import $ from 'jquery';
import _ from 'lodash';

const ProdDeployingProgress = React.createClass({

  render() {

    const baseClass = 'Prod-deploying-progress';
    const topologyProps = this.topologyProps();
    let instanceStatus = this.props.deployingProgressState.instanceStatus;
    let instanceState = this.props.instanceState || null;
    const props = this.topologyProps();

    let done = instanceStatus.deployedElements,
        unDone = instanceStatus.failedElements,
        allElements = instanceStatus.allElements;

    return(
      <div className={baseClass}>

        <div
          style={{width:'calc(100% - 40px)'}}
          className="Deploying-progress__inside Deploying-progress__inside__top-container">
          <div className="deploying-loader">
            {
             instanceState && this.props.instanceState.toUpperCase() === stateOptions.error.toUpperCase() ?
              <div className="check-container">
                <span className="icon-close"></span>
              </div>
             :
             instanceState && this.props.instanceState.toUpperCase() === stateOptions.running.toUpperCase() ?
              <button className={`btn-go-to-dashboard`} onClick={this.goToDashboard}>go to dashboard</button>
             :
             null
            }
            {instanceStatus.stateMessage}
          </div>
          {
            !this.isLoadingComplete() ?
              <ProgressBarQuantity
                done={done}
                unDone={unDone}
                allElements={allElements}
                height={8}
                width={700}
                deployingState={this.props.selectedInstance.state ? this.props.selectedInstance.state.toUpperCase() : null}
                instanceName={this.props.selectedInstance.appInstanceName}/>
            :
            null
          }
        </div>

         {
           this.props.deployingProgressState.instanceStatus.allElements.length > 0 ?
             <DevTopology
               tooltip={{}}
               activeElements={topologyProps.activeElements}
               module={topologyProps.module}
               selectedElement={topologyProps.selectedElement}
               selectedApp={TopologyParser.parse(this.props.deployingProgressState.selectedInstance)}
               state={topologyProps.topologyState}
               selectedService={topologyProps.selectedService}
               selectedDependency={topologyProps.selectedDependency}
               currentStepName={'show'}
               servicesTranslate={topologyProps.servicesTranslate}
               dependenciesTranslate={topologyProps.dependenciesTranslate}
               allServices={topologyProps.topologyState.application.allServices}
               selectedInstanceState={this.props.deployingProgressState.instanceState}
               instanceStatus={this.props.deployingProgressState.instanceStatus}
               isFetching={this.props.isFetching}/>

           : <img src={Loader} className="Deploying-progress__inside__loader"/>
         }
         {
           this.props.websockets && this.isTopologyLoaded() ?
             <Logs
               websockets={this.props.websockets}
               filters={this.props.deployingProgressState.logs.filters}
               data={this.props.deployingProgressState.logs}/>
           :
           null
         }
      </div>
    )
  },

  goToDashboard() {
    SharedActions.navigate({
      module: ProdNavigationOptions.module,
      location: ProdNavigationOptions.dashboard.location,
      subLocation: this.props.selectedInstance.id
    });
    ProdActions.goToDashboard(this.props.selectedInstance);
    ProdActions.navigateToDashboard();
  },

  isLoadingComplete: function(){
    var instanceState = this.props.instanceState ?
                        this.props.instanceState.toUpperCase() : '';
    return  instanceState === stateOptions.running.toUpperCase() ||
            instanceState === stateOptions.error.toUpperCase()
  },

  isTopologyLoaded: function(){
    return this.topologyProps().selectedApp.appServiceTemplates && !_.isEmpty(this.props.selectedInstance)
  },

  componentDidMount() {
    DevActions.checkState(Config.getCurrentHash().subLocation);
  },

  componentWillUnmount: function(){
    DeployingProgressActions.clearLogs();
    // DeployingProgressActions.initializeSelectedInstance();
    // AppTopologyActions.setSelectedApp({})
    // DevDashboardActions.setSelectedInstance({});
  },

  componentDidUpdate(nextProps) {
    if(!this.firstTIme && this.props.selectedInstance !== nextProps.selectedInstance){
      this.firstTIme = true;
      DevDashboardActions.setSelectedInstance(this.props.selectedInstance);
    }
  },

  topologyProps(){
    var state = this.props.topologyData.appTopologyState.appTopology;

    var props = {
      module: 'development',
      selectedApp: state.application.selectedApp,
      topologyState: state,
      currentStepName: 'show',
      servicesTranslate: state.services.translate,
      dependenciesTranslate: state.dependencies.translate,
      allServices: this.props.allServices,
      allDependencies: this.props.allDependencies,
      selectedElement: this.props.selectedElement,
      selectedService: state.services.selectedService.service,
      selectedDependency: state.dependencies.selectedDependency.dependency,
      activeElements: {services: state.application.allServices,
                       dependencies: state.application.allDependencies}
    }
    return props;
  },

});


export default ProdDeployingProgress;
