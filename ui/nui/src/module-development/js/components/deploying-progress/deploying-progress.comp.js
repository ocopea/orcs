// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevActions from '../../actions/dev-actions.js';
import DeployingProgressActions from '../../actions/deploying-progress-actions.js';
import DevTopology from '../dev-topology.comp.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import DashboardActions from '../../actions/dev-dashboard-actions.js';
import _ from 'lodash';
import Config from '../../../../module-production/js/config.js';
import ProgressBarQuantity from '../../../../shared-components/js/progress-bar-quantity.comp.js';
import Logs from './logs.comp.js';
import stateOptions from '../../data/deploying-state-options.js';
import Loader from '../../../assets/loader.gif';
import $ from 'jquery';


var DeployingPage = React.createClass({

  componentDidMount: function(){
    DevActions.checkState(Config.getCurrentHash().subLocation);
  },

  componentWillUnmount: function(){
    DevActions.hideDialogSavedSuccessfuly();
    DeployingProgressActions.clearLogs();
  },

  topologyProps: function(){
    var state = this.props.topologyState;
    var props = {
      module: DevNavigationOptions.module,
      selectedApp: this.props.topologyState.appTopology.application.selectedApp,
      topologyState: state.appTopology,
      currentStepName: 'show',
      servicesTranslate: state.appTopology.services.translate,
      dependenciesTranslate: state.appTopology.dependencies.translate,
      allServices: state.appTopology.application.allServices,
      allDependencies: state.appTopology.application.allDependencies,
      selectedElement: state.appTopology.selectedElement,
      selectedService: state.appTopology.services.selectedService.service,
      selectedDependency: state.appTopology.dependencies.selectedDependency.dependency,
      activeElements: {services:state.appTopology.application.allServices,
                        dependencies:state.appTopology.application.allDependencies}
    }
    return props;
  },

  isLoadingComplete: function(){
    var instanceState = this.props.state.instanceState ?
                        this.props.state.instanceState.toUpperCase() : '';
    return  instanceState === stateOptions.running.toUpperCase() ||
            instanceState === stateOptions.error.toUpperCase() ||
            instanceState === stateOptions.errorStopping.toUpperCase();
  },

  isTopologyLoaded: function(){
    return  this.props.state.instanceStatus.allElements.length > 0 &&
            this.topologyProps().selectedApp.appServiceTemplates &&
            this.props.state.instanceState;
  },

  showLogs: true,

  shouldLogsRender: function(){
    var isWS = this.props.websockets;
    return this.isTopologyLoaded() &&
           this.showLogs
  },

  deleteInstance() {
    DashboardActions.showConfirmDisposeImageDialog();
  },

  isError() {
    return  this.props.state.instanceState.toUpperCase() ===
              stateOptions.errorStopping.toUpperCase() ||
            this.props.state.instanceState.toUpperCase() ===
              stateOptions.error.toUpperCase()
  },

  render: function(){

    var props = this.topologyProps();
    var done = this.props.state.instanceStatus.deployedElements,
        unDone = this.props.state.instanceStatus.failedElements,
        allElements = this.props.state.instanceStatus.allElements;
        // console.log(this.props)
    return(
      <div className="Deploying-progress">
        <div className="Deploying-progress__inside">
          <div className="Deploying-progress__inside__top-container">
            {
              !this.props.selectedInstanceState ?
                null
              :
              this.isError() ?
                <div className="deploying-loader">
                  <div className="check-container">
                    <span className="icon-close"></span>
                  </div>
                  {this.props.state.instanceStatus.stateMessage}
                  <span onClick={this.deleteInstance} className='icon-delete link'></span>
                </div>
              :
              this.props.state.instanceState !== undefined &&
              this.props.state.instanceState !== null &&
              this.isLoadingComplete() ?
                <div className="Deploying-progress__inside__top-container__success">
                  <div className={"Deploying-progress__inside__top-container__success__check-container"+
                                  " check-container"}>
                    <span className={"Deploying-progress__inside__top-container__success__check-container__icon-check"+
                                     " icon-check"}></span>
                  </div>
                  <span className="Deploying-progress__inside__top-container__success__success-msg">
                    {this.props.selectedInstance.name !== undefined ?
                      Config.getShortName(this.props.selectedInstance.name, 20):null} was deployed successfully</span>
                </div>
              :
              null
            }

            {
              !this.isLoadingComplete() ?
                <ProgressBarQuantity
                  done={done}
                  unDone={unDone}
                  allElements={allElements}
                  height={8}
                  width={700}
                  deployingState={this.props.selectedInstance.state ? this.props.selectedInstance.state.toUpperCase() : null}
                  instanceName={this.props.selectedInstance.name}/>
              :
              null
            }
          </div>
          {
            this.isTopologyLoaded() ?
              <DevTopology
                tooltip={{}}
                activeElements={props.activeElements}
                module={props.module}
                selectedElement={props.selectedElement}
                selectedApp={props.selectedApp}
                state={props.topologyState}
                selectedService={props.selectedService}
                selectedDependency={props.selectedDependency}
                currentStepName={'show'}
                servicesTranslate={props.servicesTranslate}
                dependenciesTranslate={props.dependenciesTranslate}
                allServices={props.allServices}
                selectedInstanceState={this.props.state.instanceState}
                instanceStatus={this.props.state.instanceStatus}
                isFetching={this.props.isFetching}
              />
            : <img src={Loader} className="Deploying-progress__inside__loader"/>
          }

          {
            this.shouldLogsRender() ?
              <Logs
                websockets={this.props.websockets}
                filters={this.props.state.logs.filters}
                data={this.props.state.logs}/>
            :
            null
          }

        {/* /Deploying-progress__inside */}
        </div>
      {/* /Deploying-progress */}
      </div>
    )
  }
});

export default DeployingPage;
