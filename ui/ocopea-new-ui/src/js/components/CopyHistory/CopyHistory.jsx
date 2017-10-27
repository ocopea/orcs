// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import FilterRange from './Filters.jsx';
import styles from 'style!./styles-copy-history.scss';
import Footer from './Footer.jsx';
import ImgSeparator from './assets/separator.png';
import ToolTip from './Tooltip/ToolTip.jsx';
import ComponentLoadingGif from './assets/component-loading.gif';
import $ from 'jquery';
import d3 from 'd3';
import Helper from '../../utils/helper';
import AppInstanceHandler from '../../models/AppInstance/appInstance-handler';
import { observer } from 'mobx-react';
import { observable } from 'mobx';
import moment from 'moment';
import errorMessages from './errorMessages.json';


@observer
export default class CopyHistoy extends React.Component{

    @observable data = {};

    constructor(props) {
      super(props)

      const copyHistoryPeriod = "week";
      const copyHistoryInterval = 0;
      const instanceID = this.props.instanceID;
      this.fetchCopyHistory(instanceID, copyHistoryInterval, copyHistoryPeriod);

      this.state = {
        interval: 0,
        intervalEnd: {},
        period: 'week',
        error: '',
        copies: [],
        tooltip: {
          isRender: false,
          copy: {},
          position: {}
        }
      }

    }

    static propTypes = {
      instanceID: React.PropTypes.string.isRequired
    }

    render(){
        return (
          <div id="copy-history" className="card">
            {
              this.state.tooltip.isRender ?
                <ToolTip
                  position={this.state.tooltip.position}
                  copy={this.state.tooltip.copy}
                  onRemoveTooltip={this.onRemoveTooltip.bind(this)}/>
              : null
            }

            <div className="inside">
              <button
                  id="copy-history-next-btn"
                  type="button"
                  onClick={this.userClickOnCopyHistoryNext.bind(this)}>
                      ►
              </button>

              <button
                  id="copy-history-prev-btn"
                  type="button"
                  onClick={this.userClickOnCopyHistoryPrev.bind(this)}>
                      ◄
              </button>

              <h1 className="title">
                  <span className="title__span">copy history</span>
                  <FilterRange
                    onRangeChange={this.onRangeChange.bind(this)}
                    period={this.state.period}/>
              </h1>

              <div className="inlineError">{this.state.error}</div>
              <Footer copies={this.state.copies} instanceID={this.props.instanceID}/>
            </div>
          </div>
        )
    }

    fetchCopyHistory(instanceID, interval, period) {
      AppInstanceHandler.fetchCopyHistory(instanceID, interval, period, response => {
        const copies = response.copies;
        const intervalEnd = response.intervalEnd;

        this.setState({
          intervalEnd: intervalEnd,
          copies: copies,
          prevCopies: this.state.copies
        });
        this.initChart(copies);
      });
    }

