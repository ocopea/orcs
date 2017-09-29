// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../../module-production/js/config.js';
import DevWizardActions from '../../../actions/dev-wizard-actions.js';

import _ from 'lodash';
import $ from 'jquery';


var Topology = React.createClass({

  getInitialState: function(){
    var state = {
      types: {
        service: 'service',
        dependency: 'dependency'
      }
    };
    return state;
  },

  componentDidUpdate: function(nextProps){
    //set selected structure class name
    if(this.props.selectedStructure != nextProps.selectedStructure){
      var selectedStructure = this.props.selectedStructure,
          selectedStructureId = this.props.selectedStructure.elementId,
          selectedStructureType = this.props.selectedStructure.elementType;

      //remove selected class from all boxes
      $(".box").each((i, box)=>{
        $(box).removeClass("selected")
      });

      var selectedElement = $(`.box.${selectedStructureType}.${selectedStructureId}`)
      $(selectedElement).addClass("selected");
    }
  },

  getServices: function(){
    var services = this.props.selectedApp.appServiceTemplates;
    return this.getCards(services, this.state.types.service);
  },

  getDependencies: function(){
    var dependencies = this.getDependenciesData();
    return this.getCards(dependencies, this.state.types.dependency);
  },

  getCards: function(array, type){
    var elements = array.map((element, i)=>{
      return <div key={i} className={`box ${type} ${i}`} onClick={this.userSelection}>
        <span className="image-container">
          <img src={Config.fixImgUrl(element.img)}/>
        </span>
        <span className="name-container">{element.name}</span>
      </div>
    });
    return elements;
  },

  getDependenciesData: function(){
    var dependencies = [];
    var filteredDependencies = [];
    this.props.selectedApp.appServiceTemplates.forEach(service=>{
      service.dependencies.forEach(dependency=>{
        var obj = {name: dependency.name, type: dependency.type};
        if(_.find(filteredDependencies, obj) == undefined){
            filteredDependencies.push(dependency)
            dependencies.push(dependency);
        }
      });
    });
    return dependencies;
  },

  userSelection: function(e){
    var type = e.currentTarget.classList[1],
        id = e.currentTarget.classList[2],
        element = {};

    switch (type) {
      case this.state.types.service:
        element = this.props.selectedApp.appServiceTemplates[id];
        break;
      case this.state.types.dependency:
        element = this.getDependenciesData()[id];
        break;
      default:
    }
    element.elementId = id;
    element.elementType = type;
    DevWizardActions.copyDetailsTopologySelection(element);
  },

  render: function(){

    return(
      <div className="topology">
        <div className="services">
          {this.getServices()}
        </div>
        <div className="dependencies">
          {this.getDependencies()}
        </div>
      </div>
    )
  }
});

export default Topology;
