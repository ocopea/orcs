// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer } from 'mobx-react';

// components
import Line from './line.jsx';

// helpers
import getElementToBaseline from './lines/baselines/element-to-baseline.js';
import getSelectedBaseline from './lines/selectedLines/selected-base-line.js';
import componentTypes from './data/componentTypes.json';
import HandleState from './helpers/handleState.js';
import shake from './helpers/shake.js';

// vendors
import _ from 'lodash';
import $ from 'jquery';

import LoaderGif from './assets/loader.gif';


@observer
export default class Element extends React.Component {

  render(){

    const { id, name, componentType,
            width, height, position,
            baseClass, onClick, state,
            uiStore, imgSrc, iconSrc, isActive, instanceState } = this.props;

    const isLoading = HandleState.isLoading(state, instanceState)
    const style = {
      width: width,
      height: height,
      margin: `0 ${uiStore.marginBetweenElements}px`
    }

    if(this.hasLine()){
      this.removeLine()
    }

    let scrollLeft = 0;
    const isService = this.props.componentType === componentTypes.service;
    const isDependency = this.props.componentType === componentTypes.dependency;

    // handle scroll
    if(uiStore.scroll[this.props.componentType]){
      if(isService){
        if($('.Topology__app-services')[0]){
          scrollLeft = $('.Topology__app-services')[0].scrollLeft;
        }
      }
      if(isDependency){
        if($('.Topology__data-services')[0]){
          scrollLeft = $('.Topology__data-services')[0].scrollLeft;
        }
      }
    }

    let elementWidth = uiStore.elementSize[this.props.componentType].width;
    let pos =  $(`#${this.props.id}`).position();
    let left = pos ? pos.left + scrollLeft : null;

    return(

      <div className={!isActive ? 'disabled' : null}>

        {/* ELEMENT */}
        <div
          className = {this.getElementClassName()}
          id =        {id}
          style =     {style}
          ref =       'element'
          onClick =   {this.onClick.bind(this)}>

            {/* Icon */}
            {
              <span className={
                  uiStore.showIconCircle ?
                  "Topology__element__icon-container circle topology-icon-container" :
                  "Topology__element__icon-container"}>
                  {
                    iconSrc ?
                      <img src={iconSrc} className="Topology__element__icon" />
                    :
                    <span className={
                        componentType === componentTypes.dependency ?
                        'icon-db' : 'icon-service'
                      }></span>
                  }
              </span>
            }

            {/* Loader or State indicators */}
            <div title={name} className={'Topology__element__title'}>{name}</div>
            {
              uiStore.showState && isLoading ?
                <img src={LoaderGif} className="loader"/>
              :
              uiStore.showState ? HandleState.isError(state, instanceState) ?
                <div className="Topology__element__state-indicator fail">
                  <span className="icon-x"></span>
                </div>
              : <div className="Topology__element__state-indicator success">
                  <span className="icon-check"></span>
                </div>
              : null
            }

            {/* Logo */}
            {
              uiStore.showLogoCircle ?
                <div className='Topology__element__logo-circle'>
                  <img
                    src={imgSrc}
                    className="Topology__element__logo"/>
                </div>
              :
              <img
                src={imgSrc}
                style={{width: '30%', top: '-25px'}}
                className="Topology__element__logo" />
            }

            {/* Alerts */}
            {
              uiStore.showAlerts &&
              this.props.alerts &&
              this.props.alerts.length ?
               <div className="Topology__element__alert">
                 <span className="icon-alert" title={this.props.alerts[0]}></span>
               </div>
              : null
            }

        </div>

        {/* line from element to baseline */}
        {
          this.props.showLines && this.state.elementToBaseline ?
            <Line
              start =           {this.state.elementToBaseline.start}
              end =             {this.state.elementToBaseline.end}
              className =       {`element-to-baseline-${id}`}
              componentType =   {componentType}
              additionalClass = 'element-to-baseline'
              left =            {left}
              width =           {elementWidth}
              isSelected =      {this.isLineSelected()}></Line>
          : null
        }
      </div>
    )
  }

