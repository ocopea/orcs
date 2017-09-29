// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DashboardCard from  './dashboard.card.comp.js';
import DevDashboardActions from '../../../../module-development/js/actions/dev-dashboard-actions.js';
import CopyHistoy from './dashboard.copyhistory.comp.js';
import Actions from '../../actions/actions.js';
import Chart from '../../../assets/images/siteSetup/google-chart.png';
import Sankey from './dashboard.sankey.comp.js';
import _ from 'lodash';

let Dashboard = React.createClass({

    getCards: function(){

        var that = this;
        var dashboardStatisticsAppCopies = this.props.dashboardStatisticsAppCopies;
        Object.size = function(obj){
            var size = 0, key, cards = [];
            for(key in obj){
                cards.push(
                    <DashboardCard
                        hash={that.props.hash}
			                  appInstanceId={that.props.appInstanceId}
                        key={size}
                        data={obj[key]}
                        dashboardStatisticsAppCopies={dashboardStatisticsAppCopies}
			                  isAvailabilityZoneLoading={that.props.isAvailabilityZoneLoading}/>
                )
                size++;
            }
            return cards;
        }
        var cards = Object.size(this.props.cards);
        return cards;
    },

    render: function(){

        return (
          <div id="dashboard-container">
			      <div className="wrapper">

      					<div className="row">
      						{this.getCards()}
      					</div>

      					<div className="row">
      						<CopyHistoy
                      hash={this.props.hash}
      								copyHistory={this.props.dashboardCopyHistory}
                      isLoading={this.props.dashboardCopyHistory.isLoading}
                      sankeyLoading={this.props.sankey.isLoading}
                      isValid={this.props.dashboardCopyHistory.isValid}
      								isDashboardInlineErrorRender={this.props.isDashboardInlineErrorRender}
                      intervalStart={this.props.dashboardCopyHistory.intervalStart}
                      range={this.props.dashboardCopyHistory.range}
                      copies={this.props.dashboardCopyHistory.copies}
                      prevCopies={this.props.dashboardCopyHistory.prevCopies}
                      isToolTipRender={this.props.tooltip.isRender}
      								tooltip={this.props.tooltip}
                      tooltipPositionLeft={this.props.tooltip.position.left}/>
      					</div>

                {
                  _.size(this.props.dashboardAppDataDistribution) > 0 ?
                    <div className="row chart-row">
                      <Sankey
                        dashboardAppDataDistribution={this.props.dashboardAppDataDistribution}
                        isOnline={this.props.isOnline}
                        sankey={this.props.sankey}/>
                    </div>
                    : null
                }

             {/* /wrapper */}
    				 </div>
          {/* /dashboard-container */}
          </div>
        )
    }
});

export default Dashboard;