    initChart(data){

  		var animation = this.handleAnimation();

      var that = this;
      var w, h, tickFormat;

      if(this.state.period == 'year'){
          tickFormat = "%d-%b-%y";
      }else if(this.state.period == "day"){
		       tickFormat = "%H:%M";
  		}else{
              tickFormat = "%d-%b";
      }

      h = 150;
      w = 850;

      if($("#copy-history .inside").has("svg")){
          $("#copy-history .inside svg").remove();
      }

      // Set the dimensions
      var margin = {top: 10, right: 50, bottom: 130, left: 50};
      var width = w - margin.left - margin.right;
      var height = h - margin.top - margin.bottom;

      // Set the ranges
      var x = d3.time.scale().range([margin.left - 30, width - margin.right]);
      var prevBackupsX = d3.time.scale().range([0, width - margin.right]);

      // Define the axes
      function getTicks(period) {
        switch(period) {
          case "year":
            return 5;
            break;
          default: return 7;
        }
      }

      var xAxis = d3.svg.axis().scale(x)
          .orient("bottom").ticks(getTicks(this.state.period))
          .tickSize(0)
          .tickFormat(d3.time.format(tickFormat));

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

  		var prevCopies = !this.state.error.length ? this.state.prevCopies : [];

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
                  .attr("width", function(d){
                    if(d.hasOwnProperty("status")){return 24}else{return 0}
                  })
                  .attr("height", 40)
          				.attr("stroke", "#fff")
          				.attr("stroke-width", 1)
                  .on('click', function(d){
                    const element = this;
                    that.onCopyClick(d, element);
          				})
                  .attr("fill", function(d){
          					return that.getBackupColor(d.status)
          				})
                  .attr("clip-path", "url(#clip)" )
                  //backups initial position
                  .attr("y", 55)
                  .attr("x", function(){return animation.direction == "right" ? 30 : width})
                      .append("svg:title")
                      .style({"text-anchor": "start"})
                      .html(function(d){
                        return d.status + '</br>' +
                          Helper.formatDate(d.timeStamp).date + '</br>' +
                          Helper.formatDate(d.timeStamp).time
                      });

          // Add the X Axis
          svg.append("g")
              .attr("class", "x axis")
              .attr("transform", "translate(50,110)")
              .attr("fill", "#8c8c8c")
              .call(xAxis)
              .selectAll("text")
                  .style({
          					"text-anchor": "start",
          					"stroke":"#000",
          					"stroke-width":"0px",
          					"font-size": "13px"
          				});

          //animation
          g.selectAll(".backup")
          .transition()
            .duration(animation.animationDuration)
            .attr("x", function(d,i){
      				if(that.state.period != "day"){
      					return x(d.timeStamp)// - 30
      				}else{
      					var date = new Date(d.timeStamp);
      					var time = formatTime(date.getHours()) + ":" + date.getHours(date.getMinutes())
      					return x(d.timeStamp) + 30;
      				}

      				function formatTime(number){
      					return number < 100 ? "0" + number : number;
      				}
			    }).ease(animation.animationType);

        g.selectAll(".prevBackups")
        .transition()
            .duration(animation.animationDuration + 200)
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
  }

  onCopyClick(copy, element) {
    let position = $(element).position();
    position.left -= 320;
    this.setState({
      tooltip: {
        isRender: true,
        copy: copy,
        position: position
      }
    });
  }

  onRemoveTooltip(e) {
    this.setState({
      tooltip: {
        isRender: false,
        copy: {},
        position: {}
      }
    });
  }

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
	}

  handleAnimation(){

      var animation = {
          animationType: "",
          animationDuration: 3500,
          direction: "right"
      };

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

          case "error":
              animation.animationType = "bounce";
              animation.animationDuration = 700;
              break;
      }

      return animation;
    }

    userClickOnCopyHistoryPrev(){
        this.onIntervalChange('back');
        this.activator = "next";
    }

    userClickOnCopyHistoryNext(){
        this.onIntervalChange('forward');
        this.activator = "prev";
    }

    onRangeChange(period) {
      this.setState({
        period: period
      });
      const instanceID = this.props.instanceID;
      const interval = this.state.interval;
      this.fetchCopyHistory(instanceID, interval, period);
      this.setState({ error: "" })
    }

    onIntervalChange(direction) {
      const instanceID = this.props.instanceID;
      const period = this.state.period;
      const directions = {
        forward: 'forward',
        back: 'back'
      };
      const interval = this.state.interval;
      const intervalEnd = this.state.intervalEnd;
      let new_interval = 0;

      switch (direction) {
        case directions.forward:
          if(interval > 0){
            new_interval = interval - 1;
            this.fetchCopyHistory(instanceID, new_interval, period);
            this.setState({ error: "" })
          }else{
            // invalid date
            let random = Math.floor(Math.random() * errorMessages.length) + 0;
            this.setState({ error: errorMessages[random] });
            this.activator = "error"
            this.initChart(this.state.copies);
          }
          break;
        case directions.back:
          new_interval = interval + 1;
           this.fetchCopyHistory(instanceID, new_interval, period);
           this.setState({ error: "" });
          break;
      }
      this.setState({ interval: new_interval });
    }

    activator: ""

    componentDidMount(){
	    this.activator = "init";
    }

    componentWillReceiveProps(nextProps) {
      // if(this.props.instanceID !== nextProps.instanceID) {
        const instanceID = nextProps.instanceID;
        this.fetchCopyHistory(instanceID, 0, 'week');
      // }
    }


};
