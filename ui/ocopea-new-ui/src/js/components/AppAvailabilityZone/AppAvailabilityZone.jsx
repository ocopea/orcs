// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-app-availability-zone.scss';
import textures from 'textures';
var topojson = require('topojson')
import world from './world-110m.json';


@observer
export default class AppAvailabilityZone extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    appGeography: React.PropTypes.object.isRequired
  }

  render(){

    const { appGeography } = this.props;

    return(
      <div className={styles.AppAvailabilityZone}>
        <div className="title">app availability zone</div>
        <div className={styles.inside}>
          <div id="availability-zones-map"></div>
        </div>
      </div>
    )
  }

  initChart(){

    d3.select("#availability-zones-map").select('*').remove();

		var width = 283.52,
			height = 224;

    var rotationDuration = 2500;
		var rotationDegree = 360;
    var scale = height / 2 - 5;

		var that = this;

    var svg = d3.select("#availability-zones-map").append("svg")
        .attr("width", width)
        .attr("height", height);

    var g = svg.append("g");

    //set location to display on map
    //mock locations
    var israel = [34.8516, 31.0461],
    italy = [12.5674, 41.8719],
    alaska = [149.4937, 64.2008],
    ny = [-74.0059, 40.7128],
    china = [104.1954, 35.8617],
    sf = [-122.4194, 37.7749],
		australia = [133.7751, -25.2744],
		indonesia = [113.9213, -0.7893];

    var locations = formatCoordinates(this.props.appGeography.locations)
    //var locations = [israel, italy, alaska, ny, china, sf];
    //var locations = [australia, indonesia];
    //console.log(locations)
    function formatCoordinates(locations){
        var newLocations = locations.map((location)=>{
            var coords = location.geometry.coordinates;
            return [coords.lat, coords.lng ];
        });
        return newLocations;
    }

    //define two projections to interpolate
    var projection = interpolatedProjection(
        d3.geo.orthographic()
		.scale(scale + 50)
            .translate([width / 2, height / 2]),
        d3.geo.equirectangular()
		.scale(scale + 70)
            .translate([width / 2, height / 2])).clipAngle(90);

    var path = d3.geo.path()
        .projection(projection);

    //rotate
    d3.transition()
    .duration(rotationDuration)
    .tween("rotate", function() {
    var r = d3.interpolate(projection.rotate(), [rotationDegree, 0, 0]);
        return function(t) {

            projection.rotate(r(t));

            g.selectAll("path").attr("d", path);

            if(r(t)[0] == rotationDegree){
                animation();
            }

        };

    })

    var feature = g.selectAll("path");

    //animate projections and zoom to locations
    function animation() {

        g.transition()
        .duration(rotationDuration)
        .tween("projection", function() {

            return function(_) {

                projection.alpha(_);
                g.selectAll("path").attr("d", path);

                if(_ == 1){
          				projection.clipAngle(null);
          				g.selectAll("path").attr("d", path);
                  addLocations();

                }

            };
        })
    }

    //add locations by coordinates
    function addLocations(){

        var pointsContainer = g.append("g").classed("pointsContainer", true);
        var circles = pointsContainer.selectAll("g")
            .data(that.props.appGeography.locations)
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
                .attr("cx", function(d){
                  const coordinates = d.geometry.coordinates;
                  const coords = [coordinates.lng, coordinates.lat];
                  return projection(coords)[0]
                })
                .attr("cy", function(d){
                  const coordinates = d.geometry.coordinates;
                  const coords = [coordinates.lng, coordinates.lat];
                  return projection(coords)[1]
                })

        circles
                .append("circle")
                .classed("location", true)
                .attr("r", "4px")
                .attr("fill", "#84bd5d")
                .attr("cx", function(d){
                  const coordinates = d.geometry.coordinates;
                  const coords = [coordinates.lng, coordinates.lat];
                  return projection(coords)[0]
                })
                .attr("cy", function(d){
                  const coordinates = d.geometry.coordinates;
                  const coords = [coordinates.lng, coordinates.lat];
                  return projection(coords)[1]
                })

        circles
                .append("circle")
                .classed("location", true)
                .attr("r", "2px")
                .attr("fill", "#fff")
                .attr("cx", function(d){
                  const coordinates = d.geometry.coordinates;
                  const coords = [coordinates.lng, coordinates.lat];
                  return projection(coords)[0]
                })
                .attr("cy", function(d){
                  const coordinates = d.geometry.coordinates;
                  const coords = [coordinates.lng, coordinates.lat];
                  return projection(coords)[1]
                })

        var proportions = getProportions();
        zoom(proportions);
    }

		//handle tooltip
		function showTooltip(data){

      var coordinates = data.geometry.coordinates;

      var tooltip =  d3.select(".pointsContainer")
                                  .append("g")
                                  .attr("class", "map-tooltip")

      var rect = tooltip
              .append("rect")
                  .attr("y", projection([coordinates.lat, coordinates.lng])[1] + 10 )
                  .attr("x", projection([coordinates.lat, coordinates.lng])[0] - 20 )
                  .attr("width", 60)
                  .attr("height", 20)
                  .attr("fill", "#303132")
			.attr("rx", 1)
			.attr("ry", 1)

      var text = tooltip.append("text")
              .attr("fill", "#fff")
              .attr("y", projection([coordinates.lat, coordinates.lng])[1] + 22 )
              .attr("x", projection([coordinates.lat, coordinates.lng])[0] - 17 )
              .attr("font-size", "8px")
              .text(data.properties.name)

      var traingle = tooltip
              .attr("fill", "#303132")
              .append('path')
                  .attr('d', function(d) {
                      var x = projection([coordinates.lat, coordinates.lng])[1],
                          y = projection([coordinates.lat, coordinates.lng])[0] + 6.5;
                      return 'M ' + x +' '+ y + ' l 4 4 l -8 0 z';
                  });

		}

		function hideTooltip(){
			d3.select(".map-tooltip").remove();
		}

    //get new proportions by location coordinates
    function getProportions(){

        var points = d3.selectAll(".location")[0];

        var maxX = d3.max(points, function(d) { return d.cx.baseVal.value;} );
        var minX = d3.min(points, function(d) { return d.cx.baseVal.value;} );
        var maxY = d3.max(points, function(d) { return d.cy.baseVal.value;} );
        var minY = d3.min(points, function(d) { return d.cy.baseVal.value;} );

        return {maxX: maxX, minX: minX, maxY: maxY, minY: minY};
    }

    //translate and zoom to new proportions
    function zoom(proportions){

			if(proportions.maxX != undefined){

				g.transition()
					.duration(1500)
					.attr("transform", "translate("+[
							-proportions.minX + getPaddingX(proportions),
							-proportions.minY + getPaddingY(proportions)
					]+")"+getScale())

			}

      function getScale(){
        if(locations.length == 1){
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

			function getPaddingY(proportions){
				//single location
				if(locations.length == 1){
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
						return 0
					}

				}
			}

      function getPaddingX(proportions){

        var loc = locations[0];

        var leftX = projection([loc[0], loc[1]])[0];
        var rightX = projection([loc[0], loc[1]])[1];

				//single location
				if(locations.length == 1){
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
						return -150
					}else if(proportions.maxX - proportions.minX < 50){
						return -40;
					}else{
						return 20;
					}

		    }

      }
    }


    //handle projection interpolation
    function interpolatedProjection(a, b) {

        var projection = d3.geo.projection(raw).scale(0.5),
          center = projection.center,
          translate = projection.translate,
          α;

        function raw(λ, φ) {
        var pa = a([λ *= 180 / Math.PI, φ *= 180 / Math.PI]), pb = b([λ, φ]);
        return [(1 - α) * pa[0] + α * pb[0], (α - 1) * pa[1] - α * pb[1]];
        }

        projection.alpha = function(_) {
        if (!arguments.length) return α;
        α = +_;
        var ca = a.center(), cb = b.center(),
            ta = a.translate(), tb = b.translate();
        center([(1 - α) * ca[0] + α * cb[0], (1 - α) * ca[1] + α * cb[1]]);
        translate([(1 - α) * ta[0] + α * tb[0], (1 - α) * ta[1] + α * tb[1]]);

        return projection;
        };

        delete projection.scale;
        delete projection.translate;
        delete projection.center;
        return projection.alpha(0);

    }

    var globe = {type: "Sphere"},
        land = topojson.feature(world, world.objects.land),
        countries = topojson.feature(world, world.objects.countries).features,
        boundary = topojson.mesh(world, world.objects.countries, function(a, b) { return a !== b; });

        var t = textures.circles().lighter().size(3).fill("#e1e1e1");

        g.call(t);

        //append countries path
        feature
            .data(countries)
            .enter()
                .append("path")
                .attr("d", path(land))
                .style("fill", t.url())
                .attr("stroke", "#e1e1e1");

    }

    rotate: true;

    componentDidMount(){
        this.firstTime = true;
        this.initChart();
    }

    componentWillReceiveProps(nextProps) {
      if(this.props.appGeography !== nextProps.appGeography) {
        this.initChart();
      }
    }

}
