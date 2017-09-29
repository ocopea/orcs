// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';
import SharedActions from '../../../../shared-actions.js';
import DevActions from '../../../../module-development/js/actions/dev-actions.js';
import d3 from 'd3';
import ProdNavigationOptions from '../../data/prodNavigationOptions.js';
import DevNavigationOptions from '../../../../module-development/js/data/devNavigationOptions.js';
import Config from '../../../../module-production/js/config.js';

var options = {
	userSettings: "user-settings",
	recentActivities: "recent-activies",
	summaryReports: "summary-reports",
	savedImages: "saved-images",
	modes: "modes",
	settings: "settings",
	siteConfig: "site-config"
}

var SettingsTooltip = React.createClass({

	componentDidMount: function(){
		Actions.closeSettingsTooltipModes();
	},

	userChangeMode: function(module){

		SharedActions.setModule(module);

		switch(module){
			case ProdNavigationOptions.module:
				SharedActions.navigate({
					module: ProdNavigationOptions.module,
					location: ProdNavigationOptions.main.location,
					subLocation: ""
				});
				break;
			case DevNavigationOptions.module:
				SharedActions.navigate({
					module: DevNavigationOptions.module,
					location: DevNavigationOptions.main.location,
					subLocation: ""
				});

				break;
		}
	  DevActions.getAppInstances();
	},

	userClickOnModes: function(){
		Actions.toggleSettingsTooltipModes();
	},

    getModeTdClass: function(module){
        var className;
		//console.log(this.props.module)
        if(module == this.props.module){
            className = "selected s-menu";
        }else{
					className = "s-menu";
				}
       return className;
    },

	populateModes: function(){

		var modes = this.props.data.modes.options.map((option, i)=>{
			return (
				<tr
            key={i}
            onClick={this.userChangeMode.bind(this, option.name)}
            className={this.getModeTdClass(option.name)}>
                <td></td>
                <td>{option.name}</td>
				</tr>
			)
		});
		return modes;
	},

	userClickOnTr: function(e){
		switch(e){
			case options.userSettings:

				break;
			case options.recentActivities:

				break;
			case options.summaryReports:

				break;
			case options.modes:

				break;
			case options.siteconfig:
				SharedActions.navigate({
				 module: DevNavigationOptions.module,
				 location: DevNavigationOptions.siteConfig.location,
				 subLocation: ""
			 	});
				Actions.closeSettingsTooltip();
				break;
			case options.settings:
				DevActions.goToSettings();
				Actions.closeSettingsTooltip();
				break;
			case options.savedImages:
				if(Config.getCurrentHash().location !==
					 DevNavigationOptions.savedImages.location.substring(1)){
						SharedActions.navigate({
						 module: DevNavigationOptions.module,
						 location: DevNavigationOptions.savedImages.location,
						 subLocation: ""
					 });
					 Actions.closeSettingsTooltip();
				}
				break;
		}
	},

	render: function(){

    return(

			<div className="settings-tooltip s-menu">
				<table className="s-menu">
					<tbody>
						<tr className="user-settings s-menu"
								onClick={this.userClickOnTr.bind(this, options.userSettings)}>
							<td className="s-menu">
								<span className="icon-user-settings s-menu"></span>
							</td>
							<td className="s-menu">user settings</td>
						</tr>
						<tr className="s-menu"
							 	onClick={this.userClickOnTr.bind(this, options.recentActivities)}>
							<td className="s-menu">
								<span className="icon-activities s-menu"></span>
							</td>
							<td className="s-menu">recent activities</td>
						</tr>
						<tr className="summary-reports s-menu"
								onClick={this.userClickOnTr.bind(this, options.summaryReports)}>
							<td className="s-menu">
								<span className="icon-reports s-menu"></span>
							</td>
							<td className="s-menu">summary reports</td>
						</tr>
						<tr className="saved-images"
								onClick={this.userClickOnTr.bind(this, options.savedImages)}>
							<td>
								<span className="icon-save"></span>
							</td>
							<td className="s-menu">saved images</td>
						</tr>
						<tr className="settings s-menu"
								onClick={this.userClickOnTr.bind(this, options.settings)}>
							<td>
								<span className="icon-settings"></span>
							</td>
							<td className="s-menu">settings</td>
						</tr>
						<tr className="site-config s-menu"
								onClick={this.userClickOnTr.bind(this, options.siteconfig)}>
							<td>
								<span className="icon-settings"></span>
							</td>
							<td className="s-menu">site config</td>
						</tr>
					</tbody>
				</table>
			</div>

		)

	}

});

export default SettingsTooltip;