  constructor(props) {
    super(props);
    this.state = {
      elementToBaseline: null
    }
  }

  baseClassName = "Topology__element";

  elementTobaseline = {};

  onClick(e) {
    if(this.props.alerts.length) {
      const element = this.refs.element;
      shake(element);
    }
    this.props.onClick();
    if(this.props.uiStore.showPlanSelectionMenu){
      this.handlePlansMenu(e);
    }
  }

  handlePlansMenu(e) {
    const UiController = this.props.uiStore;
    UiController.renderPlansMenu(true);
    const element = e.target.classList.contains('Topology__element') ?
                    $(e.target) : $(e.target).closest('.Topology__element');
    const plansMenuWidth = 300;
    const cssMargin = UiController.cssMargin;
    const padding = 18;
    const position = element.position();
    position.left += this.props.width +
                      UiController.marginBetweenElements + cssMargin - padding;
    position.top = this.props.componentType === componentTypes.dependency ?
                    this.props.height :
                    this.props.height +
                      UiController.marginBetweenElements - plansMenuWidth/3 - padding;
    UiController.setTooltipPosition(position);
  }

  componentDidMount(){
    this.drawLines();
    this.addWindowResizeListener();
  }

  drawLines() {
    this.setState({
      elementToBaseline: getElementToBaseline(
        this.props.id,
        this.props.componentType,
        this.props.uiStore.baselineHeight
      )
    });
  }

  addWindowResizeListener() {
    $(window).resize(()=>{
      this.setState({
        elementToBaseline: null
      });
      this.drawLines();
    });
  }

  isSelected(id){
    const selectedElement = this.props.uiStore.selectedElement;
    if(selectedElement && !_.isEmpty(selectedElement)){
      let selectedElementId = selectedElement.element.id;
      return id === selectedElementId;
    }
  }

  isRelatedElement(id) {
    let selectedElement = this.props.uiStore.selectedElement;
    let relatedElements = selectedElement ?
                          selectedElement.relatedElements : null;
    if(relatedElements){
      return relatedElements[id] ? true : false;
    }else{
      return null;
    }
  }

  getElementClassName(){
    const componentType = this.props.componentType;
    const related =     'related';
    let className =     `${this.baseClassName} ${this.baseClassName}__${componentType}`;

    let selectedElement = this.props.uiStore.selectedElement;
    let isSelected = this.isSelected(this.props.id);
    let isRelated = false;

    if(selectedElement && !_.isEmpty(selectedElement)){
      isRelated = selectedElement.relatedElements[this.props.id] !== undefined;
    }

    if(this.props.uiStore.highlightRelatedElements){
      return isSelected ? `${className} ${this.baseClassName}__selected selected` :
             isRelated ? `${className} ${this.baseClassName}--${related} related` : className;
    }else{
      return isSelected ? `${className} ${this.baseClassName}__selected selected` : className;
    }

  }

  isLineSelected() {
    let isSelected = this.isSelected(this.props.id);
    let isRelatedElement = this.isRelatedElement(this.props.id);
    return isSelected || isRelatedElement;
  }

  hasLine() {
    return $(`.Topology__line--element-to-baseline-${this.props.id}`).children().length !== 0;
  }

  removeLine() {
    $(`.Topology__line--element-to-baseline-${this.props.id}`).empty();
  }

};

Element.propTypes = {
  componentType: React.PropTypes.string.isRequired,
  name: React.PropTypes.string.isRequired,
  imgSrc: React.PropTypes.string.isRequired,
  iconSrc: React.PropTypes.string,
  id: React.PropTypes.string.isRequired,
  state: React.PropTypes.string.isRequired,
  onClick: React.PropTypes.func.isRequired,
  width: React.PropTypes.number.isRequired,
  height: React.PropTypes.number.isRequired,
  version: React.PropTypes.string
}
