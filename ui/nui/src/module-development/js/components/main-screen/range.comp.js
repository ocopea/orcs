// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import D3 from 'd3';
import _ from 'lodash';
import $ from 'jquery';


var Range = React.createClass({

	componentDidMount: function(){
		this.init();
	},

	componentDidUpdate: function(nextProps){
		this.init();
	},

	init: function(){

			var instanceClassName = this.props.instanceName;

			if(d3.select(".range." + instanceClassName)[0][0].childNodes.length > 0){
				d3.selectAll(".range svg").remove();
			}

			var height = 25,
				width = 100;

			var scale = d3.scale.linear()
				.domain([0, width])
				.range([0, 100]);

			var data = this.parseToArray(this.props.dsbQuota);

			var max = d3.max(data, function(d){ return d.precent })
			var min = d3.min(data, function(d){ return d.precent })

			var svg = d3.select(".range." + instanceClassName)
						.append("svg")
						.attr({width: width, height: height});

			var container = svg.append('g')
								.classed('range-container', true);

			var outerRange = container.append('rect')
							.classed('outer-range', true)
							.attr({
								width: width,
								height: 7
							});

			var range = container.selectAll("g")
							.data([max])
							.enter()
								.append('rect')
								.attr({
									width: 0,
									height: 7,
									class: 'psb-range'
								});

			range.transition()
				.duration(800)
				.attr('width', function(d){ return d })

			var ticks = container.selectAll("g")
							.data(data)
							.enter()
								.append('rect')
								.attr({
									width: 1.5,
									height: 3.5,
									x: 0,
									y: 3.5,
									class: 'tick'
								});

			ticks.transition()
				.duration(800)
				.attr('x', function(d,i){return scale(d.precent) })

			var formatPercent = d3.format("%");

			var precentMin = container.append('g')
							.data(data)
							.append('foreignObject')
							.classed('precent-range min', true)
							.text(function(d){
								return min
							})
							.attr({
								'x': function(){ return min !== max ? 60 : 40 },
								'y': 11,
								'height': 10
							});

			if(max !== min){
				var precentMax = container.append('g')
							.data(data)
							.append('foreignObject')
							.classed('precent-range max', true)
							.text(function(d){
								return max
							})
							.attr({
								'x': function(){ return 21 },
								'y': 11,
								'height': 10
							});

				var seperator = container.append('g')
							.append("foreignObject")
							.classed("precent-range seperator", true)
							.text("-")
							.attr({
								'x': 48,
								'y': 11,
								'height': 10
							})
			}

			animateNumbers(".precent-range.max", min);
			animateNumbers(".precent-range.min", max);

			function animateNumbers(className, value){
				container.selectAll(className).transition().ease("quad-out").duration(800)
					.tween("text", function(d){
						var i = d3.interpolate(0, value/100);
						return function(t){
							d3.select(this).text(formatPercent(i(t)));
						};
				});
			}
	},

	parseToArray: function(obj){
		var array = [];
		_.pickBy(obj, function(value, key){
			array.push({
				name: key,
				precent: value
			})
		});
		return array;
	},

	render: function(){

		var className = "range " + this.props.instanceName.replace(/ /g,'');

		return(

			<div className={className}></div>

		)
	}

});

export default Range;
