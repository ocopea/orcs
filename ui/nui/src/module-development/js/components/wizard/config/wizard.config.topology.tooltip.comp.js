// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

//actions
import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import AppTopologyActions from '../../../../../module-production/js/actions/appTopologyActions.js';

//components
import Switch from './switch.comp.js';
import SelectableTable from '../../../../../shared-components/js/selectable-table.comp.js';
import PlansSelection from '../../plans-selection/_plans-selection-home.comp.js';

//vendors
import _ from 'lodash';
import $ from 'jquery';

import Config from '../../../../../module-production/js/config.js';
import DevNavigationOptions from '../../../data/devNavigationOptions.js';

var TopologyTooltip = React.createClass({

  getInitialState: function(){
    return {
      plans: {},
      versions: []
    }
  },

  componentDidMount: function(){
    this.getConfiguration();
    this.sortVersions();
  },

  isDataService: function(selectedElement){
    return this.props.selectedElement.componentType === 'dependency';
  },

  componentDidUpdate: function(nextProps){
    if(!_.isEmpty(this.props.selectedElement) && this.props.selectedElement !== nextProps.selectedElement){
      this.getConfiguration();
    }
    if(this.props.selectedElementPlan !== nextProps.selectedElementPlan){
      if(this.props.selectedElement.componentType === 'dependency'){
        this.handlePlans();
      }
    }
    this.sortVersions();
  },

  sortVersions: function(value){
    value = this.props.selectedElement.version === undefined ?
            this.props.selectedElement.imageVersion :
            this.props.selectedElement.version;

    this.sortedVersionOptions = _.sortBy(this.sortedVersionOptions, o=>{
      return o !== value;
    });

    $('.Topology-tooltip__inside__row__select.version').val(value);
  },

  sortedVersionOptions: [],

  getVersionOptions: function(){
    // console.log(this.state.versions)
    return this.state.versions.map((option, i)=>{
      return <option className="topology-tt" key={i}>{option}</option>
    });
  },

  userSelectedVersion: function(e){
    DevWizardActions.userChangedSelectedAppServiceVersion(
      e.target.value,
      this.props.selectedElement.serviceId
    );
  },

  getConfiguration: function(){
    switch (this.props.selectedElement.componentType) {
      case 'service':
        this.handleVersion();
        break;
      case 'dependency':
        this.handlePlans();
        break;
    }
  },

  handleVersion: function(){
    var serviceConfig = this.props.configuration.appServiceConfigurations,
        selectedIndex = this.props.selectedElement.serviceId;
    var config = serviceConfig[selectedIndex];
    this.setState({
      versions: config.supportedVersions[Object.keys(config.supportedVersions)[0]] || []
    });
  },

  handlePlans: function(){
    var that = this;
    var dataServiceConfig = this.props.configuration.dataServiceConfigurations,
        selectedIndex = this.props.selectedElement.index;
    var config = _.filter(dataServiceConfig, o=>{
      return o.dataServiceName === this.props.selectedElement.name;
    });

    var keys = Object.keys(config[0].dsbPlans);

    var parsedPlans = {};
    _.map(config[0].dsbPlans, (value, key)=>{
      if(this.props.selectedElement.name === config[0].dataServiceName)
        return parsedPlans[key] = that.parsePlan(value, config[0].dataServiceName);
    });

    this.setState({
      plans: parsedPlans
    });
  },

  parsePlan: function(plan, dataServiceName){

    var parsedPlan = {}
    var plans = _.map(plan, option=>{
      return option;
    });
    parsedPlan.description = plan.description ||`mock description ${this.props.selectedElement.plan}`
    parsedPlan.plans = plans;
    parsedPlan.dataServiceName = dataServiceName;

    return parsedPlan;
  },

  getCurrentConfig: function(configuration, selectedName){
    return this.getDataServiceConfigByName(configuration, selectedName);
  },

  getDataServiceConfigByName: function(configuration, name){
    var config = configuration.filter(c=>{
      return c.dataServiceName === name;
    });
    return config[0];
  },

  render: function(){

    var config = this.getCurrentConfig(
      this.props.configuration.dataServiceConfigurations, this.props.selectedElement.name
    );
    // console.log('this.props.selectedElement.plan: ', this.props.selectedElement.plan)
    // console.log('this.props.configuration: ', this.props.configuration)
    return(
      <div className={"Topology-tooltip topology-tt "+
                      "Topology-tooltip__"+this.props.selectedElement.componentType}
            style={this.props.position}>

        <Switch
          isOn={this.props.switch}
          selectedElement={this.props.selectedElement}
          className="topology-tt"/>

        <div className="Topology-tooltip__title topology-tt">
          { this.props.selectedElement.name === undefined ?
            this.props.selectedElement.appServiceName :
            this.props.selectedElement.name}
        </div>
        <div className="Topology-tooltip__inside topology-tt">
          {
            this.props.selectedElement.componentType == 'service' && this.props.selectedElement.isActive ?
              <div className="Topology-tooltip__inside__row--last topology-tt">
                <div className="Topology-tooltip__inside__row__label topology-tt">
                  version
                </div>

                <select
                  className="Topology-tooltip__inside__row__select topology-tt version"
                  onChange={this.userSelectedVersion}>
                    {this.getVersionOptions()}
                </select>
              </div>
            :
            this.props.selectedElement.componentType == 'dependency' && this.props.selectedElement.isActive ?
            <div className="Topology-tooltip__inside__row--last topology-tt">
              {
                this.isDataService() && config !== undefined ?
                  <PlansSelection
                    config={config}
                    selectedElement={this.props.selectedElement}
                    selectedElementPlanService={this.props.selectedElement.plan} />
                :
                null
              }
            </div>
            :
            null
          }
          {/* /Topology-tooltip__inside */}
        </div>
        {/* /Topology-tooltip */}
      </div>
    )
  }
});

export default TopologyTooltip;

// <PlansSelection
  // config={this.props.configuration}
  // selectedElement={this.props.selectedElement}/>


// <SelectableTable
//     data={this.state.plans}
//     selectedElement={this.props.selectedElement}/>
