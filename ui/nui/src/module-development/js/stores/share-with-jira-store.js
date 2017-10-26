// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import JiraMockData from '../data/jiraMockData.js';
import Config from '../../../module-production/js/config.js';
import SharedImageActions from '../actions/dev-shared-image-actions.js';
import SharedStore from '../../../shared-store.js';
import SettingsActions from '../actions/dev-settings-actions';

var ShareWithJiraStore = Reflux.createStore({

  listenables: [SharedImageActions, SettingsActions],

  state: {
    projectId: null,
    issueTypeId: null,
    url: null
  },

  getInitialState(){
    return this.state;
  },

  init(){},

  // onSubmitJiraConfig(url) {
  //
  //   const data = {
  //     url: url,
  //     issueTypeId: this.state.issueType,
  //     projectId: this.state.projectId
  //   }
  //
  //   const options = {
  //     url: `${APISERVER}/hub-web-api/commands/add-jira-integration`,
  //     method: 'POST',
  //     data: data,
  //     contentType: 'application/json'
  //   }
  //
  //   Config.request(options, (response)=>{
  //     console.log('saved successfully');
  //     SettingsActions.hideIntegrationDialog();
  //     SettingsActions.getIntegrations();
  //   }, (error)=>{
  //     console.log(error)
  //   })
  // },

  // onConfigureJiraIssueType(issueType) {
  //   this.state.issueTypeId = issueType;
  //   this.trigger(this.state);
  // },

  // onConfigureJiraProject(project) {
  //   this.state.projectId = project;
  //   this.trigger(this.state);
  // },

  onReceiveDetails (jiraDetails){
    if(jiraDetails){
      SettingsActions.receiveJiraDetails(jiraDetails);
      const connectionDetails = jiraDetails.connectionDetails;
      this.state.url = connectionDetails.url;
      this.state.projectId = connectionDetails.projectId;
      this.state.issueTypeId = connectionDetails.issueTypeId;
      this.trigger(this.state);
    }
  },

  onCreateJiraIssue: function(data){

    let imageSummary = data.description.length ? data.description : 'no summary was provided';
    let descriptionText = `Reproduce the app image using Ocopea:
                          `;
    var projectId = `pid=${data.projectId}`,
        description = `description=${descriptionText}
                       ${encodeURIComponent(data.shareURL)}`,
        summary = `summary=${imageSummary}`,
        issueType = `issuetype=${data.issueTypeId}`,
        components = `components=${this.state.component}`,
        reporter = `reporter=${SharedStore.state.loggedInUser}`,
        jiraUrl = data.url;

    var url = this.addParamsToUrl(jiraUrl, [
      projectId,
      summary,
      description,
      issueType
      // components,
    ]);

    window.open( url, '_blank' );
  },

  addParamsToUrl(jiraUrl, paramsArray){
    var createIssue = '/secure/CreateIssueDetails!init.jspa?',
        url = `${jiraUrl}${createIssue}`;
    paramsArray.forEach((param, i)=>{
      i === paramsArray.length ?
        url += param :
        url += param+'&';
    });
    return url;
  }

});

export default ShareWithJiraStore;
