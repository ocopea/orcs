// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import AppTopology from '../../../module-production/js/components/wizard/appTopology/appTopology.comp.js';
import AppTopologyActions from '../../../module-production/js/actions/appTopologyActions.js';
import TopologyParser from '../../../shared-components/js/topology-data-parser.js';
import DevWizardActions from '../actions/dev-wizard-actions.js';
import Tooltip from './wizard/config/wizard.config.topology.tooltip.comp.js';
import Config from '../../../module-production/js/config.js';
import d3 from 'd3';
import $ from 'jquery';
import BrowserDetector from '../../../module-production/js/browserDetection.js';
import stateOptions from '../data/deploying-state-options.js';
import Loader from '../../assets/loader.gif';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import SimpleTooltip from '../../../shared-components/js/simple-tooltip.comp.js';


var DevTopology = React.createClass({

  getInitialState: function(){
    return stateOptions;
  },

  isFirefox: function(){
    return BrowserDetector.isBrowser(BrowserDetector.browserNames().firefox);
  },

  isInternetExplorer: function(){
    return BrowserDetector.isBrowser(BrowserDetector.browserNames().ie);
  },

  isEdge: function(){
    return BrowserDetector.isBrowser(BrowserDetector.browserNames().edge);
  },

  componentDidMount: function(){
    this.intiateElementsState();
    DevWizardActions.setTopologyActiveElements(this.props.selectedApp);
    var that = this;
    // hide tooltip on document click
    $(document).click(function(e){
      var parentsHasClass = that.checkifParentsHasClass($(e.target).parents(), 'topology-tt');
      if(!parentsHasClass && !e.target.classList.contains('topology-tt')){
        DevWizardActions.hideConfigTopologyTooltip();
      }
    });
  },

  intiateElementsState: function(){
    var services = this.props.selectedApp.appServiceTemplates;
    services.forEach(service=>{
      service.state = null;
      service.dependencies.forEach(dependency=>{
        dependency.state = null;
      })
    });
  },

  checkifParentsHasClass: function(parents, className){
    var hasClass = false;
    _.filter(parents, p=>{
      if(p.classList.contains(className)){
        hasClass = true;
        return true;
      }
    });
    return hasClass;
  },

  componentWillUnmount: function(){
    AppTopologyActions.initialAppTopology();
  },

  componentDidUpdate: function(nextProps){
    this.appendImageContainer(this.servicesImageContainerParams());
    this.appendImageContainer(this.dependenciesImageContainerParams());
    this.appendTitle();
    this.setImageNewLocation(this.servicesImageParams());
    this.setImageNewLocation(this.dependenciesImageParams());
    this.setBoxNewLocation(this.servicesParams());

    // if(Config.getCurrentHash().module === DevNavigationOptions.module &&
    //    Config.getCurrentHash().location === DevNavigationOptions.deployingProgress.location.substring(1)){
         if(this.props.selectedInstanceState){
           this.setStateIndicators();
         }
    // }

    if(this.props.isSelectedElementActive !== nextProps.isSelectedElementActive){
        DevWizardActions.setTopologyActiveElements(this.props.selectedApp);
    }

    this.dimInActiveElements();
  },

  setStateIndicators: function(){

    if(this.props.selectedInstanceState !== stateOptions.error.toUpperCase() &&
       this.props.selectedInstanceState !== stateOptions.running.toUpperCase()){
         this.appendLoaderToComponents(d3.selectAll('g.dependency'));
         this.appendLoaderToComponents(d3.selectAll('g.service'));
       }
     this.appendSuccessAndFailIcons(d3.selectAll('g.dependency'));
     this.appendSuccessAndFailIcons(d3.selectAll('g.service'));
  },

  appendTitle: function(params){
    var that = this;

    if(this.props.selectedApp.appServiceTemplates !== undefined){
      this.props.selectedApp.appServiceTemplates.map(service=>{
          d3.selectAll('g.service')[0].forEach(serviceElement=>{
            if(parseInt(serviceElement.id) === service.serviceId){

              var hasTitle;
              _.forEach(serviceElement.children, o=>{
                  if(o.classList.contains('service-title')){
                    hasTitle = true;
                    return;
                  }
              });

              if(!hasTitle){
                var serviceName = service.appServiceName === undefined ?
                  service.name : service.appServiceName;

                var title = serviceName.length > 10 ? serviceName : null;
                d3.select(serviceElement)
                  .append('g')
                    .classed('service-title', true)
                  .append('foreignObject')
                    .attr({
                      'x': parseInt(serviceElement.children[0].attributes[2].value)
                    })
                    .on("click", AppTopologyActions.userClickOnAppTopologyService.bind(this))
                    .html(`<span>${Config.getShortName(serviceName, 10)}</span>`)
                    .append('title')
                      .html(serviceName.length > 10 ? serviceName : null)
              }
            }
          });
          that.appendDependencyTitle(service.dependencies)
      });
    }
  },

  appendDependencyTitle: function(dependencies){
    dependencies.forEach(dependency=>{
      d3.selectAll('g.dependency')[0].forEach(dependencyElement=>{

          var hasTitle;
          _.forEach(dependencyElement.children, o=>{
              if(o.classList.contains('dependency-title')){
                hasTitle = true;
                return;
              }
          });

          if(!hasTitle){
            if(parseInt(dependencyElement.id) === dependency.index){
              d3.select(dependencyElement)
                .append('g')
                  .classed('dependency-title', true)
                .append('foreignObject')
                  .attr({
                    'x': parseInt(dependencyElement.children[0].attributes[2].value)
                  })
                  .on("click", AppTopologyActions.userClickOnAppTopologyDependency.bind(this))
                  .html(`<span>${Config.getShortName(dependency.name, 10)}</span>`)
                  .append('title')
                    .html(dependency.name.length > 10 ? dependency.name : null)
            }
          }
      });

    })
  },

  dimInActiveElements: function(){

    var activeServicesIDs = [],
        activeDependenciesIDs = [];

    this.props.activeElements.services.forEach(service=>{
      activeServicesIDs.push(service.serviceId)
    });

    this.props.activeElements.dependencies.forEach(dependency=>{
      activeDependenciesIDs.push(dependency.index);
    });

    this.dimElement("g.service", activeServicesIDs);
    this.dimElement("g.dependency", activeDependenciesIDs);
  },

  dimElement: function(selector, activeElements){
    $(selector).each((index, element)=>{
      var id = parseInt($(element)[0].id);
      if(activeElements.indexOf(id) == -1){
        $(element).addClass('dimmed');
      }else{
        $(element).removeClass('dimmed');
      }

    });

  },

  servicesParams: function(){
    return {
      selector: 'g.service',
      x: -15,
      y: 50
    }
  },

  servicesImageParams: function(){
    return {
      selector: 'image.service',
      x: 20,
      y: 75
    }
  },

  servicesImageContainerParams: function(){
    return {
      selector: 'g.service',
      className: 'icon-container',
      y: this.isEdge() || this.isInternetExplorer() ? 140 : 110,
      x: this.isEdge() || this.isInternetExplorer() ? 68 : 52,
      r: 42
    }
  },

  dependenciesImageParams: function(){
    return {
      selector: 'image.dependency',
      x: 33,
      y: 340
    }
  },

  dependenciesImageContainerParams: function(){
    return {
      selector: 'g.dependency',
      className: 'icon-container',
      y: 360,
      x: this.isFirefox() || this.isEdge() || this.isInternetExplorer() ? 50 : 55,
      r: 30
    }
  },

  setBoxNewLocation: function(params){
    d3.selectAll(params.selector).select('rect').attr({
      "x": function(d){return d.x + params.x},
      "y": params.y
    })
  },

  setImageNewLocation: function(params){
    d3.selectAll(params.selector).attr({
      "x": function(d){ return d.x + params.x },
      "y": params.y
    })
  },

  appendImageContainer: function(params){

    if(!this.childrenHasClass($(params.selector), 'icon-container')){
      d3.selectAll(params.selector).insert('circle', "image")
      .attr({
        "class": params.className,
        "cx": function(d){return d.x + params.x},
        "cy": params.y,
        "r": params.r
      })
      .on("click", AppTopologyActions.userClickOnAppTopologyDependency.bind(this));
    }
  },

  childrenHasClass: function(parents, className){
    var hasClass = false;
    _.forEach(parents, parent=>{
      _.forEach(parent.children, child=>{
        if(child.classList.contains(className)){
          hasClass = true;
        }
      });
    });

    return hasClass
  },

  isLoading: function(state){    
    if(this.props.selectedInstanceState === stateOptions.error.toUpperCase() &&
       state === stateOptions.pending.toUpperCase()){
         return false;
       }
    return state !== stateOptions.bound.toUpperCase() &&
           state !== stateOptions.deployed.toUpperCase() &&
           state !== stateOptions.running.toUpperCase();
  },

  appendLoaderToComponents: function(components){

    var that = this;
    var selectedInstanceState = this.props.selectedInstanceState ?
                                this.props.selectedInstanceState.toUpperCase() : ''

    var filteredComponents = components.filter(function(d, i){
      var state = d.state ? d.state.toUpperCase() : '';
      return that.isLoading(state);
    });

    if(selectedInstanceState === stateOptions.running.toUpperCase() ||
       selectedInstanceState === stateOptions.error.toUpperCase()){
         //$('.loader').remove();
        //  return;
    }

    if(filteredComponents[0].length > 0){

      filteredComponents.forEach(component=>{
        var hasLoader = false;
        var children = component[0].children;

        component.forEach((c, i)=>{
          var child = c.children;
          $(child).each((i, element)=>{
            hasLoader = element.classList.contains('loader');
          });
          if(!hasLoader){
            d3.select(c).append('svg:image').attr({
              "xlink:href": function(d){
                return Loader;
              },
              "x": function(d){ return d.componentType === 'service' ? d.x+12 : d.x+25 },
              "y": function(d){ return d.componentType === 'service' ? 70 : 330},
              "class": function(d){ return 'loader' },
              "width": 80,
              "height": 80
            });
          }
        });
      });
    }

  },

  appendSuccessAndFailIcons: function(elementsArray){
    var deployed = this.getFilteredArray(elementsArray, stateOptions.deployed)[0];
    var bounds = this.getFilteredArray(elementsArray, stateOptions.bound)[0];
    var errorbindings = this.getFilteredArray(elementsArray, stateOptions.errorbinding)[0];
    var created = this.getFilteredArray(elementsArray, stateOptions.created)[0];
    var deploying = this.getFilteredArray(elementsArray, stateOptions.deploying)[0];
    var pending = this.getFilteredArray(elementsArray, stateOptions.pending)[0];
    var errorCreateing = this.getFilteredArray(elementsArray, stateOptions.errorCreateing)[0];
    var error = this.getFilteredArray(elementsArray, stateOptions.error)[0];

    if(this.props.selectedInstanceState.toUpperCase() === stateOptions.error.toUpperCase()){
      this.appendStateIcon(pending,
                             'fail-icon',
                             'icon-close'
                           );

    }

    this.appendStateIcon(deployed,
                           'success-icon',
                           'icon-check'
                         );

    this.appendStateIcon(bounds,
                           'success-icon',
                           'icon-check'
                         );

    this.appendStateIcon(errorbindings,
                           'fail-icon',
                           'icon-close'
                         );

    this.appendStateIcon(errorCreateing,
                           'fail-icon',
                           'icon-close'
                         );

   this.appendStateIcon(error,
                          'fail-icon',
                          'icon-close'
                        );

  },

  getFilteredArray: function(array, filter){
    return array.filter(o=>{
      var state = o.state || '';
      return state.toUpperCase() === filter.toUpperCase()
    });
  },

  appendStateIcon: function(elements, container_className, icon_className){
    var that = this;
    var hasIcon = false;
    d3.selectAll(elements).filter(function(d, i){
      d3.selectAll(elements[i].children).filter(function(d, i){
        if(this.classList.contains(container_className)){
          hasIcon = true;
          return;
        }else{
          that.appendStateIndicator(elements[i], container_className ,icon_className);
        }
      });

      // if(!hasIcon){
        // that.appendStateIndicator(elements[i], container_className ,icon_className);
      // }
    });
  },

  appendStateIndicator: function(element, container_className, icon_className){

    var container = d3.select(element)
          .append('g').classed(container_className + ' state-indicator', true);

    var that = this;

    container
      .append('circle')
      .attr({
        'r': 10,
        'cx': function(d){return d.x+getContainerX(d.componentType)},
        'cy': function(d){return getContainerY(d.componentType)},
        'fill': function(d){
          var state = d.state.toUpperCase();
          var selectedInstanceState = that.props.selectedInstanceState.toUpperCase();
          return state === stateOptions.bound.toUpperCase() ||
                 state === stateOptions.deployed.toUpperCase() ||
                 selectedInstanceState === stateOptions.running.toUpperCase() &&
                 state === stateOptions.deploying.toUpperCase() ? '#43ab60'
                 :
                 state === stateOptions.error.toUpperCase() ||
                 state === stateOptions.errorbinding.toUpperCase() ||
                 selectedInstanceState === stateOptions.error.toUpperCase() &&
                 state === stateOptions.pending.toUpperCase() ||
                 state === stateOptions.errorCreateing.toUpperCase() ||
                 selectedInstanceState === stateOptions.error.toUpperCase() &&
                 state === stateOptions.created.toUpperCase() ? '#cc5669' :
                 null
        }
      });

      container.append('foreignObject')
        .attr({
          'x': function(d){return d.x+getIconX(d.componentType) + 1},
          'y': function(d){return getContainerY(d.componentType) - 3},
          'class': icon_className
        });

      if(element){
        $(element.children).each((i,o)=>{
          if(o.classList.contains('loader')){
            $(element.children[i]).remove();
          }
          if(o.classList.contains('state-indicator')){
            $(element.children[i]).remove();
          }
        })
      }

      function getContainerY(componentType){
        return componentType === 'dependency' ? 330 : 65;
      }
      function getContainerX(componentType){
        return componentType === 'dependency' ? 95 : 105;
      }
      function getIconX(componentType){
        return componentType === 'dependency' ? 89 : 99;
      }
  },

  render: function(){
    const isNoVersionsTooltipRender = this.props.simpleTooltip ? this.props.simpleTooltip.isRender : false;
    return(
      <div className="Topology">
        <div className="Topology__container">
          <div className="Topology__container__title">
            <span className="Topology__container__title__span">edit topology</span>
            <div className="Topology__container__title__legend">
              <div className="Topology__container__title__legend__section">
                <span className="app-i"></span>
                <span className="Topology__container__title__legend__label">application</span>
              </div>
              <div className="Topology__container__title__legend__section">
                <span className="service-i"></span>
                <span className="Topology__container__title__legend__label">service</span>
              </div>
              {/* /Topology__container__title__legend */}
            </div>
            {/* /Topology__container__title */}
          </div>

          <div className="Topology__container__inside">
            {
              this.props.hasDynamicLoadbalancer ?
                <div className="dynamic-load-balancer">
                    <span className="text">Dynamic Load Balancer</span>
                    <span className="icon-settings link"></span>
                </div>
              :
              null
            }

            {
              isNoVersionsTooltipRender ?
                <SimpleTooltip data={this.props.simpleTooltip.data}/>
              : null
            }

            <AppTopology
              module={this.props.module}
              state={this.props.state}
              selectedApp={this.props.selectedApp}
              selectedService={this.props.selectedService}
              selectedDependency={this.props.selectedDependency}
              servicesTranslate={this.props.servicesTranslate}
              dependenciesTranslate={this.props.dependenciesTranslate}
              allServices={this.props.allServices}
              currentStepName={'show'}
              containerWidth={this.props.containerWidth !== undefined ? this.props.containerWidth : null}
              containerHeight={this.props.containerHeight !== undefined ? this.props.containerHeight : null}
              configuration={this.props.configuration}
              simpleTooltip={this.props.simpleTooltip}
              />
          {/* /Topology__container__inside */}
          </div>
        {/* /Topology__container */}
        </div>

        {
          this.props.tooltip.isRender ?
            <Tooltip
              position={this.props.tooltip.position}
              selectedElement={this.props.selectedElement}
              selectedElementPlan={this.props.selectedElement.plan}
              configuration={this.props.configuration}
              /> : null
        }

      {/* /Topology */}
      </div>
    )
  }
});

DevTopology.propTypes = {
  module: React.PropTypes.string.isRequired,
  state: React.PropTypes.object.isRequired,
  selectedApp: React.PropTypes.object.isRequired,
  selectedService: React.PropTypes.object.isRequired,
  selectedDependency: React.PropTypes.object.isRequired,
  servicesTranslate: React.PropTypes.number.isRequired,
  dependenciesTranslate: React.PropTypes.number.isRequired,
  allServices: React.PropTypes.array.isRequired,
  currentStepName: React.PropTypes.string.isRequired,
  tooltip: React.PropTypes.object.isRequired,
  selectedElement: React.PropTypes.object.isRequired,
  activeElements: React.PropTypes.object.isRequired,
  selectedInstanceState: React.PropTypes.string
}

export default DevTopology;
