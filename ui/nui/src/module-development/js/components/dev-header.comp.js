// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ProdActions from '../../../module-production/js/actions/actions.js';
import DevActions from '../actions/dev-actions.js';
import SettingsTooltip from '../../../module-production/js/components/tooltips/tt.settings.comp.js';
import Hamburger from '../../../module-production/js/components/menuLeftBtn.comp.js';
import GeneralMenu from '../../../shared-components/js/general-menu.comp.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import SharedActions from '../../../shared-actions.js';
import Config from '../../../module-production/js/config.js';
import $ from 'jquery';


var DevHeader = React.createClass({

	componentDidMount: function(){

    var that = this;
    $(document).click(function(e){

			//hide user menu on document click
      if(that.props.isUserMenuRender){
        if(!e.target.classList.contains('user-menu')){
            DevActions.closeUserMenu();
        }
      }

			//close settings tooltip on document click
			if(that.props.settingsTooltip.isRender){
				$(document).click(function(e){
					if(!e.target.parentElement.parentElement.classList.contains('s-menu')){
						if(!e.target.parentElement.classList.contains('s-menu') ||
							 !e.target.classList.contains('s-menu')){
								 ProdActions.closeSettingsTooltip();
					  }
					}
				});
			}

    });

  },

	isMaximized: function(){
    var currentLocation = this.props.currentLocation.location;
    return currentLocation !== DevNavigationOptions.wizard.location &&
           currentLocation !== DevNavigationOptions.dashboard.location &&
           currentLocation !== DevNavigationOptions.savedImages.location &&
					 currentLocation !== DevNavigationOptions.sharedImage.location &&
					 currentLocation !== DevNavigationOptions.deployingProgress.location &&
					 currentLocation !== DevNavigationOptions.settings.location &&
					 currentLocation !== DevNavigationOptions.siteConfig.location &&
           this.props.leftMenu.isRender;
  },

  getHeaderClass: function(){
      return this.isMaximized() ? "header" : "header maximized"
  },

	userClickOnAvatar: function(){
		this.goToMainScreen();
	},

	goToMainScreen: function(){
		var module = Config.getCurrentHash().module,
				location = Config.getCurrentHash().location;

		if(module == DevNavigationOptions.module &&
			 location != DevNavigationOptions.main.location.substring(1)){

				 SharedActions.navigate({
		 			module: DevNavigationOptions.module,
		 			location: DevNavigationOptions.main.location,
		 			subLocation: ""
		 		})
	 	}
	},

	isDashboard: function(){
		return this.props.currentLocation.module === DevNavigationOptions.module &&
					 this.props.currentLocation.location === DevNavigationOptions.dashboard.location;
	},

	userClickOnOpenApp: function(){
		window.open(
			this.props.selectedInstance.webEntryPointURL,
			//Config.getWebEntryPointURL(this.props.selectedInstance.name),
			'_blank'
		);
	},

	isHamburgerRender: function(){
		return Config.getCurrentHash().module === DevNavigationOptions.module &&
					 Config.getCurrentHash().location !== DevNavigationOptions.sharedImage.location.substring(1) &&
					 Config.getCurrentHash().location !== DevNavigationOptions.savedImages.location.substring(1) &&
					 Config.getCurrentHash().location !== DevNavigationOptions.deployingProgress.location.substring(1) &&
					 Config.getCurrentHash().location !== DevNavigationOptions.dashboard.location.substring(1) &&
					 Config.getCurrentHash().location !== DevNavigationOptions.wizard.location.substring(1) &&
					 Config.getCurrentHash().location !== DevNavigationOptions.settings.location.substring(1) &&
					 Config.getCurrentHash().location !== DevNavigationOptions.siteConfig.location.substring(1);
	},

	isSettings(){
		return Config.getCurrentHash().location === DevNavigationOptions.settings.location.substring(1);
	},

	render: function(){
		// console.log(DevNavigationOptions.settings.location.substring(1))
		return(

			<div className={this.getHeaderClass()}>

				<div className="header-inside">

          <div
						className="title"
						style={!this.isHamburgerRender() ? {width: 90} : null}>
						{
							this.isHamburgerRender() ?
								<Hamburger
	                  isRender={this.props.leftMenu.isRender}/>
							:
							null
						}

            <div className="content">
                <span
									className="link"
									onClick={this.goToMainScreen}
									style={!this.isHamburgerRender() ? {paddingLeft: 28} : null}>
										OCOPEA
								</span>
            </div>

            <div className="triangle">
                <div className="inner-triangle"></div>
            </div>
					{/* /title */}
          </div>

          <span
						style={!this.isHamburgerRender() ? {left:120} : null}
						className="subtitle">
							{this.isSettings() ? 'settings' : 'test / dev instances'}
					</span>

          <div className="user-area">

              <div className="icons">
									<span className="icon-alerts dev-header-icon"></span>

                  <div className="settings s-menu">

                        <span
                            id='settings-img'
                            title="settings"
                            className="icon-settings s-menu link dev-header-icon"
                            onClick={ProdActions.toogleSettingsTooltip}/>
                        {
                            this.props.settingsTooltip.isRender ?
                                <SettingsTooltip
                                    data={this.props.settingsTooltip}
                                    module={this.props.module}/>
                            :
                            null
                        }
												{
													this.isDashboard() ?
														<span
															className="icon-link dev-header-icon link"
															onClick={this.userClickOnOpenApp}></span>
													:
													null
												}

									{/* /icons */}
                  </div>
							{/* /user area */}
              </div>

              <div className="username-container">
									<div className="open-user-menu" onClick={DevActions.toggleUserMenu}></div>
									<img
                      src={APISERVER+"/hub-web-api/user/" +
                      this.props.currentUser.id + "/avatar"}
                      className="avatar"
											onClick={this.userClickOnAvatar}/>

									<span className="firstName">
										{this.props.currentUser.firstName+" "}
									</span>

									<span className="lastName">
										{this.props.currentUser.lastName}
									</span>

                    {!this.props.isUserMenuRender ?
                        <div
													className="open-button link u-menu"
													onClick={DevActions.toggleUserMenu}>
                            <span></span>
                            <span></span>
                        </div>
                        :
                        <div
													className="close-button link u-menu"
													onClick={DevActions.toggleUserMenu}>
                            <span></span>
                            <span></span>
                        </div>
	                    }
								{/* /username-container */}
                </div>
						{/* /header-inside */}
            </div>
				{/* /header */}
				</div>

				{
					this.props.isUserMenuRender ?
						<GeneralMenu
							options={
								[
									{text: 'logout', onClick: ProdActions.logout}
								]
							}
							className="user-menu"
						/>
					:
					null
				}
			</div>

		)

	}

});

export default DevHeader;
