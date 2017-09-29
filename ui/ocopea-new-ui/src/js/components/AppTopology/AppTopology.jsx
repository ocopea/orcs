import React from 'react';
import { observer } from 'mobx-react';

// controllers
import DataController from './controllers/topology-data-controller.js';
import UiController from './controllers/topology-ui-controller.js';

// components
import AppServices from './appServices.jsx';
import DataServices from './dataServices.jsx';
import Line from './line.jsx';
import PlanSelection from './plans-selection/_plans-selection-home.comp.js';

//helpes
import Baseline from './lines/baselines/base-line.js';
import getSelectedBaseline from './lines/selectedLines/selected-base-line.js';
import HandleState from './helpers/handleState.js';
import componentTypes from './data/componentTypes.json';

// vendors
import $ from 'jquery';
import dragscroll from 'dragscroll';

// style
import topologyStyles from './css/_topology.sass';


@observer
export default class Topology extends React.Component {

  render(){

    let selectedElement = UiController.selectedElement;
    let relatedElements = selectedElement ?
                          selectedElement.relatedElements : null;
    let selectedBaseLine, isMenuDataValid;
    $(".Topology__line--base-line--selected").empty();

    if(selectedElement && !_.isEmpty(selectedElement)){
      selectedBaseLine = getSelectedBaseline(
        selectedElement, UiController.margin
      );
      if(this.props.configuration){
        isMenuDataValid = this.isElementMenuValid(
          selectedElement.element, this.props.configuration
        )
      }else{
        isMenuDataValid = false;
      }
    }
    const instanceState = this.props.instanceState;

    return(
      <div className={this.props.baseClass ?
                      `Topology ${this.props.baseClass}__Topology`
                      : 'Topology'}>

        <div
          className="Topology__container"
          style={
              {width: UiController.containerWidth,
              height: UiController.containerHeight}}>

            {/* Plans Selection Menu */}
            {
              isMenuDataValid && UiController.showPlanSelectionMenu &&
              UiController.tooltip.isRender &&
              selectedElement && UiController.selectedElement.element ?
                <PlanSelection
                  selectedElement={UiController.selectedElement.element}
                  config={this.props.configuration}
                  position={UiController.tooltip.position}>
                </PlanSelection>
              : null
            }

            <AppServices
              uiStore =   {UiController}
              dataStore = {DataController}
              configuration={this.props.configuration}
              instanceState={instanceState} />

            <div className="Topology__container__baselines">
              {
                UiController.showLines && this.state.baseLine ?
                    <Line
                      start =       {this.state.baseLine.start}
                      end =         {this.state.baseLine.end}
                      className =   "base-line" />
                : null
              }
              {
                UiController.showLines && selectedBaseLine ?
                  <Line
                    start =     {selectedBaseLine.start}
                    end =       {selectedBaseLine.end}
                    className = {'base-line--selected'} />
                : null
              }
            </div>
            <DataServices
              uiStore =       {UiController}
              dataStore =     {DataController}
              configuration={this.props.configuration}
              instanceState={instanceState} />
        </div>
      </div>
    )
  }

  constructor(props) {
    super(props);
    this.state = {
      baseLine: null,
      cssMargin: 0
    }
    UiController.getScrolledContainers();
  }

  isElementMenuValid(selectedElement, configuration) {
    switch (selectedElement.componentType) {
      case componentTypes.service:
        if(configuration.appServiceConfigurations) {
          return configuration.appServiceConfigurations.filter(service=>{
            return service.appServiceName === selectedElement.name && !_.isEmpty(service.supportedVersions);
          })[0];
        }
        break;
      case componentTypes.dependency:
        if(configuration.dataServiceConfigurations) {
          return configuration.dataServiceConfigurations.filter(dependency=>{
            return dependency.dataServiceName === selectedElement.name && _.size(dependency.dsbPlans) > 0;
          })[0];
          break;
        }
      default:
    }
  }

  addScrollListener() {
    $('.Topology__data-services')[0].addEventListener(
      'scroll', this.drawBaseline.bind(this), false
    );
  }

  componentDidMount(){
    // redraw baseline on window resize
    this.setState({
      baseLine: Baseline.getBaseline()
    });
    this.addWindowResizeListener();
    this.addScrollListener();
    this.addClickEventListener();
  }

  componentDidUpdate() {
    const baselineHeight =  UiController.baselineHeight;
    // set baseline height in uiStore
    const baselineClassName = '.Topology__container__baselines';
    if(DataController.services.length === 1 &&
       DataController.dependencies.length === 1){
          Baseline.setBaselineHeight(baselineClassName, 0);
       }else{
          Baseline.setBaselineHeight(baselineClassName, baselineHeight);
       }
  }

  addWindowResizeListener() {
    window.addEventListener('resize', this.drawBaseline.bind(this), false);
  }

  addScrollListener() {
    $('.Topology__data-services')[0].addEventListener(
      'scroll', this.drawBaseline.bind(this), false
    );
    $('.Topology__app-services')[0].addEventListener(
      'scroll', this.drawBaseline.bind(this), false
    );
  }

  addClickEventListener() {
    document.addEventListener(
      'click', this.removePlansMenu.bind(this), false
    )
  }

  removePlansMenu(e) {
    const classNames =  e.target.className;
    let isElement;

    _.each($(e.target).parents(), parent=>{
      isElement = e.target.classList.contains('Topology__element') ||
                  parent.classList.contains('Topology__element') ||
                  parent.classList.contains('topology-tt');
      if(isElement) return false;
    });

    if(!isElement){
      UiController.renderPlansMenu(false);
    }
  }

  componentWillUnmount() {
    // DOES NOT WORK !!!
    window.removeEventListener('resize', this.drawBaseline, false);
    $('.Topology__data-services')[0].removeEventListener(
      'scroll', this.drawBaseline, false
    );
    document.removeEventListener('click', this.removePlansMenu, false);
    UiController.initiateSelectedElement();
  }

  drawBaseline() {
    UiController.renderPlansMenu(false);
    this.setState({
      baseLine: null
    });

    this.setState({
      baseLine: Baseline.getBaseline()
    });

  }

};

Topology.propTypes = {
  baseClass: React.PropTypes.string,
  configuration: React.PropTypes.object,
  instanceState: React.PropTypes.string
}
