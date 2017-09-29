// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


var PlansList = React.createClass({

  getPlans: function(baseClass){
    return this.props.plans.map((plan, i)=>{
      var isSelected = this.props.selectedPlan.name === plan.name;
      var isLast = i === this.props.plans.length - 1;

      return <li
              key={i}
              onClick={this.userselectedPlan}
              className={this.planClassName(baseClass, isSelected, isLast)}>{plan.name}</li>
    });
  },

  planClassName: function(baseClass, isSelected, isLast){
    if(isSelected){
      if(isLast){
        return `${baseClass}__option--last ${baseClass}__option__selected`;
      }else{
        return `${baseClass}__option__selected`;
      }
    }else{
      if(isLast){
        return `${baseClass}__option ${baseClass}__option--last`;
      }else{
        return `${baseClass}__option`;
      }
    }
  },

  userselectedPlan(e){
    var dependencyIndex = this.props.selectedElement.index,
        serviceIndex = this.props.selectedElement.relatedServiceId[0];

    this.props.onClick(
      { name: e.target.innerHTML, service: this.props.selectedService.name },
      serviceIndex,
      dependencyIndex
    )
  },

  render(){
    var baseClass = `${this.props.className}__plans__plans-list`;
    return(
      <div className={baseClass}>
        <ul className={`${baseClass}__list`}>
          {this.getPlans(baseClass)}
        </ul>
      </div>
    )
  }
});

PlansList.propTypes = {
  plans: React.PropTypes.array.isRequired,
  selectedPlan: React.PropTypes.object.isRequired, // <------- {name:"", description: ""}
  selectedService: React.PropTypes.object.isRequired, // <------- {name:"", description: ""}
  onClick: React.PropTypes.func.isRequired,
  className: React.PropTypes.string.isRequired
}

export default PlansList;
