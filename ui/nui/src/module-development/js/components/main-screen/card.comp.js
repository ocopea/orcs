// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import MockImg from '../../../assets/images/main-screen-card/mock.png';
import DateIcon from '../../../assets/images/main-screen-card/date-icon.svg';
import ProgressBar from './progress-bar.comp.js';
import Range from './range.comp.js';
import d3 from 'd3';
import DevActions from '../../actions/dev-actions.js';
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import SharedActions from '../../../../shared-actions.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import moment from 'moment';
import Config from '../../../../module-production/js/config.js';
import stateOptions from '../../data/deploying-state-options.js';
import Isvg from 'react-inlinesvg';


var Card = React.createClass({

	shouldComponentUpdate: function(nextProps){
		return this.props.filteredInstances !== nextProps.filteredInstances;
	},

	userClickOnCard: function(e){
		//go to dashboard if instance is running

			if(!e.target.classList.contains('icon-logs')){
				DevDashboardActions.userClickOnMainScreenCard(this.props.instance);
			}else{
				//view application if clicked on arrow icon
				window.open(
					this.props.instance.webEntryPointURL,
				  //Config.getWebEntryPointURL(this.props.instance.name),
				  '_blank'
				);
			}

	},

	getDate: function(){
		var dateCreated = new Date(this.props.instance.dateCreated);
		var dayInMonth = dateCreated.getDate(),
				month = dateCreated.getMonth(),
				thisMonth = moment()._locale._monthsShort[month],
				year = dateCreated.getFullYear();

		var date = dayInMonth + " " + thisMonth + " " + year;
		return date;
	},

	getCleanInstanceName: function(){
		var instanceName = Config.removeWhiteSpaces(this.props.instance.name);

		if(Config.onlyNumbers(instanceName) ||
		 	 Config.startsWithNumber(instanceName)){
			instanceName = "a"+instanceName;
		}

		return instanceName.length === 0 ?
					 "a" + new Date().getTime() : instanceName;
	},

	render: function(){

    if(this.props.creator != undefined){
        var firstName = this.props.creator.firstName,
            lastName = this.props.creator.lastName;
    }
		var instance = this.props.instance,
				instanceState = instance ? instance.state.toUpperCase() : '';
		var isRunning = instanceState === stateOptions.running.toUpperCase();
		
		return(
        <div className="card">
            <div className="inside" onClick={this.userClickOnCard}>
                <div className="title">

                    <img src={APISERVER +
                                '/hub-web-api/user/' +
                                this.props.instance.creatorUserId +
                                '/avatar'}
                                className="avatar" />

                    <div className="username" title={firstName + " " + lastName}>
                        <span>{firstName}</span>
                    </div>

										<div className="date-created">
											<Isvg src={DateIcon} className="date-icon"/>
												{this.getDate()}
										</div>
                </div>

                <div className="icon-container">
										{
											isRunning ?
												<div className="open-icon-container">
													<span className="icon-logs link"></span>
												</div>
											: null
										}
                    {this.props.img}
                </div>

                <div className="instance-name"
									title={Config.getTitleOrNull(this.props.instance.name, 15)}>
                    {Config.getShortName(this.props.instance.name, 15)}
                </div>

      					<div className="app-breakdown">
      						<div className="box">
      							<div className="inside">
      								{this.props.instance.numberOfAppServices} apps
      							</div>
      						</div>
      						<div className="box">
      							<div className="inside">
      								{this.props.instance.numberOfInfraServices} services
      							</div>
      						</div>
      					</div>

      					<div className="progress-bars">
      						<div className="psb-quota">
                    <ProgressBar
                        precent={this.props.instance.quota.psbQuota}
                        instanceName={this.getCleanInstanceName()}/>
      						</div>

      						<Range
      							 dsbQuota={this.props.instance.quota.dsbQuota}
      							 instanceName={this.getCleanInstanceName()}/>

      					</div>

            </div>
        </div>
      )
    }
});

export default Card;
