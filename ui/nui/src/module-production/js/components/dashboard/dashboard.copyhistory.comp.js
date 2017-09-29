// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import FilterRange from './dashboard.copyHistory.filterRange.comp.js';
import Actions from '../../actions/actions.js';
import CopyHistoryFooter from './dashboard.copyHistory.footer.comp.js';
import CopyHistoryInlineError from './dashboard.copyHistory.inlineError.comp.js';
import ImgSeparator from '../../../assets/images/dashboard/copyHistory/separator.png';
import ToolTip from '../tooltips/tt.copyHistory.comp.js';
import ComponentLoadingGif from '../../../assets/gif/component-loading.gif';
import $ from 'jquery';
import d3 from 'd3';

let CopyHistoy = React.createClass({

    initChart: function(animationType, animationDuration){

		var animation = this.handleAnimation(animationType, animationDuration);
        var that = this;
        var w, h, tickFormat;

        if(this.props.copyHistory.range == 'year'){
            tickFormat = "%d-%b-%y";
        }else if(this.props.range == "day"){
			tickFormat = "%H:%M";
		}else{
            tickFormat = "%d-%b";
    }

    h = 150;

    if($("#main-pane").hasClass('maximized')){
        w = 1200;
    }else{
        w = 850;
    }

    if($("#copy-history .inside").has("svg")){
        $("#copy-history .inside svg").remove();
    }

    // Set the dimensions of the canvas / graph
    var margin = {top: 10, right: 50, bottom: 130, left: 50};
    var width = w - margin.left - margin.right;
    var height = h - margin.top - margin.bottom;

    // Set the ranges
    var x = d3.time.scale().range([0, width - 50]);
    var prevBackupsX = d3.time.scale().range([0, width - 50]);

    // Define the axes
    var xAxis = d3.svg.axis().scale(x)
        .orient("bottom").ticks(7)
        .tickSize(0)
        //.innerTickSize(-8)
        .tickFormat(d3.time.format(tickFormat)); // tickFormat

    // Adds the svg canvas
    var svg = d3.select("#copy-history .inside")
        .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)

		var clip = svg.append("svg:clipPath")
		  .attr("id", "clip")
			.append('svg:rect')
			.attr('width', width)
			.attr('height', 40)
			.attr('transform', 'translate('+ [0,55]+')' )

		var g = svg.append("g")
                .attr("transform",
                      "translate(" + margin.left + "," + margin.top + ")")
                .attr("clip-path", "url(#clip)" );

    var background = g.append("rect")
      .attr("class", "background")
			.attr("width", 773)
			.attr("fill", "#e0e0e0")
			.attr("y", 55)
			.attr("x", -13)
			.attr("height", 40)

    // Get the data
    var data = this.props.copies;

		var prevCopies = this.props.prevCopies;
		prevCopies = !this.props.isValid ||
                  animationType == "bounce" ||
                  this.activator == "init" ? [] : prevCopies;

    g.append("line")
            .attr("x1", 0)
            .attr("y1", 75)
            .attr("x2", 750)
            .attr("y2", 75)
            .attr("stroke-width", 1)
            .attr("stroke", "#ffffff");

		// Scale the range of the data
        x.domain(d3.extent(data, function(d) { return d.timeStamp; }));
        prevBackupsX.domain(d3.extent(prevCopies, function(d) { return d.timeStamp; }));

		//previous backups for animation
		g.selectAll(".prevBackups")
			.data(prevCopies)
			.enter()
				.append('rect')
				.classed('prevBackups', true)
				.attr("width", function(d){ if(d.hasOwnProperty("status")){return 24}else{return 0} })
                .attr("height", 40)
				.attr("stroke", "#fff")
				.attr("stroke-width", 1)
				//previous backups initial position
				.attr("y", 55)
                .attr("x", function(d){return prevBackupsX(d.timeStamp) - 30})
                .attr("fill", function(d){
				  return that.getBackupColor(d.status)
				})
				.attr("clip-path", "url(#clip)" )

        g.selectAll(".backup")
            .data(data)
            .enter()
                .append("rect")
                .classed('backup', true)
                .attr("width", function(d){ if(d.hasOwnProperty("status")){return 24}else{return 0} })
                .attr("height", 40)
        				.attr("stroke", "#fff")
        				.attr("stroke-width", 1)
                .on('click', function(d){
        					Actions.removeCopyHistoryErrorMsg();
        					return Actions.userClickOnCopyHistoryBackup.bind(this, this, d)();
        				})
                .attr("fill", function(d){
        					return that.getBackupColor(d.status)
        				})
                .attr("clip-path", "url(#clip)" )
                //backups initial position
                .attr("y", 55)
                .attr("x", function(){return animation.direction == "right" ? 0 : width})
                    .append("svg:title")
                    .style({"text-anchor": "start"})
                    .html(function(d){ return d.status+'<br/>'+d.timeStamp+'<br/>'+d.copyId })

        // Add the X Axis
        svg.append("g")
            .attr("class", "x axis")
            // .attr("transform", "translate(0,110)")
            .attr("transform", "translate(70,110)")
            .attr("fill", "#8c8c8c")
            .call(xAxis)
            .selectAll("text")
                .style({
        					"text-anchor": "start",
        					"stroke":"#000",
        					"stroke-width":"0px",
        					"font-size": "13px"
        				})
            // .attr("dy", 110)
            // .attr("dx", 100)

        //animation
	      var activator = this.activator;

        g.selectAll(".backup")
        .transition()
            .duration(animation.animationDuration)
            .attr("x", function(d,i){

				if(that.props.range != "day"){
					return x(d.timeStamp) - 30
				}else{

					var date = new Date(d.timeStamp);
					var time = formatTime(date.getHours()) + ":" + date.getHours(date.getMinutes())

					return x(d.timeStamp) - 30
				}

				function formatTime(number){
					return number < 100 ? "0" + number : number;
				}

			})
            .ease(animation.animationType);

        g.selectAll(".prevBackups")
        .transition()
            .duration(animation.animationDuration+200)
            .attr("transform", function(d,i){
                return animation.direction == "right" ? "translate(1000,0)" : "translate(-1000,0)"
            })
            .ease(animation.animationType);

		//separators
        svg.selectAll("separator").data([0])
            .enter()
            .append("svg:image")
            .attr("xlink:href", ImgSeparator)
            .attr('class','separator-left')
            .attr('x',40)
            .attr('y',42)
            .attr('width', 20)
            .attr('height', 83)

        svg.selectAll("separator").data([0])
            .enter()
            .append("svg:image")
            .attr("xlink:href", ImgSeparator)
            .attr('class','separator-right')
            .attr('x',790)
            .attr('y',43)
            .attr('width', 20)
            .attr('height', 83)
    },

	getBackupColor(status){
		var color;
			switch(status){
				case "created":
					color = "#83bd5c";
					break;

				case "failed":
					color = "#bd4c49";
					break;

				case "scheduled":
					color = "#83bd5c";
					break;

				case "inprogress":
					color = "#bd4c49";
					break;
        case "creating":
          color = "#83bd5c";
          break;
			}
			return color;
	},

    handleAnimation: function(animationType, animationDuration){

        var animation = {
            animationType: "",
            animationDuration: 3500,
            direction: "right"
        };

        if(animationType != undefined && animationDuration != undefined){
            animation.animationType = animationType;
            animation.animationDuration = animationDuration;
        }

        if(animationType != "bounce"){
            switch(this.activator){
                case "init":
                    animation.direction = "right";
                    animation.animationType = "elastic";
                    animation.animationDuration = 3500;
                    break;

                case "next":
                    animation.direction = "left";
                    animation.animationType = "linear";
                    animation.animationDuration = 700;
                    break;

                case "prev":
                    animation.direction = "right";
                    animation.animationType = "linear";
                    animation.animationDuration = 700;
                    break;

                case "range":
                    animation.animationType = "linear";
                    animation.animationDuration = 700;
                    break;

                default:
                    animation.animationType = "elastic";
                    animation.animationDuration = 3500;
                    break;
            }

        }
        return animation;
    },

    userClickOnCopyHistoryPrev: function(){
        Actions.userClickOnCopyHistoryPrevBtn();
        Actions.hideCopyHistoryRestoreTooltip();
        this.activator = "next";
    },

    userClickOnCopyHistoryNext: function(){
        Actions.userClickOnCopyHistoryNextBtn();
        Actions.hideCopyHistoryRestoreTooltip();
        this.activator = "prev";
    },

    activator: "",

    shouldComponentUpdate: function(nextProps){
        //set activator to handle animation
        if(this.props.range != nextProps.range){
            this.activator = "range";
        }

        if(this.props.isDashboardInlineErrorRender != nextProps.isDashboardInlineErrorRender){
            if(!this.props.isDashboardInlineErrorRender){
                this.initChart("bounce", 500)
            }
            return true;

        }else{
			return true;
		}
    },

    componentDidMount: function(){
		this.activator = "init";
    },

    componentDidUpdate : function(nextProps){

      if(this.props.intervalStart != nextProps.intervalStart){

    			if(this.activator == "init"){

    				if(!this.firstTime){
    					this.firstTime = true;
    					this.initChart();
    				}

    			}else{
    				this.initChart();
    			}
      }

      if(this.props.isToolTipRender != nextProps.isToolTipRender ||
         this.props.tooltipPositionLeft != nextProps.tooltipPositionLeft ||
         this.props.copies.length == nextProps.copies.length ||
         this.props.isValid != nextProps.isValid){

      }else{
          this.initChart();
      }

  		if(this.props.copies.length != nextProps.copies.length){
  			this.initChart();

  		}

    },

    render: function(){
        //console.log(this.props)
        return (
            <div id="copy-history" className="card">

            {
                this.props.sankeyLoading || this.props.isLoading ?

                    <img src={ComponentLoadingGif} id="component-loading-gif"/>
                :

                null

            }

            <div className="inside">

                    <ToolTip data={this.props.tooltip}/>

                    <button
                        id="copy-history-next-btn"
                        type="button"
                        onClick={this.userClickOnCopyHistoryPrev}>
                            ►
                    </button>

                    <button
                        id="copy-history-prev-btn"
                        type="button"
                        onClick={this.userClickOnCopyHistoryNext}>
                            ◄
                    </button>

                    <h1 className="title">
                        <span className="title__span">copy history</span>
                        <FilterRange range={this.props.copyHistory.range}/>
                    </h1>

					<CopyHistoryInlineError data={this.props.copyHistory.inlineError}/>

                </div>

                <CopyHistoryFooter allCopies={this.props.copyHistory.copies}/>

            </div>
        )
    }
});

export default CopyHistoy;
