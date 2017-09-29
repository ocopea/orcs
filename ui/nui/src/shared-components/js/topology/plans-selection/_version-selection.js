// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import TopologyDataController from '../controllers/topology-data-controller.js';
import { observer } from 'mobx-react';


@observer
class VersionSelection extends React.Component{

  constructor(props){
    super(props)
    this.state = {
      version: this.props.version
    }
  }

  render() {
    const isActive = this.props.selectedElement.isActive;
    return (
      <div className="Version-selection">
        <label style={!isActive ? {opacity:'0.5'}:null}>select version</label>
        <Select
          value={this.state.version}
          clearable={false}
          searchable={false}
          options={this.getOptions()}
          onChange={this.onChange.bind(this)}
          disabled={!isActive} />
      </div>
    )
  }

  onChange(e) {
    TopologyDataController.setElementVersion(this.props.selectedElement.id, e.value);
    this.setState({
      version: e.value
    })
  }

  getOptions() {
    return this.props.versions.map(version=>{
      return {value: version, label: version}
    })
  }
}

export default VersionSelection;
