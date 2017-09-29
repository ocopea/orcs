// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevActions from '../actions/dev-actions.js';
import MockIcon from '../../assets/images/wizard/backup.svg';
import EmailIcon from '../../assets/images/saved-images/E_MAIL.png';
import JiraIcon from '../../assets/images/saved-images/Jira.png';
import Trello from '../../assets/images/saved-images/Trello.png';
import PivotalTrackerIcon from '../../assets/images/saved-images/pivotaltracker.png';
import Config from '../../../module-production/js/config.js';
import SharedActions from '../../../shared-actions.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import _ from 'lodash';


var ShareImageDialog = React.createClass({

  userClickOnCopyLink: function(){

      var copyTextarea = document.querySelector('.Share-image-dialog__inside__input-container__input');
      copyTextarea.select();

      try {
        var successful = document.execCommand('copy');
        var msg = successful ? 'successful' : 'unsuccessful';
        //console.log('Copying text command was ' + msg);
      } catch (err) {
        console.log('Oops, unable to copy');
      }

  },

  getIconContainerClassName(isConfigured, modifier) {
    let className  =  "Share-image-dialog__inside__icons-container__icon link " +
                      `Share-image-dialog__inside__icons-container__icon${modifier}`;
    const disabled =  ' disabled';
    if(!isConfigured){
      className += disabled;
    }
    return className;
  },

  getConfigureIcon(isConfigured, platformName) {
    const shareWith = this.props.shareOptions[platformName];
    const click = this.userClickOnShareWith;
    return !isConfigured ?

      <div className="settings-icon-container"
        onClick={click.bind(this, shareWith, isConfigured)}>
        <span className="icon-settings"></span>
      </div>

    : null
  },

  userClickOnShareWith(platformName, isConfigured){
    let shareOptions = this.props.shareOptions;
    console.log(platformName)
    if(isConfigured){
      DevActions.shareImage(shareOptions[platformName], this.props.jiraConfiguration);
    }else{
      DevActions.goToConfigurePlatform(platformName);
    }
  },

  render:function(){
    // console.log(this.props.jiraConfiguration)
    const shareOptions =    this.props.shareOptions;
    let isJiraConfigured    =         !_.isEmpty(this.props.jiraConfiguration);
    let isTrelloConfigured  =         false;
    let isEmailConfigured   =         false;
    let isPivotalTrackerConfigured =  false;

    return(
      <div className="Share-image-dialog">
        <div className="Share-image-dialog__title">
          <span className="Share-image-dialog__title__span">share image</span>
          <span
            className="close-btn"
            onClick={DevActions.hideShareImageDialog}>
              <span></span>
              <span></span>
          </span>
        {/* /Share-image-dialog__title */}
        </div>
        <div className="Share-image-dialog__inside">
          <div className="Share-image-dialog__inside__msg">
            <span className="Share-image-dialog__inside__msg__span">
              how would you like to share your image
            </span>
            <div className="Share-image-dialog__inside__msg__second-line">
              <span className="Share-image-dialog__inside__msg__second-line__image-name"
                title={Config.getTitleOrNull(this.props.image.name, 20)}>
                 " {Config.getShortName(this.props.image.name, 20)} "
              </span>
              <span className="font-weight-medium"> ?</span>
            </div>
          {/* /Share-image-dialog__inside__msg */}
          </div>
          <div className="Share-image-dialog__inside__icons-container">
            <div className={this.getIconContainerClassName(false, '--third')}>
              <img
                src={PivotalTrackerIcon}
                className="Share-image-dialog__inside__icons-container__icon__image"
                onClick={this.userClickOnShareWith.bind(
                  this, shareOptions.pivotalTracker, isPivotalTrackerConfigured)} />
              {this.getConfigureIcon(isPivotalTrackerConfigured, shareOptions.pivotalTracker)}
            {/* share via pivotal tracker */}
            </div>            
            <div className={this.getIconContainerClassName(isJiraConfigured, '--first')}>
              <img
                src={JiraIcon}
                className={"Share-image-dialog__inside__icons-container__icon__image"}
                onClick={this.userClickOnShareWith.bind(this, shareOptions.jira, isJiraConfigured)} />
              {this.getConfigureIcon(isJiraConfigured, shareOptions.jira)}
            {/* share with jira */}
            </div>
            <div className={this.getIconContainerClassName(false, '--second')}>
              <img
                src={Trello}
                className="Share-image-dialog__inside__icons-container__icon__image"
                onClick={this.userClickOnShareWith.bind(this, shareOptions.trello, isTrelloConfigured)} />
              {this.getConfigureIcon(isTrelloConfigured, shareOptions.trello)}
            {/* share with trello */}
            </div>
            <div className={this.getIconContainerClassName(false, '--third')}>
              <img
                src={EmailIcon}
                className="Share-image-dialog__inside__icons-container__icon__image"
                onClick={this.userClickOnShareWith.bind(this, shareOptions.email, isEmailConfigured)} />
              {this.getConfigureIcon(isEmailConfigured, shareOptions.email)}
            {/* share via email */}
            </div>
          {/* /Share-image-dialog__inside__icons-container */}
          </div>
          <div className="Share-image-dialog__inside__input-container">
            <input
              type="text"
              value={this.props.image.shareURL}
              readOnly={true}
              className="Share-image-dialog__inside__input-container__input"/>
            <button
              type="button"
              className="Share-image-dialog__inside__input-container__button link"
              onClick={this.userClickOnCopyLink}>
                copy link
            </button>
          {/* /Share-image-dialog__inside__input-container */}
          </div>
          <button
            className={"Share-image-dialog__inside__button-dismiss "+
                       "button button-secondary"}
            onClick={DevActions.hideShareImageDialog}>
              close
          </button>
        {/* /Share-image-dialog__inside */}
        </div>
      {/* /Share-image-dialog */}
      </div>
    )
  }
});

ShareImageDialog.propTypes = {
  image: React.PropTypes.object.isRequired
}

export default ShareImageDialog;
