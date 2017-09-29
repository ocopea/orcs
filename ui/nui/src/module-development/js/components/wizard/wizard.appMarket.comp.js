// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../module-production/js/config.js';
import WizardActions from '../../actions/dev-wizard-actions.js';
import AppTopologyActions from '../../../../module-production/js/actions/appTopologyActions.js';
import $ from 'jquery';
import _ from 'lodash';
import BrowserDetection from '../../../../module-production/js/browserDetection.js';
import CheckIcon from '../../../assets/images/wizard/check.svg';
import Isvg from 'react-inlinesvg';


var DevAppMarket = React.createClass({

	componentDidMount: function(){
		WizardActions.userSelectedImage(null);
		this.userSelectedApplication(null)
	},

	componentDidUpdate: function(nextProps){

		if(!_.isEmpty(this.props.selectedApp)){
				this.setSelectedCardClass();
		}else{
			var cards = $(".dev-wizard .cards-container .card");
			cards.each((index, card)=>{
				$(card).removeClass('selected');
			});
		}
	},

	userSelectedApplication: function(id){
		var application = {};
		if(id !== null){
			var application = this.props.applications[id];
		}
		WizardActions.userSelectedApp(application);
		AppTopologyActions.setSelectedApp(application);
	},

	isFirefox: function(){
		return BrowserDetection.isBrowser(BrowserDetection.browserNames().firefox);
	},

	getNameSpanCssByBrowser: function(){
		if(this.isFirefox()){
			return { "marginTop": "35px" }
		}else{
			return { "marginTop": "25px" }
		}
	},

	imageCssByBrowser: function(){
		if(this.isFirefox()){
			return { "marginTop": "-8px" }
		}else{
			return null
		}
	},

	getCards: function(){
		var cards = _.map(this.props.applications, (app, i)=>{
			return 	<div
									className="card"
									key={i} id={i}
									onClick={this.userSelectedApplication.bind(this, i)}>
								<div className="inside">
									<Isvg src={CheckIcon} className="check-icon"/>
									<div className="card-icon">
										<img style={this.imageCssByBrowser()} src={Config.fixImgUrl(app.img)} />
									</div>
									<span style={this.getNameSpanCssByBrowser()}>{app.name}</span>
								</div>
							</div>
		});
		return cards;
	},

	setSelectedCardClass: function(){
		var selectedCardId = this.props.selectedApp.id;
		var cards = $(".dev-wizard .cards-container .card");
		cards.each((index, card)=>{
				if(card.id == selectedCardId){
					$(card).addClass('selected');
					$(card).siblings().removeClass('selected');
				}
		});
	},

	render: function(){
		return(
			<div className="dev-app-market">
				<div className="title">choose app</div>
				<div className="sub-title">Select an app</div>

				<div className="cards-container">
					{this.getCards()}
				</div>

			</div>
		)
	}
});

export default DevAppMarket;
