// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SettingsActions from '../../../../actions/dev-settings-actions.js';
import $ from 'jquery';
import Loader from '../../../../../assets/loader.gif';
import Config from '../../../../../../module-production/js/config.js';


let JiraCredentialsDialog = React.createClass({
  render() {
    const baseClass = "Jira-credentials-dialog";
    return(
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>Jira Credentials</div>
        <div className={`${baseClass}__inside`}>
          <section>
            <div className={`${baseClass}__inside__label`}>username</div>
            <input
              onChange={this.removeValidationError}
              type="text"
              className={`${baseClass}__inside__input input username`}
              ref="jiraUsername" />
          </section>
          <section>
            <div className={`${baseClass}__inside__label`}>password</div>
            <input
              onChange={this.removeValidationError}
              type="password"
              className={`${baseClass}__inside__input input`}
              ref="jiraPassword" />
          </section>
          <div className={`${baseClass}__inside__inline-error`}>{this.state.error}</div>
          {
            this.props.isLoading ?
              <div className={`${baseClass}__inside__loader-container`}>
                <img src={Loader} className={'loader'}/ >
              </div>
            : null
          }

          <div className={`${baseClass}__inside__buttons-container`}>
            <button
              onClick={this.submitCredentials}
              className={`${baseClass}__inside__buttons-container__button `+
                         `button button-primary submitBtn`}
              type="button">submit</button>

            <button
              onClick={this.dismiss}
              className={`${baseClass}__inside__buttons-container__button `+
                         `button button-secondary dismiss`}
              type="button">cancel</button>
          </div>
        </div>
      </div>
    )
  },

  getInitialState() {
    return {
      error: ''
    }
  },

  componentDidMount() {
    console.log(this.props.cleanUrl)
    $('.input.username').focus();
    window.addEventListener('keydown', this.enterClicked, false)
  },

  enterClicked(e) {
    if(e.keyCode === 13){
      this.submitCredentials();
    }
  },

  getValidationError() {
    if(this.isUsernameEmpty() && this.isPasswordEmpty()){
      error = 'no username and password entered';
    }else{
      if(this.isUsernameEmpty()){
        error = 'no username entered';
      }else{
        error = 'no password entered'
      }
    }
    this.setState({
      error: error
    });
  },

  removeValidationError() {
    this.setState({
      error: ''
    })
  },

  isValid() {
    return !this.isUsernameEmpty() && !this.isPasswordEmpty();
  },

  isUsernameEmpty() {
    return this.refs.jiraUsername.value.length === 0;
  },

  isPasswordEmpty() {
    return this.refs.jiraPassword.value.length === 0;
  },

  componentWillUnmount() {
    SettingsActions.stopJiraLoading();
    window.removeEventListener('keydown', this.enterClicked, false);
  },

  submitCredentials() {

    if(this.isValid()){
      this.submit();
    }else{
      this.getValidationError()
    }

  },

  submit() {
    const url = this.props.url;
    const port = this.props.port;
    const username = this.refs.jiraUsername.value;
    const password = this.refs.jiraPassword.value;

    $(".submitBtn").attr('style', 'opacity:0.5; pointer-events:none')

    SettingsActions.startJiraLoader();

    this.nazgulApi(url, username, password);
    // this.clientApi(this.props.cleanUrl, port, username, password);
  },

  nazgulApi(url, username, password) {
    const data = {
      "jiraUrl": url,
      "username": username,
      "password": password
    };

    const options = {
      url: `${APISERVER}/hub-web-api/test-dev/jira-query`,
      method: 'POST',
      data: data,
      contentType: 'application/json'
    }

    Config.request(options, response=>{
      console.log(response)
      SettingsActions.receiveJiraProjectsAndIssueTypes(response.projects, response.issueTypes);
      SettingsActions.hideJiraCredentialDialog();
      $(".submitBtn").removeAttr('style');
      SettingsActions.stopJiraLoading();
    }, error=>{console.log(error)})

  },

  clientApi(url, port, username, password) {
    $.ajax({
      url: `../../../client-api/jira`,
      method: 'GET',
      data: {
        url: url,
        port: port,
        username: username,
        password: password
      },
      success: response=>{
        if(response.error){
          this.setState({
            error: response.error
          });
          SettingsActions.clearJiraData();
        }else{
          SettingsActions.receiveJiraProjectsAndIssueTypes(response.projects, response.issueTypes);
          SettingsActions.hideJiraCredentialDialog();
          this.setState({
            error: ''
          });
        }

        $(".submitBtn").removeAttr('style');
        SettingsActions.stopJiraLoading();

      },
      error: error=>{
        SettingsActions.stopJiraLoading();
        console.log(error)
      }
    });

  },

  dismiss() {
    SettingsActions.hideJiraCredentialDialog();
  },
});

export default JiraCredentialsDialog;
