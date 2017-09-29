// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import UserImg from '../../assets/images/header/user.png';
import NotificationsImg from '../../assets/images/header/notifications.png';
import SettingsImg from '../../assets/images/header/settings.png';
import Actions from '../actions/actions.js';
import SharedActions from '../../../shared-actions.js';
import NavigationOptions from '../data/prodNavigationOptions.js';


let UserArea = React.createClass({

   clickOnUserImg: function(){
       if(!this.props.isErrorDialogShown){
           if(!this.props.isLoading){
                Actions.goToMainScreen();
                Actions.setAppInstanceNameFromDialog("");
                Actions.setAppInstance({});
        				SharedActions.navigate({
        					module: NavigationOptions.module,
        					location: NavigationOptions.main.location,
        					subLocation: ""
        				})
           }
       }
   },

	userClickOnLogout: function(){
		Actions.logout();
	},

	render: function(){

       return(
                <div>
                {

					<ul id="user-area">

						<li className="username-container">
								{
									this.props.currentUser.id != undefined ?

										<img
											src={APISERVER+"/hub-web-api/user/" +
											this.props.currentUser.id + "/avatar"}
											onClick={this.clickOnUserImg}
											className="avatar"
											title={this.props.navigation != 'main' ?
												"go to main screen" : null}/>

									:

									<img src={UserImg} id='user-default-img'
										onClick={this.clickOnUserImg}
										className="avatar"
										title={this.props.navigation != 'main' ?
										"go to main screen" : null}/>
								}

							<span className="firstName">
								{this.props.currentUser.firstName+" "}
							</span>

							<span className="lastName">
								{this.props.currentUser.lastName}
							</span>

						</li>

						<li className="notifications">

							<img src={NotificationsImg}
								id='notifications-img'
								title="notifications"
								className="link"/>

							<div className="notifications-circle"><span>4</span></div>
						</li>

						<li className="settings s-menu">
							<img src={SettingsImg}
								id='settings-img'
								title="settings"
								className="link s-menu"
		   						onClick={Actions.toogleSettingsTooltip}/>
						</li>

						<li className="logout" onClick={this.userClickOnLogout}>
							logout
						</li>



					</ul>

                }
                </div>
       )
   }
});

export default UserArea;
