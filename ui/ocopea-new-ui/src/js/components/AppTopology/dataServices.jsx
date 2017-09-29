import React from 'react';
import { observer } from 'mobx-react';


// components
import Element from './element.jsx';

// helpers
import componentTypes from './data/componentTypes.json';

// vendors
import $ from 'jquery';


@observer
export default class DataServices extends React.Component {

  render(){

    const { dataStore, uiStore, instanceState } = this.props;

    const containerStyle = {
      height: uiStore.containerHeight/2
    };

    return(
      <div style={containerStyle} className="container">
        <div className={this.getClassName()}>
          {
            dataStore.dependencies.map(dependency=>{
              const alerts = this.getAlerts(dependency)
              return <Element
                        key =           {dependency.id}
                        componentType = {componentTypes.dependency}
                        name =          {dependency.name}
                        imgSrc =        {dependency.imgSrc}
                        iconSrc =       {dependency.iconSrc}
                        id =            {dependency.id}
                        state =         {dependency.state}
                        isActive =      {dependency.isActive}
                        onClick =       {this.userClickOnDependency.bind(this, dependency.id)}
                        width =         {uiStore.elementSize.dependency.width}
                        height =        {uiStore.elementSize.dependency.height}
                        showLines =     {uiStore.showLines}
                        alerts =        {alerts}
                        uiStore =       {uiStore}
                        instanceState = {instanceState} />
            })
          }
        </div>
      </div>
    )
  }

  constructor(props) {
    super(props);
  }

  getClassName() {
    const baseClass = 'dragscroll Topology__data-services';
    return this.props.uiStore.scroll.dependency ? `${baseClass} scrollable` : baseClass;
  }

  userClickOnDependency(id){
    let selectedElement = this.props.dataStore.parsedData.dataServices[id];
    if(selectedElement){
      this.props.uiStore.setSelectedElement(selectedElement);
    }

  }

  getAlerts(dependency) {
    let alerts = [];
    const config = this.getConfigurationByName(dependency.name)
    if(config && _.isEmpty(config.dsbPlans)) {
      const noDsbPlans = 'no dsb plans';
      alerts.push(noDsbPlans);
    }
    return alerts;
  }

  getConfigurationByName(name) {
    const configuration = this.props.configuration || {};
    const dataServiceConfigurations = configuration.dataServiceConfigurations;
    if(dataServiceConfigurations){
      return _.filter(dataServiceConfigurations, config=>{
        return config.dataServiceName === name;
      })[0];
    }
  }

};

DataServices.propTypes = {
  uiStore: React.PropTypes.object.isRequired,
  dataStore: React.PropTypes.object.isRequired
}
