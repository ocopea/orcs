// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import d3 from 'd3';
import CountTo from 'react-count-to';


var ProgressBar = React.createClass({

    componentDidMount: function(){
        this.init();
    },

    componentDidUpdate: function(nextProps){
        this.init();
    },

    init: function(){

      var instanceClassName = this.props.instanceName;      

      if(d3.select(".progress-bar." + instanceClassName)[0][0].childNodes.length > 0){
  			d3.select(".progress-bar." + instanceClassName + " svg").remove();
  		}

      var height = this.props.height == undefined ? 7 : this.props.height,
          width = this.props.width == undefined ? 100 : this.props.width;

  		var scale = d3.scale.linear()
  			.domain([0, 100])
  			.range([0, width]);

  		var svg = d3.select(".progress-bar." + instanceClassName)
  					.append("svg")
  					.attr({width: width, height: height});

  		var container = svg.append('g')
  							.classed('progress-bar-container', true);

  		var outerRange = container.append('rect')
  						.classed('outer-range', true)
  						.attr({
  							width: width,
  							height: height
  						});

      if(this.props.disableAnimation == undefined){
        var progress = container.selectAll('g')
                        .data([this.props.precent])
                        .enter()
                            .append('rect')
                            .classed('progress', true)
                            .attr({
                                width: 0,
                                height: height
                            })

        progress.transition()
            .duration(800)
            .attr('width', function(d){ return scale(d) })
      }else{
        var progress = container.selectAll('g')
                        .data([this.props.precent])
                        .enter()
                            .append('rect')
                            .classed('progress', true)
                            .attr({
                                width: function(d){ return scale(d) },
                                height: height
                            })
      }

    },

    render: function(){

        var className = "progress-bar " + this.props.instanceName.replace(/ /g,'');

        return(
            <div>
              <div className={className}></div>
              <div className="precent">
                <CountTo from={0} to={this.props.precent} speed={800}/>%
              </div>
            </div>
        )
    }
});

export default ProgressBar;
