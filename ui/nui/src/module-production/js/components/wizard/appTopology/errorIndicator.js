// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';
import SharedActions from '../../../../../shared-actions.js';


const append = (elements, array, query, className, conditionParam, shapeString, errorMsg, componentType) => {

  const _elements = elements[0];

  _elements.forEach(service=>{
    array.forEach(item=>{
      elements.filter((d, i)=>{
        if(item[query] === d[query] || item[query] === d.name){
          let domElement = _elements[i];
          if(_.isEmpty(item[conditionParam])){
            let hasIcon = childrenHasClass(domElement.children, className);
            if(!hasIcon && _.isEmpty(item[conditionParam])){
              shapeString === 'circle' ?
                appendCircle(domElement, componentType) :
              shapeString === 'triangle' ?
                appendTriangle(item, domElement, errorMsg, componentType) : null
            }
          }
        }
      });
    });
  });
}

const childrenHasClass = (object, className) => {
  let hasIcon = false;
  _.forEach(object, child=>{
    if(child.classList.contains(className)){
      hasIcon = true;
      return true;
    }
  });
  return hasIcon;
}

const appendCircle = (domElement, componentType) => {

  let circle =  d3.select(domElement)
    .append('circle')
    .attr({
      'stroke': '#cc5669',
      'stroke-width': 2,
      'class': 'no-versions',
      'cx': (d)=>{ return componentType === 'service' ? d.x + 52 : componentType === 'dependency' ? d.x + 55 : null},
      'cy': componentType === 'service' ? 110 : componentType === 'dependency' ? 360 : null,
      'r': componentType === 'service' ? 43 : componentType === 'dependency' ? 32 : null
    });
}

const appendTriangle = (objectElement, domElement, errorMsg, componentType) => {
  d3.select(domElement)
  .insert('path')
   .attr({
     'd': d3.svg.symbol().type('triangle-up').size(160),
     'transform': (d)=>{
       return componentType === 'service' ? `translate( ${d.x + 80}, 75)` :
       `translate( ${d.x + 80}, 335)`},
     'class': 'no-versions-triangle',
     'fill': '#cc5669',
     'stroke': '#cc5669',
     'stroke-width': 2
   }).on("mouseover", function(d){return showNoVersionsTooltip.bind(this, domElement, d, errorMsg, componentType)() })
     .on("mouseout", hideNoVersionsTooltip);

   d3.select(domElement)
    .insert('foreignObject')
    .attr({
      'class': 'icon-missing-field',
      'transform': (d)=>{
        return componentType === 'service' ? `translate( ${d.x + 79}, 73)` :
        `translate( ${d.x + 79}, 333)`},
      'font-size': '8px'
    }).on("mouseover", function(d){return showNoVersionsTooltip.bind(this, domElement, d, errorMsg, componentType)() });

    function showNoVersionsTooltip(domElement, d, errorMsg, componentType) {
      const top = componentType === 'service' ? 100 :
                  componentType === 'dependency' ? 355 : null; //$(domElement).position().top;
      const left = componentType === 'service' ? $(domElement).position().left - 120 :
                   componentType === 'dependency' ? $(domElement).position().left - 135 : null;
      const position = {left: left , top: top };
      const title = d.appServiceName || d.name;
      SharedActions.showSimpleTooltip(position, title, errorMsg)
    }

    function hideNoVersionsTooltip() {
      SharedActions.hideSimpleTooltip()
    }
}

export default append;
