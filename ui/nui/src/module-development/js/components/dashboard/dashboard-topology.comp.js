// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import TopologyTitle from './dashboard-topology-title.comp.js';
import AppTopology from '../../../../module-production/js/components/wizard/appTopology/appTopology.comp.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import AppTopologyActions from '../../../../module-production/js/actions/appTopologyActions.js';
import DevTopology from '../dev-topology.comp.js';
import SettingsIcon from '../../../../module-production/assets/images/header/settings.png';
import LoadingGif from '../../../../module-production/assets/gif/component-loading.gif';
import _ from 'lodash';


var DasboardTopology = React.createClass({

  topologyProps: function(){
    var props = {
      module: DevNavigationOptions.module,
      selectedApp: this.props.data,
      topologyState: this.props.topologyState.appTopology,
      currentStepName: 'show',
      servicesTranslate: this.props.topologyState.appTopology.services.translate,
      dependenciesTranslate: this.props.topologyState.appTopology.dependencies.translate,
      allServices: this.props.topologyState.appTopology.application.allServices,
      allDependencies: this.props.topologyState.appTopology.application.allDependencies,
      selectedElement: this.props.topologyState.appTopology.selectedElement,
      selectedService: this.props.topologyState.appTopology.services.selectedService.service,
      selectedDependency: this.props.topologyState.appTopology.dependencies.selectedDependency.dependency,
      activeElements: {services:this.props.topologyState.appTopology.application.allServices,
                        dependencies:this.props.topologyState.appTopology.application.allDependencies}
    }
    return props;
  },

  isLoading: function(){
    return _.isEmpty(this.topologyProps().selectedApp)
  },

  render: function(){
    var props = this.topologyProps();
    //console.log(this.props.data)
    return(
      <div>
      {
        this.isLoading() ?
          <img src={LoadingGif} id="component-loading-gif"/>
        :
        <div className="Dashboard__inside__topology">
          <div className="Dashboard__inside__topology__dynamic-load-balancer">
            <span className="Dashboard__inside__topology__dynamic-load-balancer__span">
              dynamic load balancer
            </span>
            <img
              src={SettingsIcon}
              className="Dashboard__inside__topology__dynamic-load-balancer__settings-icon link"/>
          {/* /Dashboard__inside__topology__dynamic-load-balancer */}
          </div>
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
          />
        {/* /Dashboard__inside__topology */}
        </div>
      }
      </div>
    )
  }
});

export default DasboardTopology;
