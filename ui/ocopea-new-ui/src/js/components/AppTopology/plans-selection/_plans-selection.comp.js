// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ServiceSelect from './service-select.comp.js';
import PlansList from './plans-list.comp.js';
import Description from './description.comp.js';
import { observer } from 'mobx-react';


@observer
export default class PlansSelection extends React.Component{

  constructor(props) {
    super(props)
  }

  render(){
    let selectedPlan;
    if(!this.props.selectedPlan){
      selectedPlan = this.props.defaultPlan;
    }else{
      selectedPlan = this.props.selectedPlan;
    }

    var baseClass = 'topology-tt Plans-selection'

    return(
      <div className={`${baseClass}`}>
        <ServiceSelect
          className={baseClass}
          selectedService={this.props.selectedService}
          services={this.props.services}
          defaultPlan={this.props.defaultPlan.name}
          selectedElement={this.props.selectedElement}
          onClick={()=>{console.log('click')}}/>
        <div className={`${baseClass}__plans`}>
          <label className={`${baseClass}__label`}>plans: </label>
          <div className={`${baseClass}__plans__wrapper`}>
            <PlansList
              className={baseClass}
              plans={this.props.plans}
              selectedPlan={selectedPlan}
              selectedService={this.props.selectedService}
              selectedElement={this.props.selectedElement}
              onClick={this.props.onPlanSelection.bind(this)}/>
            <Description
              className={baseClass}
              plansCount={this.props.plans.length}
              text={selectedPlan.description}/>
          </div>
        {/* /Plans-selection__plans */}
        </div>
      {/* /Plans-selection */}
      </div>
    )
  }
}


PlansSelection.propTypes = {
  config: React.PropTypes.object.isRequired,
  selectedElement: React.PropTypes.object.isRequired,
  services: React.PropTypes.array.isRequired,
  plans: React.PropTypes.array.isRequired,
  defaultPlan: React.PropTypes.object.isRequired, // <------- {name:"", description: ""}
  selectedPlan: React.PropTypes.object, // <------- {name:"", description: ""}
  selectedService: React.PropTypes.object.isRequired // <------- {name:"", description: ""}
}

/**
 *  expected config object
 */
// var config = {
//   dataServiceName: "",
//   dsbPlans: {
//     name: "", // <-----------service
//     description: "",
//     plans: [
//       {name:"",description:""}
//     ]
//   }
// }
