// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

import EmcLogo from '../../assets/images/header/emc-logo.png';
import MenuLeftBtn from './menuLeftBtn.comp.js';
import UserArea from './header.userArea.comp.js';
import MenuRightBtn from './menuRightBtn.comp.js';
import SettingsTooltip from './tooltips/tt.settings.comp.js';
import BreadCrumbs from './breadCrumbs.js';

import Actions from '../actions/actions.js';
import SharedActions from '../../../shared-actions.js';

import NavigationOptions from '../data/prodNavigationOptions.js';
import $ from 'jquery';


let Header = React.createClass({

	userClickOnLogo: function(){
		if(this.props.navigation != 'main'){
			Actions.goToMainScreen();
			Actions.setAppInstanceNameFromDialog("");
			Actions.setAppInstance({});
			SharedActions.navigate({
				module: NavigationOptions.module,
				location: NavigationOptions.main.location,
				subLocation: ""
			})
		}
	},

	render: function() {

		//close settings tooltip on document click
		if(this.props.settingsTooltip.isRender){
				$(document).click(function(e){
					if(!e.target.parentElement.parentElement.classList.contains('s-menu')){
						if(!e.target.parentElement.classList.contains('s-menu') ||
							 !e.target.classList.contains('s-menu')){
								 Actions.closeSettingsTooltip();
					  }
					}
				});
		}

		return (

            <header>

                  <MenuLeftBtn
                        isRender={this.props.leftMenu.isRender}/>

                  <MenuRightBtn
                        selectedApp={this.props.selectedApp}
                        isRightMenu={this.props.rightMenu.isRender}
                        navigation={this.props.navigation}
                        dashboardAppInstance={this.props.dashboardAppInstance}/>

                  <div className="header-inside">

                    <div className="logo-container">
                        <img src={EmcLogo} id="emc-logo"
							                 onClick={this.userClickOnLogo}
                               style={this.props.navigation == "main" ? {border:"none"} : null}
	                             title="Go To Main Screen"/>
                    </div>

                    <BreadCrumbs
                        selectedApp={this.props.selectedApp}
		                    selectedAppInstance={this.props.selectedAppInstance}
                        currentStep={this.props.currentStep}
		                    navigation={this.props.navigation}/>

                    <UserArea
                        isErrorDialogShown={this.props.isErrorDialogShown}
                        isLoading={this.props.isLoading}
                        navigation={this.props.navigation}
												currentUser={this.props.currentUser}
                        isLogged={this.props.isLogged}/>

					{

						this.props.settingsTooltip.isRender ?
							<SettingsTooltip
                  data={this.props.settingsTooltip}
                  module={this.props.module}/>
								:
								null

					}

        </div>

      </header>
		)
	}
});

export default Header;
