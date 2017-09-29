// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

import autoloader from '../../sankey/autoloader.js';
import ComponentLoadingGif from '../../../assets/gif/component-loading.gif';

let Sankey = React.createClass({

    drawChart: function(){

        var dashboardAppDataDistribution = this.props.dashboardAppDataDistribution;
        var data = new google.visualization.DataTable();        
        data.addColumn('string', 'From');
        data.addColumn('string', 'To');
        data.addColumn('number', 'Weight');
        data.addRows(dashboardAppDataDistribution.sankeyData);

        // Sets chart options.
        var options = {
            width: 887,
            height: 400,
			sankey: {
					node:{
						label:{
							fontName: "sans-serif",
							fontSize: 14
						}
					}
				}
			};

        var chart = new google.visualization.Sankey(document.getElementById('sankey_basic'));
        chart.draw(data , options);

    },

    componentDidMount: function(){
        function fun(){
            //console.log('fun indeed');
        }

        if(navigator.onLine){
            setGoogleChart();
        }

        function setGoogleChart(){
            google.setOnLoadCallback(fun);
//            google.charts.load('current', {'packages':['sankey']});
        }

        if(this.props.dashboardAppDataDistribution.sankeyData != undefined)
            this.drawChart();
    },

    componentDidUpdate: function(){
        if(this.props.dashboardAppDataDistribution.sankeyData != undefined)
            this.drawChart();
    },

    isFirstTime: true,

    render: function(){

        this.drawChart;

        var style= {
            marginTop: 50
        }
        return (
			<div>
				{
					this.props.sankey.isLoading ?

						<img src={ComponentLoadingGif} id="component-loading-gif"/>

					:

					null
				}
				<div id="sankey_basic" style={style}></div>
			</div>
        )
    },



});

export default Sankey;
