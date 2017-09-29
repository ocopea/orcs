// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevActions from '../../module-development/js/actions/dev-actions.js';
import Loader from '../../module-development/assets/loader.gif';
import d3 from 'd3';
import Config from '../../module-production/js/config.js';

var ProgressBarQuantity = React.createClass({

  componentDidMount: function(){
    DevActions.initateInstanceStatus();
  },

  componentDidUpdate: function(nextProps){
    if(this.props.done !== nextProps.done){
      if(this.props.allElements.length > 0){
          this.initBar();
      }
    }
  },

  initBar: function(){

    d3.select(".Progress-bar-quantity svg").remove();

    var height = this.props.height,
        width = this.props.width;

    var scale = d3.scale.linear()
                .domain([0, this.props.allElements.length])
                .range([0, width]);

    var svg = d3.select(".Progress-bar-quantity").append('svg')
                      .attr({
                        width: width,
                        height: height,
                        class: "Progress-bar-quantity__container"
                      });

    var range = svg.append('g').classed('range-container', true)
                  .append('rect')
                  .attr({
                    class: 'Progress-bar-quantity__range',
                    width: width,
                    height: height
                  });

    var done = svg.append('g').classed('done-container', true)
                .append('rect')
                .attr({
                  class: 'Progress-bar-quantity__done',
                  width: scale(this.props.done.length),
                  height: height
                });

  },

  render: function(){

    var instanceName = this.props.instanceName !== undefined ?
                       Config.getShortName(this.props.instanceName, 15) : null;
    return(
      <div className="Progress-bar-quantity">
        <div className="Progress-bar-quantity__status">
          {this.props.done.length}/{this.props.allElements.length}
        </div>
        <div className="Progress-bar-quantity__deploying-msg"
            title={Config.getTitleOrNull(this.props.instanceName, 15)}>
          Deploying {instanceName}
        </div>
      </div>
    );
  }
});

ProgressBarQuantity.propTypes = {
  done: React.PropTypes.array.isRequired,
  unDone: React.PropTypes.array.isRequired,
  allElements: React.PropTypes.array.isRequired,
  height: React.PropTypes.number.isRequired,
  width: React.PropTypes.number.isRequired,
  instanceName: React.PropTypes.string
}

export default ProgressBarQuantity;
