// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import ApplicationIcon from '../../../assets/images/wizard/app-type.svg';
import BackupIcon from '../../../assets/images/wizard/backup.svg';
import ConfigIcon from '../../../assets/images/wizard/details.svg';
import d3 from 'd3';
import _ from 'lodash';
import Config from '../../../../module-production/js/config.js';
import InlineSvg from 'react-inlinesvg';

var StatusBar = React.createClass({

  componentDidMount: function(){
    this.handleBar();
  },

	componentDidUpdate: function(){
		d3.select('.bar').select('svg').remove();
		this.handleBar();
	},

  handleBar: function(){
		var width = 442,
		 		height = 50;

		var svg = d3.select(".bar").append("svg")
								.attr({ width: width, height: height });

		var container = svg.append("g")
												.classed("bar-container", true);

    //dot shadow
    var shadow = container.append('defs').append('filter').attr("id", "f1")

    var filter = d3.select('.bar-container').selectAll('defs').select('filter');

    filter
      .attr({
        "x": "0",
        "y": "0",
        "width": "200%",
        "height": "200%"
      })

    filter
      .append('feOffset')
      .attr({
        "result": "offOut",
        "in": "SourceGraphic",
        "dx": "0",
        "dy": "1"
      });

    filter
      .append('feGaussianBlur')
        .attr({
          "result": "blurOut",
          "in": "offOut",
          "stdDeviation": "1"
        })

    filter
      .append('feBlend')
      .attr({
        "in": "SourceGraphic",
        "in2": "blurOut",
        "mode": "normal"
      })

		var baseLine = [
			{"x": 40, "y": 20},
			{"x": 400, "y": 20 }
		];

		this.drawLine(container, baseLine, "basic-path");

		var appTypeDotLocation = { cx: 40, cy: 20 },
				imageDotLocation = { cx: 220,	cy: 20 },
				configDotLocation = { cx: 400, cy: 20 },
				outterDotRadius = 12,
				innerDotRadius = 4;

		var dictionary = {
			'App-Market': {
				outter: { cx: appTypeDotLocation.cx,
									cy: appTypeDotLocation.cy,
									r: outterDotRadius,
									className: "dot" },
				inner:  { cx: appTypeDotLocation.cx,
									cy: appTypeDotLocation.cy,
									r: innerDotRadius,
									className: this.getInnerDotClassName(DevNavigationOptions.wizard.subLocation.appMarket) }
			},
			'image': {
				outter: { cx: imageDotLocation.cx,
									cy: imageDotLocation.cy,
									r: outterDotRadius,
									className: "dot"  },
				inner:  { cx: imageDotLocation.cx,
									cy: imageDotLocation.cy,
									r: innerDotRadius,
									className: this.getInnerDotClassName(DevNavigationOptions.wizard.subLocation.image) }
			},
			'config': {
				outter: { cx: configDotLocation.cx,
									cy: configDotLocation.cy,
									r: outterDotRadius,
									className: "dot" },
				inner:  { cx: configDotLocation.cx,
									cy: configDotLocation.cy,
									r: innerDotRadius,
									className: this.getInnerDotClassName(DevNavigationOptions.wizard.subLocation.config) }
			}
		}

		this.drawDot(container, dictionary['App-Market'].outter);
		this.drawDot(container, dictionary['image'].outter);
		this.drawDot(container, dictionary['config'].outter);

		this.drawLine(container, baseLine, "basic-path-inside");

		this.drawDot(container, dictionary['App-Market'].inner);
		this.drawDot(container, dictionary['image'].inner);
		this.drawDot(container, dictionary['config'].inner);

		this.lineFromCompletedToCurrent(container, dictionary);

	},

	lineFromCompletedToCurrent: function(container, dictionary){
		var currentStepParams = dictionary[this.props.currentStep.substring(1)];
    if(!currentStepParams) return;
		var currentStepLocation = {x: currentStepParams.outter.cx, y: currentStepParams.outter.cy};

		d3.selectAll(".completed-line").remove();

		var completedImgs = d3.selectAll(".isvg")[0];
		var that = this;
    let completedStepParams, completedStepLocation;
		completedImgs.forEach(img=>{
				if(img.classList.contains("completed")){
					completedStepParams = dictionary[img.classList[2].substring(1)];
					completedStepLocation = {x: completedStepParams.outter.cx, y: completedStepParams.outter.cy}
					that.drawLine(container, [completedStepLocation, currentStepLocation], "completed-line")
				}
		})
	},

	getInnerDotClassName: function(stepName){

		var stepIndex = this.props.steps.indexOf(stepName),
				currentStepIndex = this.props.steps.indexOf(this.props.currentStep);
      let className;
			if(stepName == this.props.currentStep){
				className = "inner-dot selected";
			}else if(stepIndex < currentStepIndex){
				className = "inner-dot complete";
			}else{
				className = "inner-dot";
			}
			return className
	},

	getImageClassName: function(stepName){
		var stepIndex = this.props.steps.indexOf(stepName),
				currentStepIndex = this.props.steps.indexOf(this.props.currentStep);

		return stepIndex < currentStepIndex ? stepName + " completed" : null;
	},

	drawDot: function(container, params){
		var dot = container.append("g")
											.classed(params.className, true)
											.append("circle")
											.attr({
												"r": params.r,
												"cx": params.cx,
												"cy": params.cy
											});
    if(params.className.indexOf('selected') > -1){
      dot.attr('filter', 'url(#f1)')
    }
	},

	drawLine: function(container, line, className){
			var lineFunction = d3.svg.line()
							.x(function(d){ return d.x })
							.y(function(d){ return d.y })
							.interpolate("step-before");

			container
				.append("path")
						.attr({
							"d": lineFunction(line),
							"class": className
						})
	},

  render: function(){

    return(
      <div className="navigation">

        <div className="nav-icon">
          <InlineSvg src={ApplicationIcon} className={this.getImageClassName(DevNavigationOptions.wizard.subLocation.appMarket)}/>
          <span>application</span>
        </div>

        <div className="nav-icon">
          <InlineSvg src={BackupIcon} className={this.getImageClassName(DevNavigationOptions.wizard.subLocation.image)}/>
          <span>image</span>
        </div>

        <div className="nav-icon">
          <InlineSvg src={ConfigIcon} className={this.getImageClassName(DevNavigationOptions.wizard.subLocation.config)}/>
          <span>config</span>
        </div>

        <div className="bar"></div>
      </div>
    )
  }

});

export default StatusBar;
