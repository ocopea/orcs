// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import PlansSelection from './_plans-selection.comp.js';
import AppTopologyActions from '../../../../module-production/js/actions/appTopologyActions.js';


var PlansSelectionHome = React.createClass({

  getInitialState() {
    return {
      selectedPlan: {}
    }
  },

  componentDidUpdate(nextProps){
    if(this.props.selectedElementPlanService !== nextProps.selectedElementPlanService){
      this.setState({
        selectedPlan: this.props.selectedElement.plan
      });
    }

    // AppTopologyActions.setSelectedElementPlan(
    //   { name: that.props.defaultPlan, service: e.value },
    //   serviceIndex,
    //   dependencyIndex
    // );

  },

  getServices(){
    return _.map(this.props.config.dsbPlans, (value, key)=>{
      var service = this.props.config.dsbPlans[key];
      // console.log(service)
      return {name: service.name, description: service.description, plans: service.plans};
    });
  },

  getPlans(){
    return this.getSelectedService() ? this.getSelectedService().plans : [];
  },

  getSelectedPlan(){
    var that = this;
    if(that.props.selectedElement.plan !== undefined){
      return _.filter(this.getPlans(), plan=>{
        return plan.name === that.props.selectedElement.plan.name;
      })[0];
    }else{
      return this.getPlans()[0];
    }
  },

  getSelectedService(){
    var that = this;
    var services = this.getServices();

    if(that.props.selectedElement.plan !== undefined){
      var selectedService = _.filter(services, service=>{
        return service.name === that.props.selectedElement.plan.service;
      });
      return selectedService[0];
    }else{
      return this.getServices()[0];
    }
  },

  render(){

    return(
      <main>
      {
        this.getPlans().length > 0 ?
          <PlansSelection
            config = { this.props.config }
            selectedElement = { this.props.selectedElement }
            plans = { this.getPlans() }
            services = { this.getServices() }
            selectedPlan = { this.getSelectedPlan() || null }
            defaultPlan = { this.getSelectedService().plans[0] }
            selectedService = { this.getSelectedService() }/>
        :
        null
      }
      </main>
    )

  }
});

PlansSelectionHome.propTypes = {
  config: React.PropTypes.object.isRequired,
  selectedElement: React.PropTypes.object.isRequired
}

export default PlansSelectionHome;
