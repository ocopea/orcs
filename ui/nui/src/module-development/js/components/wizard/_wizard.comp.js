// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevWizardActions from '../../actions/dev-wizard-actions.js';
import ProdActions from '../../../../module-production/js/actions/actions.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';

import StatusBar from './wizard.statusBar.comp.js';
import AppMarketComponent from './wizard.appMarket.comp.js';
import ImageComponent from './image/image.comp.js';
import ConfigComponent from './config/wizard.config.comp.js';

import Config from '../../../../module-production/js/config.js'

var GeminiScrollbar = require('react-gemini-scrollbar');


var Wizard = React.createClass({

	userClickOnWizardNext: function(){
		DevWizardActions.userClickOnNext();
	},

	userClickOnWizardBack: function(){
		DevWizardActions.userClickOnBack();
	},

	componentDidMount: function(){
		DevWizardActions.hideConfigTopologyTooltip();

		//default to app market unless navigating from saved images
		if(!this.props.config.fromSavedImages){
			var appMarket = DevNavigationOptions.wizard.subLocation.appMarket;
			DevWizardActions.setCurrentStep(appMarket);
		}

	},

	render: function(){

		var currentSubLocation = Config.getCurrentHash().subLocation;
		return(
				<div>
					<div className="dev-wizard">
						<StatusBar
								currentStep={this.props.currentStep}
								steps={this.props.steps}/>

						<div className="wizard-main">
							{
	                currentSubLocation ==
									DevNavigationOptions.wizard.subLocation.appMarket.substring(1) ?
	                    <AppMarketComponent
													applications={this.props.applications}
													selectedApp={this.props.selectedApp}/>
	                :
	                currentSubLocation ==
									DevNavigationOptions.wizard.subLocation.image.substring(1) ?
	                    <ImageComponent
													image={this.props.image}
													currentUser={this.props.currentUser}
													users={this.props.users}/>
	                :
	                currentSubLocation ==
									DevNavigationOptions.wizard.subLocation.config.substring(1)?
	                    <ConfigComponent
												config={this.props.config}
												selectedImage={this.props.image.selectedImage}
												copyDetails={this.props.image.copyDetails}
												selectedApp={this.props.selectedApp}
												isTooltipRender={this.props.config.tooltip.isRender}
												appTopologyState={this.props.appTopologyState}
												selectedService={this.props.selectedService}
												selectedDependency={this.props.selectedDependency}
												servicesTranslate={this.props.servicesTranslate}
												dependenciesTranslate={this.props.dependenciesTranslate}
												allServices={this.props.allServices}
												simpleTooltip={this.props.simpleTooltip}
												/>
	                :
	                null
	            }

							<div className="footer">
	                <button
										className={this.props.validation ? "wizard-next" : "wizard-next disabled"}
										onClick={this.userClickOnWizardNext}>
											{currentSubLocation ==
											DevNavigationOptions.wizard.subLocation.config.substring(1) ?
												"run" : "next"}
									</button>
	                <button className={this.props.steps.indexOf(this.props.currentStep) == 0 ?
											"wizard-back disabled" : "wizard-back"}
											onClick={this.userClickOnWizardBack}>prev</button>
									<div className="inline-error">{this.props.errorMsg}</div>
							{/* /footer */}
	            </div>
						{/* /wizard-main */}
						</div>
					{/* /dev-wizard */}
					</div>
				{/* /general wrapper */}
				</div>
		)
	}

});

export default Wizard;

//<GeminiScrollbar>
