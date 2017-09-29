// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import TopologyDataController from '../controllers/topology-data-controller.js';


export default class ServiceSelect extends React.Component{

  constructor(props) {
    super(props)
  }

  userSelectedService(e){
    TopologyDataController.setSelectedPlanService(this.props.selectedElement.id, e)
  }

  parseOptions(services){
    return services.map((service, i)=>{
      return { value: service.name, label: service.name }
    })
  }

  render(){

    var baseClass = `${this.props.className}__service-select`;
    var selectedService = this.props.selectedService

    return(
      <div className={baseClass}>
        <label className={`${baseClass}__label ${this.props.className}__label`}>services:</label>
        <Select
          value={this.props.selectedService.name}
          arrowRenderer={()=>{
              return <span className="icon-arrow-dropdown-down Naz-select__toggle link topology-tt"></span>
            }
          }
          placeholder={null}
          clearable={false}
          searchable={false}
          options={this.parseOptions(this.props.services)}
          onChange={this.userSelectedService.bind(this)}
          className={`Naz-select ${baseClass}__select`}
          optionClassName={`Naz-select__option ${baseClass} ${baseClass}__select__option`} />
        <div
          className={`${baseClass}__description`}
          title={selectedService.description}>
            {selectedService.description}
        </div>
      </div>
    )
  }
}

ServiceSelect.propTypes = {
  services: React.PropTypes.array.isRequired,
  selectedService: React.PropTypes.object.isRequired, // <------- {name:"", description: ""}
  defaultPlan: React.PropTypes.string.isRequired,
  onClick: React.PropTypes.func.isRequired,
  className: React.PropTypes.string.isRequired
}
