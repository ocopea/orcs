// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import Config from '../../../../module-production/js/config.js';


var ServiceSelect = React.createClass({

  userSelectedService(e){
    var that = this;
    var dependencyIndex = this.props.selectedElement.index,
        serviceIndex = this.props.selectedElement.relatedServiceId[0];

    this.props.onClick(
      { name: that.props.defaultPlan, service: e.value },
      serviceIndex,
      dependencyIndex
    );

  },

  parseOptions(services){
    return services.map((service, i)=>{
      return { value: service.name, label: service.name }
    })
  },

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
          onChange={this.userSelectedService}
          className={`Naz-select ${baseClass}__select`}
          optionClassName={`Naz-select__option ${baseClass} ${baseClass}__select__option`} />
        <div
          className={`${baseClass}__description`}
          title={Config.getTitleOrNull(selectedService.description, 80)}>
            {Config.getShortName(selectedService.description, 80)}
        </div>
      </div>
    )
  }
});

ServiceSelect.propTypes = {
  services: React.PropTypes.array.isRequired,
  selectedService: React.PropTypes.object.isRequired, // <------- {name:"", description: ""}
  defaultPlan: React.PropTypes.string.isRequired,
  onClick: React.PropTypes.func.isRequired,
  className: React.PropTypes.string.isRequired
}

export default ServiceSelect;
