// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import d3 from 'd3';
import textures from 'textures';
var topojson = require('topojson');
import Config from '../../../../module-production/js/config.js';


var ServiceInfo = React.createClass({

  shouldComponentUpdate: function(nextProps){
    return this.props.selectedElement !== nextProps.selectedElement;
  },

  componentDidMount: function(){
    this.initMap();
  },

  mockCoordinates: function(){
    return {
      israel: [34.9005128, 32.1792126],
      italy: [12.5674, 41.8719],
      alaska: [149.4937, 64.2008],
      ny: [-74.0059, 40.7128],
      china: [104.1954, 35.8617],
      sf: [-122.4194, 37.7749],
      australia: [133.7751, -25.2744],
      indonesia: [113.9213, -0.7893]
    }
  },

  locations: function(){
    return [
      {coordinates: this.mockCoordinates()['israel'], name: 'israel'},
      // {coordinates: this.mockCoordinates()['china'], name: 'china'}
    ]
  },

  initMap: function(){

    d3.select(".Dashboard__inside__service-info__map").select('*').remove();

    var that = this;

    var width = 279,
        height = 217,
        scale = height / 2 - 5;

    var svg = d3.select('.Dashboard__inside__service-info__map')
              .append('svg')
              .attr({
                height: height,
                width: width
              });

    var container = svg.append('g').classed('Dashboard__inside__service-info__map__container', true)

    var projection = d3.geo.equirectangular()
            .translate([width / 2, height / 2]);

    var path = d3.geo.path()
        .projection(projection);

    var options = {
      url: PUBLICDOMAIN + "appAvailabilityZone/world-110m.jsn",
      method: 'GET',
      contentType: 'application/json'
    }

    Config.request(options, response=>{

      var world = JSON.parse(response);

      var land = topojson.feature(world, world.objects.land),
      countries = topojson.feature(world, world.objects.countries).features;

      var t = textures.circles().lighter().size(4).fill("#e1e1e1");

      container.call(t);

      //append countries path
      container.selectAll('path')
          .data(countries)
          .enter()
              .append("path")
              .attr("d", function(d){ return path(d.geometry)})
              .style("fill", t.url())
              .attr("stroke", "#e1e1e1");

      addLocations(that.locations())

    }, error=>{ console.log(error) })


    d3.json(PUBLICDOMAIN + "appAvailabilityZone/world-110m.jsn", function(world){

      // var land = topojson.feature(world, world.objects.land),
      //     countries = topojson.feature(world, world.objects.countries).features;
      //
      //     var t = textures.circles().lighter().size(4).fill("#e1e1e1");
      //
      //     container.call(t);
      //
      //     //append countries path
      //     container.selectAll('path')
      //         .data(countries)
      //         .enter()
      //             .append("path")
      //             .attr("d", function(d){ return path(d.geometry)})
      //             .style("fill", t.url())
      //             .attr("stroke", "#e1e1e1");
      //
      //     addLocations(that.locations())

    });

    function addLocations(locations){
        var that = this;
        var pointsContainer = container.append("g").classed("pointsContainer", true);

        var circles = pointsContainer.selectAll("g")
            .data(locations)
            .enter()
            .append("g")
        			.on('mouseover', showTooltip)
        			.on('mouseout', hideTooltip)

        circles
                .append("circle")
                .classed("location", true)
                .attr("r", "8px")
                .attr("fill", "#d3e1ca")
                .attr("fill-opacity", "0.5")
                .attr("cx", function(d){ return projection(d.coordinates)[0] })
                .attr("cy", function(d){ return projection(d.coordinates)[1] })

        circles
                .append("circle")
                .classed("location", true)
                .attr("r", "4px")
                .attr("fill", "#84bd5d")
                .attr("cx", function(d){ return projection(d.coordinates)[0] })
                .attr("cy", function(d){ return projection(d.coordinates)[1] })

        circles
                .append("circle")
                .classed("location", true)
                .attr("r", "2px")
                .attr("fill", "#fff")
                .attr("cx", function(d){ return projection(d.coordinates)[0] })
                .attr("cy", function(d){ return projection(d.coordinates)[1] })

        var proportions = getProportions();
        zoom(proportions);
    }

    function getProportions(){

        var points = d3.selectAll(".location")[0];

        var maxX = d3.max(points, function(d) { return d.cx.baseVal.value;} );
        var minX = d3.min(points, function(d) { return d.cx.baseVal.value;} );
        var maxY = d3.max(points, function(d) { return d.cy.baseVal.value;} );
        var minY = d3.min(points, function(d) { return d.cy.baseVal.value;} );

        return {maxX: maxX, minX: minX, maxY: maxY, minY: minY};
    }

    function zoom(proportions){

			if(proportions.maxX != undefined){

				container.transition()
					.duration(1500)
					.attr("transform", "translate("+[
							-proportions.minX + getPaddingX(proportions),
							-proportions.minY + getPaddingY(proportions)
					]+")"+getScale())

			}
      function getPaddingY(proportions){

				//single location
				if(that.locations().length == 1){
					if(proportions.maxX - proportions.minX > width){
						return 50;
					}else{
						return 20;
					}
				}
				//multiple locations
				else{
					if(proportions.maxX - proportions.minX > width){
						return 50;
					}else{
						return 20
					}

				}
			}

      function getPaddingX(proportions){

        var loc = that.locations()[0];

        var leftX = projection([loc[0], loc[1]])[0];
        var rightX = projection([loc[0], loc[1]])[1];

				//single location
				if(that.locations().length == 1){
					if(loc[0] < 0){
						return leftX + width > 200 ? 100 : 20;
					}else{
						if(rightX < 20){
							return -150;
						}else{
							return -100;
						}
					}
				//multiple locations
				}else{
					if(proportions.maxX - proportions.minX < width && proportions.maxX > width){
						return 20;
					}else if(proportions.maxX - proportions.minX < 50){
						return -40;
					}else{
						return 20;
					}

		    }

      }

      function getScale(){

        if(that.locations().length == 1){
            return "scale(2)";
        }else{
					if(proportions.maxX - proportions.minX < 65){
						return "scale(2)"
					}else if(proportions.maxX - proportions.minX > width){
						return "scale(0.5)"
					}else{
						return "";
					}
        }
      }
    }

    function showTooltip(data){

      var coordinates = data.coordinates;

      var tooltip =  d3.select(".pointsContainer")
                                  .append("g")
                                  .attr("class", "map-tooltip")

      var rect = tooltip
              .append("rect")
                  .attr("y", projection([coordinates[0], coordinates[1]])[1] + 10 )
                  .attr("x", projection([coordinates[0], coordinates[1]])[0] - 20 )
                  .attr("width", 60)
                  .attr("height", 20)
                  .attr("fill", "#303132")
			.attr("rx", 1)
			.attr("ry", 1)

      var text = tooltip.append("text")
              .attr("fill", "#fff")
              .attr("y", projection([coordinates[0], coordinates[1]])[1] + 22 )
              .attr("x", projection([coordinates[0], coordinates[1]])[0] - 17 )
              .attr("font-size", "8px")
              .text(data.name)

      var traingle = tooltip
              .attr("fill", "#303132")
              .append('path')
                  .attr('d', function(d) {
                      var x = projection([coordinates[0], coordinates[1]])[0],
                          y = projection([coordinates[0], coordinates[1]])[1] + 6.5;
                      return 'M ' + x +' '+ y + ' l 4 4 l -8 0 z';
                  });

		}

    function hideTooltip(){
			d3.select(".map-tooltip").remove();
		}

  },

  render: function(){

    return(
      <div className="Dashboard__inside__service-info">
        <div className="Dashboard__inside__service-info__title">
          service info
        </div>
        <div className="Dashboard__inside__service-info__map"></div>
      </div>
    )
  }
});

export default ServiceInfo;
