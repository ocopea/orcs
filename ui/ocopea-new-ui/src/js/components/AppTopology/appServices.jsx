import React from 'react';
import { observer } from 'mobx-react';
import _ from 'lodash';


// components
import Element from './element.jsx';

// helpers
import componentTypes from './data/componentTypes.json';
import shakeElement from './helpers/shake.js';

// vendor
import $ from 'jquery';

@observer
export default class AppServices extends React.Component {

  render(){

    const { dataStore, uiStore, instanceState } = this.props;

    const elementsWidthDiff = uiStore.elementSize.service.height -
                              uiStore.elementSize.dependency.height;
    const scroll = uiStore.scroll.service || uiStore.scroll.dependency;

    const margin = !scroll ? uiStore.marginBetweenElements + elementsWidthDiff :
                             elementsWidthDiff;

    const style = {
      height: uiStore.containerHeight/2 + margin
    }

    return(
      <div style={style} className="container">
        <div className={this.getClassName()}>
          {
            dataStore.services.map(service=>{
              const alerts = this.getAlerts(service)
              return <Element
                        key =           {service.id}
                        componentType = {componentTypes.service}
                        name =          {service.name}
                        imgSrc =        {service.imgSrc}
                        iconSrc =       {service.iconSrc}
                        id =            {service.id}
                        state =         {service.state}
                        isActive =      {service.isActive}
                        onClick =       {this.userClickOnService.bind(this, service.id)}
                        width =         {uiStore.elementSize.service.width}
                        height =        {uiStore.elementSize.service.height}
                        version =       {service.version}
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

  getAlerts(service) {
    let alerts = [];
    const config = this.getConfigurationByName(service.name)

    // check if service has data services
    const relatedDependencies = this.props.uiStore.getRelatedDependencies(service);
    if(_.isEmpty(relatedDependencies)){
      const noDataServices = 'no data services';
      alerts.push(noDataServices);
    }

    if(config && _.isEmpty(config.supportedVersions)) {
      const noVersions = 'no versions';
      alerts.push(noVersions);
    }

    return alerts;
  }

  getConfigurationByName(name) {
    const configuration = this.props.configuration || {};
    const appServiceConfigurations = configuration.appServiceConfigurations;
    if(appServiceConfigurations){
      return _.filter(appServiceConfigurations, config=>{
        return config ? config.appServiceName === name : '';
      })[0];
    }
  }

  userClickOnService(id){

    let selectedElement = this.props.dataStore.parsedData.appServices[id];
    if(selectedElement){
      if(selectedElement.serviceBindings.length){
        this.props.uiStore.setSelectedElement(selectedElement);
      }else{
        let domElement = document.getElementById(selectedElement.id);
        shakeElement(domElement);
      }
    }

  }

  getClassName() {
    const baseClass = 'dragscroll Topology__app-services';
    return this.props.uiStore.scroll.service ? `${baseClass} scrollable` : baseClass;
  }

};

AppServices.propTypes = {
  uiStore: React.PropTypes.object.isRequired,
  dataStore: React.PropTypes.object.isRequired
}
