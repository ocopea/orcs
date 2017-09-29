// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevActions from '../../actions/dev-actions.js';
import SharedActions from '../../../../shared-actions.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import Config from '../../../../module-production/js/config.js';
import AppTopologyActions from '../../../../module-production/js/actions/appTopologyActions.js';


var DialogGoToDashboard = React.createClass({

  userClickOnGoToDashboard: function(){
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.dashboard.location,
      subLocation: "/"+this.props.selectedAppInstanceId
    });
    DevActions.hideDialogSavedSuccessfuly();
  },

  userClickOnViewApp: function(){
    window.open(
      this.props.instance.webEntryPointURL,
      //Config.getWebEntryPointURL(this.props.instance.webEntryPointURL),,
      '_blank'
    );
  },

  bottomButtonsClassName: function(){
    return "button-primary"+
           " Dialog-saved-succesfuly__buttons__button"+
           " Dialog-saved-succesfuly__buttons__bottom-buttons-container__button"+
           " link";
  },

  userClickOnGoToMainScreen: function(){
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.main.location,
      subLocation: ""
    });
    DevActions.hideDialogSavedSuccessfuly();
  },

  render: function(){
    // console.log(this.props)
    return(
      <div className="Dialog-saved-succesfuly">
        <div className="Dialog-saved-succesfuly__title">
          deployed successfuly
          <span
            className="close-btn"
            onClick={DevActions.hideDialogSavedSuccessfuly}>
            <span></span>
            <span></span>
          </span>
        {/* ./Dialog-saved-succesfuly__title */}
        </div>

        <div className="Dialog-saved-succesfuly__content">
          <div className="Dialog-saved-succesfuly__content__check-container check-container">
            <span className="icon-check Dialog-saved-succesfuly__content__check-container__icon-check"></span>
          </div>
          <p className="Dialog-saved-succesfuly__content__firstline">
            <span
              className="Dialog-saved-succesfuly__content__instance-name"
              title={Config.getTitleOrNull(this.props.instance.name, 10)}>
              {Config.getShortName(this.props.instance.name, 10)}
            </span>
            <span> was deployed successfuly</span>
          </p>
          <p className="Dialog-saved-succesfuly__content__sub-title">
            What would you like to do now ?
          </p>
          <div className="Dialog-saved-succesfuly__buttons">
            <button className={"Dialog-saved-succesfuly__buttons__btn-view-app button-primary " +
                               "Dialog-saved-succesfuly__buttons__button link"}
                                onClick={this.userClickOnViewApp}>
              <span className="icon-link Dialog-saved-succesfuly__buttons__btn-view-app__icon"></span>
              open application
            </button>
            <div>- or go to -</div>
            <div className="Dialog-saved-succesfuly__buttons__bottom-buttons-container">
              <button
                className={this.bottomButtonsClassName()}
                onClick={this.userClickOnGoToDashboard}>dashboard
              </button>
              <button
                className={this.bottomButtonsClassName()}
                onClick={this.userClickOnGoToMainScreen}>main view
              </button>
            </div>
          {/*  ./Dialog-saved-succesfuly__buttons */}
          </div>
        {/* ./Dialog-saved-succesfuly__content */}
        </div>
      {/* ./Dialog-saved-succesfuly */}
      </div>
    )
  }
});

export default DialogGoToDashboard;
