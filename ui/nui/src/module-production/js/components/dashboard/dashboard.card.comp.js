// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ActiveMapMockImage from '../../../assets/images/dashboard/mock-map-card.png';
import QuotaSummaryMockImage from '../../../assets/images/dashboard/mock-quota-summary.png';
import StatisticsImg from '../../../assets/images/dashboard/mock-statistics-card.png';
import StatisticsPie from './dashboard.statistics.pie.comp.js';

import AvailabilityZones from './dashboard.availabilityZones.comp.js';

let DashboardCard = React.createClass({

    setCardContent: function(){

        var img;
        var that = this;

       switch(this.props.data.title){

           case "quota summary":
                return <img src={QuotaSummaryMockImage} />
                break;
           case "statistics":
                return this.statisticsPie();
                break;
           case "app availability zones":
                return <AvailabilityZones
							           appInstanceId={that.props.appInstanceId}
                         appGeography = {that.props.data.appGeography}
	                       isLoading={that.props.isAvailabilityZoneLoading}
                        />
                //return <img src={ActiveMapMockImage} />
                break;
       }
    },

    statisticsPie: function(){
        return <StatisticsPie
					dashboardStatisticsAppCopies={this.props.dashboardStatisticsAppCopies}
					isLoading={this.props.data.isLoading}
          hash={this.props.hash}/>
    },

    render: function(){

        return (
            <div className="card" id={this.props.data.title}>

                <div className="inside">

                    <h1 className="title">{this.props.data.title}</h1>

                    <div id="main">
                        {this.setCardContent()}
                    </div>

                </div>

            </div>
        )
    }
});

export default DashboardCard;
