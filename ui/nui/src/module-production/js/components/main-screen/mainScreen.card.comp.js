// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/actions.js';
import SharedActions from '../../../../shared-actions.js';
import Config from '../../config.js';

//static images
import BtnExpand from '../../../assets/images/main-screen/expand-btn.png';
import MockIMg from '../../../assets/images/cards/hackathon.png';

import NavigationOptions from '../../data/prodNavigationOptions.js';

let MainScreenCard = React.createClass({

    clickOnAppInstanceCard: function(){
  		Actions.setAppInstanceName(this.props.appInstance.name);
      Actions.goToDashboard(this.props.appInstance);
  		Actions.initializeCopyHistorySettings();

  		SharedActions.navigate({
  			module: NavigationOptions.module,
  			location: NavigationOptions.dashboard.location,
        subLocation: this.props.appInstance.id
  		});
    },

    render: function(){

        return(
          <div id="main-screen-card">
            <div className="inside" onClick={this.clickOnAppInstanceCard}>

                <img src={BtnExpand} className="btn-expand"/>

                <div id="icon-container">
                    <img src={this.props.originApp.img} />
                </div>

                <div id="app-name" title={this.props.appInstance.name}>
                    <p>{Config.getShortName(this.props.appInstance.name)}</p>
                </div>

                <div id="app-id">
                    <p>{this.props.appInstance.description}</p>
                </div>

      					<div className="user">
      						<div className="avatar">
      							<img src={APISERVER+"/hub-web-api/user/" +
      							this.props.appInstance.creatorUserId + "/avatar"} />
      						</div>
      						<span>Created By </span>
      						<span>
      							{ this.props.selectedUser.firstName +
      							" " + this.props.selectedUser.lastName }
      						</span>
                {/* /user */}
      					</div>
            {/* /inside */}
            </div>
          {/* /main-screen-card */}
          </div>
        )
    }

});

export default MainScreenCard;
