// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import d3 from 'd3';
import CountTo from 'react-count-to';
import styles from './styles-progress-bar.scss';
import uuid from 'uuid';


export default class ProgressBar extends React.Component{

  static propTypes = {
    instanceName: React.PropTypes.string,
    precent: React.PropTypes.number.isRequired,
    width: React.PropTypes.number,
    height: React.PropTypes.number,
    progressBarFill: React.PropTypes.string,
  }

  render(){

    const precent = this.props.precent || 0;
    const className = "progress-bar " + this.state.instanceName;
    let start;
    if(!this.singleton){
      this.singleton = true;
      start = 0;
    }else{
      start = precent;
    }

    return(
      <div className={styles.ProgressBar}>
        <div className={className}></div>
        {
          !this.props.hidePrecent ?
            <div className="precent">
              <CountTo from={start} to={precent} speed={800}/>%
            </div>
          : null
        }
      </div>
    )
  }

  constructor(props){
    super(props)
    this.state = {
      instanceName: this.props.instanceName ?
                      this.getCleanName(this.props.instanceName) : `a-${uuid()}`
    };
  }

  componentDidMount(){
    this.init();
  }

  getCleanName(name) {
    const firstLetter = name[0];
    if(!isNaN(parseInt(firstLetter))){name = name.substring(1)}
    return name ? `${name.replace(/ /g,'')}` : '';
  }

  init(){

    var instanceClassName = this.getCleanName(this.state.instanceName);

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
                              height: height,
                              fill: this.props.progressBarFill
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
                              height: height,
                              fill: this.props.progressBarFill
                          })
    }

  }

};
