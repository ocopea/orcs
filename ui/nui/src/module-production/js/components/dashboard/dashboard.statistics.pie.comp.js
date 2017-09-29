// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Highcharts from 'highcharts';
import DashboardStore from '../../stores/dashboard/_dashboard-main-store.js';
import ComponentLoadingGif from '../../../assets/gif/component-loading.gif';
import BrowserDetector from '../../browserDetection.js';
import Actions from '../../actions/actions.js';
import $ from 'jquery';

let StatisticsPie = React.createClass({

    initChart: function(){
        
        var options = this.getOptions();
        var chart = new Highcharts.Chart(options);
		
		//remove highcharts promotional link
		var pie = document.getElementById("statistics-pie");
		var parent = pie.childNodes[0].lastChild;
		var elementToRemove = parent.lastChild;
		parent.removeChild(elementToRemove);			
				        
    },
    
    getTotalAmountOfCopies: function(){
        
        var allCopies = this.props.dashboardStatisticsAppCopies;
        var totalAmount = 0;
		
        allCopies.forEach(function(copie, index){
           totalAmount += copie.y; 
        });
        
        return totalAmount;
    },
    
    getOptions: function(){
        
        var data = this.props.dashboardStatisticsAppCopies;
        var totalAmountOfCopies = this.getTotalAmountOfCopies();
				
        var options = {
            chart: {
                backgroundColor: "#FAFAFA",
                width: 270,
                height: 220,
                renderTo: 'statistics-pie',
                type: 'pie',
                style: {
                    fontFamily: 'Helvetica',
                },
                animation: {
                    duration: 1200
                }
            },
            title: {
                text: 	totalAmountOfCopies == 1 ? 
						totalAmountOfCopies + "<br>" + ' COPY' : 
						totalAmountOfCopies + "<br>" + ' COPIES' ,
                y: 60,
                x: -4,
                floating: true,
                style: {color: '#797979', fontSize: '23px'},
            },
            plotOptions: {
                pie: {
                    size:'100%',
                    shadow: false,
                    point:{
                        events:{
                           legendItemClick: function () {
                               return false; 
                            }
                        }                            
                    }                        
                },
            series: {
                    states: {
                        hover: {
                            halo: {
                                size: 7,
                                attributes: {
                                    fill: '#000',
                                    strokeWidth: 1
                                }
                            }
                        }
                    }
                },
            },

            tooltip: {
                formatter: function() {
                    return '<b>'+ this.point.name + '</b>: <br>' + ' count: ' + this.y;
                }
            },
            legend: {
                itemHiddenStyle: {
                    color: 'green',
                    itemWidth: 15
                },                                      
                labelFormatter: function () {
                    return '<div>'+
                                '<span style="color:'+this.color+'">'+
                                    this.name+
                                    '<br>'+
                                    '<span style="color:'+this.color+'">'+this.y+'</span>'
                                '</span>'
                           '</div>'                  
                },
                align: "center",
                symbolWidth: 0,
                itemWidth: 75,
                x: 20,
                itemDistance: 15,
            },
            series: [{
                name: '',
                data: data,
                size: '100%',
                innerSize: '92%',
                showInLegend:true,
                dataLabels: {
                    enabled: false
                }
            }]
        }
        return options;        
    },   
	
	componentDidMount: function(){
		this.initChart();        
	},
	
    componentDidUpdate: function(newProps){    
		
        var that = this;     	       	
		
		
		
        if(this.props.dashboardStatisticsAppCopies.length != newProps.dashboardStatisticsAppCopies.length){
            this.initChart();
        }
        
        //copy change
        if(this.props.dashboardStatisticsAppCopies.length != newProps.dashboardStatisticsAppCopies.length){
            //hash change
            if(this.props.hash == newProps.hash){
                //loading change
                if(this.props.isLoading != newProps.isLoading){
                    this.initChart();        
                }
            }
        }
        
        //iterate appCopies to detect change in y param (repurpose copy)
        this.props.dashboardStatisticsAppCopies.forEach(function(copy, index){	
            if(newProps.dashboardStatisticsAppCopies[index] != undefined){
               if(copy.y != newProps.dashboardStatisticsAppCopies[index].y){
                   
                   that.initChart();     
               } 
            }
        });
        
    },
            
    render: function(){
        
        return (
			<div>
				{				
					this.props.isLoading ?

						<img src={ComponentLoadingGif} id="component-loading-gif"/>

					:

					null
				}
				<div id="statistics-pie"></div>                        
			</div>
        )

    },
});

export default StatisticsPie;