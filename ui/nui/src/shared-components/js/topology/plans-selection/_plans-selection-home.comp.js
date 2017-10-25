// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import PlansSelection from './_plans-selection.comp.js';
import componentTypes from '../data/componentTypes.json';
import UiController from '../controllers/topology-ui-controller.js';
import DataController from '../controllers/topology-data-controller.js';
import VersionSelection from './_version-selection.js';
import Switch from '../../../../module-development/js/components/wizard/config/switch.comp.js';
import {observer} from 'mobx-react';


@observer
class PlansSelectionHome extends React.Component{

  constructor(props) {
    super(props)
    this.state = {
      selectedPlan: {}
    }
  }

  componentDidUpdate(nextProps){
    if(this.props.selectedElementPlanService !== nextProps.selectedElementPlanService){
      this.setState({
        selectedPlan: this.props.selectedElement.selectedPlan
      });
    }
  }

  getServices(selectedConfig){
    if(selectedConfig && !_.isEmpty(selectedConfig)){
      return _.map(selectedConfig.dsbPlans, (value, key)=>{
        var service = selectedConfig.dsbPlans[key];
        return {name: service.name, description: service.description, plans: service.plans};
      });
    }else{
      console.log('_plans-selection-home- '+
                  'config props must be valid, supplied: ', selectedConfig)
    }
  }

  getPlans(selectedConfig){
    return this.getSelectedService(selectedConfig) ? this.getSelectedService(selectedConfig).plans : [];
  }

  getSelectedPlan(selectedConfig){
    var that = this;
    if(that.props.selectedElement.selectedPlan !== undefined){
      return _.filter(this.getPlans(selectedConfig), plan=>{
        return plan.name === that.props.selectedElement.selectedPlan.name;
      })[0];
    }else{
      return this.getPlans(selectedConfig)[0];
    }
  }

  getSelectedService(selectedConfig){
    var that = this;
    var services = this.getServices(selectedConfig);
    var selectedElement = this.props.selectedElement;
    const selectedPlan = that.props.selectedElement.selectedPlan;

    if(selectedElement && !_.isEmpty(selectedElement)){
      if(selectedPlan && selectedPlan.service){
        var selectedService = _.filter(services, service=>{
          return service.name === that.props.selectedElement.selectedPlan.service;
        });
        return selectedService[0];
      }else{
        return services ? services[0] : null;
      }
    }else{
      console.log('_plans-selection-home- '+
                  'selectedElement props must be valid, supplied: ', selectedElement)
    }
  }

  getSelectedConfiguration(selectedElement) {
    const componentType = this.props.selectedElement.componentType;
    const name = this.props.selectedElement.name;
    const dict = {}
    dict[componentTypes.service] = 'appServiceConfigurations';
    dict[componentTypes.dependency] = 'dataServiceConfigurations';
    const selectedConfiguration = this.props.config[dict[componentType]];
    let selected = {};

    if(Array.isArray(selectedConfiguration)){
      selected = selectedConfiguration.filter(config => {
        const name = config.dataServiceName || config.appServiceName;
        return name === this.props.selectedElement.name;
      })[0];
    }

    return selected;
  }

  render(){
    const selectedConfig = this.getSelectedConfiguration();
    const services = this.getServices(selectedConfig);
    const selectedService = this.getSelectedService(selectedConfig);
    const defaultPlan = selectedService ? selectedService.plans[0] : null;
    const selectedPlan = this.getSelectedPlan(selectedConfig);
    const plans = this.getPlans(selectedConfig);
    const position = this.props.position;
    const componentType = this.props.selectedElement.componentType;
    const style = {top: position.top, left: position.left};
    const versions = selectedConfig ? selectedConfig.supportedVersions : null;
    const registry = versions ? Object.keys(versions)[0] : null;
    const supportedVersions = versions ? versions[registry] : null;

    return(
      <main className={`topology-tt Topology-menu ${componentType}`} style={style}>
        <div className="topology-tt Topology-menu__title">
          <span>{this.props.selectedElement.name}</span>
          <Switch
            onSwitch={this.onSwitch.bind(this)}
            selectedElement={this.props.selectedElement}/>
        </div>
        {
          componentType === componentTypes.dependency &&
          plans.length > 0 ?
            <PlansSelection
              config = { this.props.config }
              selectedElement = { this.props.selectedElement }
              plans = { plans }
              services = { services }
              selectedPlan = { selectedPlan }
              defaultPlan = { defaultPlan }
              selectedService = { selectedService }/>
          :
          this.props.selectedElement.componentType ===
            componentTypes.service && supportedVersions ?
            <VersionSelection
              selectedElement={this.props.selectedElement}
              version={this.props.selectedElement.version}
              versions={supportedVersions}/>
          :
          null
        }
      </main>
    )

  }

  onSwitch() {
    DataController.toggleElementActivation(this.props.selectedElement.id);
  }
};

PlansSelectionHome.propTypes = {
  config: React.PropTypes.object.isRequired,
  selectedElement: React.PropTypes.object.isRequired
}

export default PlansSelectionHome;
