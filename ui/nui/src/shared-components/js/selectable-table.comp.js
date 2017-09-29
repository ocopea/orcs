// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';
import $ from 'jquery';

import AppTopologyActions from '../../module-production/js/actions/appTopologyActions.js';
import Select from 'react-select';


var SelectableTable = React.createClass({

  getInitialState: function(){
    return {
      selectedPlan: null,
      selectedService: null,
      services: null,
      selectedServicePlans: []
    }
  },

  componentDidMount: function(){
    console.log(this.props.selectedElement)
  },

  componentDidUpdate: function(nextProps){
    if(this.state.selectedService === null){
      if(this.props.selectedElement.plan === undefined){
        this.setDefaultSelection();
      }else{
        this.setPreviousSelection();
      }
    }
    //console.log(this.state)

  },

  setDefaultSelection: function(){
    this.setDefaultService();
    this.setDefaultPlan();
  },

  setPreviousSelection: function(){
    var plan = this.props.selectedElement.plan,
        planService = plan.service
        planName = plan.name,
        services = this.parseServicesOptions(Object.keys(this.props.data)),
        parsedPlansObject = this.getPlansArrayFromObject(this.props.data[planService].plans),
        parsedPlans = parsedPlansObject !== undefined ? parsedPlansObject : {},
        selectedPlan = _.filter(parsedPlans, p=>{return p.name===planName})[0];

    this.setState({
      selectedService: planService,
      selectedPlan: selectedPlan,
      services: services
    });

  },

  setDefaultService: function(){
    var firstService = Object.keys(this.props.data)[0];
    this.setState({
      services: this.parseServicesOptions(Object.keys(this.props.data)),
      selectedService: firstService
    });

    this.userSelectedService(this.parseServicesOptions(Object.keys(this.props.data))[0]);
  },

  setDefaultPlan: function(){
    var firstService = Object.keys(this.props.data)[0];
    var firstPlan = this.getPlansArrayFromObject(this.props.data[firstService].plans)[0];

    this.setState({
      selectedPlan: firstPlan
    });


    this.userSelectedPlan(firstPlan, firstService);
    this.saveToObject(firstPlan.name, firstService)
  },

  parseServicesOptions: function(options){
    return _.map(options, option=>{
      return {value: option, label: option}
    });
  },

  getDescription: function(){
    return this.state.selectedPlan !== null && this.state.selectedPlan !== undefined ?
           this.state.selectedPlan.description : ""
  },

  getPlans: function(){
    var selectedService = this.props.data[this.state.selectedService],
        plans = selectedService !== undefined ? selectedService.plans : [],
        that = this;

    //get plans array from plans object and set state plans
    var parsedPlans = this.getPlansArrayFromObject(plans);
    // iterate plans to return list
    return _.map(parsedPlans, (plan, i)=>{
      var isSelected = that.state.selectedPlan !== undefined ?
                       that.state.selectedPlan.name === plan.name : "";
      return  <li
                key={i}
                className={that.getItemClassName(i, plan.name)}
                onClick={this.userSelectedPlan.bind(that, plan)}>
                  {plan.name}
              </li>
    });
  },

  getPlansArrayFromObject: function(plansObject){
    if(!_.isEmpty(plansObject)){
      var plans = _.filter(plansObject, o=>{
        return typeof o === 'object';
      })
      return plans[0];
    }else{
      return [];
    }
  },

  userSelectedPlan: function(plan, service){

    this.setState({
      selectedPlan: plan
    });

    var service = this.state.selectedService === null ?
                  service : this.state.selectedService;

    // console.log('userSelectedPlan(), plan:', plan.name)
    // console.log('userSelectedPlan(), service:', service)
    // console.log('userSelectedPlan(), this.props.selectedElement.name:', this.props.selectedElement.name)
    this.saveToObject(plan.name, service);

  },

  saveToObject: function(planName, serviceName){
    AppTopologyActions.setSelectedElementPlan(
          { name: planName, service: serviceName },
          this.props.selectedElement.relatedServiceId[0],
          this.props.selectedElement.index
        );
  },

  userSelectedService: function(e){

    this.setState({
      selectedService: e.value
    });

    this.setDefaultPlan();

    var defaultPlan = this.getPlansArrayFromObject(this.props.data[e.value].plans)[0];

    this.userSelectedPlan(defaultPlan, e.value)
  },

  getItemClassName: function(count, key){

    var data = this.props.data,
        selectedService = this.state.selectedService,
        selectedPlan = this.state.selectedPlan,
        selectedPlanName = selectedPlan !== undefined ? selectedPlan.name : "",
        isLastPlan = count === data[selectedService].plans.length,
        isSelected = key === selectedPlanName,
        baseItemClassNames =    "Selectable-table__bottom-container__plans-list__item " +
                                "topology-tt "+
                                "link "+
                                "Selectable-table__bottom-container__plans-list__item",
        lastItemClassName =     "Selectable-table__bottom-container__plans-list__item--last",
        selectedItemClassName = "Selectable-table__bottom-container__plans-list__item__selected"

    if(isSelected){
       if(isLastPlan){
         return `${baseItemClassNames} ${lastItemClassName} ${selectedItemClassName}`;
       }else{
         return `${baseItemClassNames} ${selectedItemClassName}`;
       }
    }else{
      if(isLastPlan){
        return `${baseItemClassNames} ${lastItemClassName}`;
      }else{
        return `${baseItemClassNames}`;
      }
    }
  },

  render: function(){

    var selectedService, plansObject, plans = [];
    if(this.state.selectedService !== null){
      selectedService = this.props.data[this.state.selectedService],
      plansObject = selectedService === undefined ? {} : selectedService.plans,
      plans = this.getPlansArrayFromObject(plansObject);
    }

    return(
      <div className="Selectable-table topology-tt">

        <div className="Selectable-table__top-container topology-tt">
          <div className="Topology-tooltip__inside__row__label topology-tt">
            services
          </div>
          <Select
             value={this.state.selectedService}
             className={"Naz-select Selectable-table__services-select topology-tt"}
             arrowRenderer={
               function(){
                 return <span
                           className={"icon-arrow-dropdown-down Naz-select__toggle link topology-tt"}></span>
               }
             }
             placeholder={null}
             clearable={false}
             onChange={this.userSelectedService}
             options={this.state.services}
             optionClassName="Naz-select__option topology-tt">
          </Select>
        {/* /Selectable-table__top-container */}
        </div>

        <div className="Topology-tooltip__inside__row__label topology-tt">
          plans
        </div>

        <div className="Selectable-table__bottom-container">

          <ul className="Selectable-table__bottom-container__plans-list">
            {this.getPlans()}
          </ul>
          <div className="Selectable-table__bottom-container__description topology-tt"
                style={plans === undefined ? null : {height:plans.length*42+3}}>
              {this.getDescription()}
          </div>
        {/* /Selectable-table__bottom-container */}
        </div>
      {/*  /Selectable-table */}
      </div>
    )
  }
});

SelectableTable.propTypes = {
  data: React.PropTypes.object.isRequired
}

export default SelectableTable;
