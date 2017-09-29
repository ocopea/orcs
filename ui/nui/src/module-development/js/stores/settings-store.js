// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Reflux from 'reflux';
import Config from '../../../module-production/js/config.js';
import SettingsActions from '../actions/dev-settings-actions.js';
import SharedActions from '../../../shared-actions.js';
import DevStore from './dev-store.js';
import DevNavigationOptions from '../data/devNavigationOptions.js';
import $ from 'jquery';

var SettingsStore = Reflux.createStore({

  listenables: [SettingsActions],

  getInitialState: function(){
    return this.state;
  },

  state: {
    platforms: DevStore.state.shareImageDialog.shareOptions,
    integrations: {
      jira: {
        projects: {},
        selectedProject: {},
        issueTypes: {},
        selectedIssueType: {},
        issueTypeId: "",
        projectId: "",
        url: "",
        isLoading: false,
        credentialsDialog: {
          isRender: false,
          url: "",
          cleanUrl: "",
          port: 0
        }
      },
      dialog: {
        isRender: false
      },
    }
  },

  init: function(){
    // console.log(this.state.platforms)
  },

  onStartJiraLoader() {
    this.state.integrations.jira.isLoading = true;
    this.trigger(this.state);
  },

  onStopJiraLoading() {
    this.state.integrations.jira.isLoading = false;
    this.trigger(this.state);
  },

  onSubmitJiraConfig(url) {

    const data = {
      url: this.state.integrations.jira.url,
      issueTypeId: this.state.integrations.jira.selectedIssueType.id,
      projectId: this.state.integrations.jira.selectedProject.id
    }

    const options = {
      url: `${APISERVER}/hub-web-api/commands/add-jira-integration`,
      method: 'POST',
      data: data,
      contentType: 'application/json'
    }

    Config.request(options, (response)=>{
      console.log('saved successfully');
      SettingsActions.hideIntegrationDialog();
      SettingsActions.getIntegrations();
    }, (error)=>{
      console.log(error)
    })
  },


  onConfigureJiraIssueType(issueTypeId) {
    this.state.integrations.jira.issueTypeId = issueTypeId;
    this.state.integrations.jira.selectedIssueType = this.state.integrations.jira.issueTypes[issueTypeId];
    this.trigger(this.state);
  },

  onConfigureJiraProject(projectId) {
    this.state.integrations.jira.projectId = projectId;
    this.state.integrations.jira.selectedProject = this.state.integrations.jira.projects[projectId];
    this.trigger(this.state);
  },

  onConfigureJiraUrl(url){
    this.state.integrations.jira.url = url;
    this.trigger(this.state);
  },

  onShowJiraCredentialDialog(url, port, cleanUrl) {
    this.state.integrations.jira.credentialsDialog.url = url;
    this.state.integrations.jira.credentialsDialog.port = port;
    this.state.integrations.jira.credentialsDialog.cleanUrl = cleanUrl;
    this.state.integrations.jira.credentialsDialog.isRender = true;
    this.trigger(this.state);
  },

  onHideJiraCredentialDialog() {
    this.state.integrations.jira.credentialsDialog.isRender = false;
    this.trigger(this.state);
  },

  onReceiveJiraProjectsAndIssueTypes(projects, issueTypes) {
    if(projects){
      projects.forEach(project=>{
        this.state.integrations.jira.projects[project.id] = project;
      });
    }
    if(issueTypes){
      issueTypes.forEach(issueType=>{
        this.state.integrations.jira.issueTypes[issueType.id] = issueType;
      });
    }
    this.trigger(this.state);
    this.populateSelectedProject();
    this.populateSelectedIssueType();
  },

  populateSelectedProject(){
    const allProjects = this.state.integrations.jira.projects;
    const projectId = this.state.integrations.jira.projectId;
    this.state.integrations.jira.selectedProject = allProjects[projectId];
    this.trigger(this.state);
  },

  populateSelectedIssueType(){
    const allIssueTypes = this.state.integrations.jira.issueTypes;
    const issueTypeId = this.state.integrations.jira.issueTypeId;
    this.state.integrations.jira.selectedIssueType = allIssueTypes[issueTypeId];
    this.trigger(this.state);
  },

  onReceiveJiraDetails(jiraDetails) {
    if(jiraDetails){
      const details = jiraDetails.connectionDetails;
      this.state.integrations.jira.projectId =    details.projectId;
      this.state.integrations.jira.issueTypeId =  details.issueTypeId;
      this.state.integrations.jira.url =          details.url;

      this.trigger(this.state);
    }
  },

  onShowIntegrationDialog(platform) {
    if(platform){
      this.state.integrations.platformName = platform;
    }
    this.state.integrations.dialog.isRender = true;
    this.trigger(this.state);
  },

  onHideIntegrationDialog() {
    this.state.integrations.dialog.isRender = false;
    this.trigger(this.state);
  },

  onLeftMenuNavigation(navigationString) {
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.settings.location,
      subLocation: `/${navigationString}`
    })
  },

  onReceiveData(data, platform) {
    switch (platform) {
      case this.state.platforms.jira:
        this.state.integrations.jira.url = data.connectionDetails.url;
        this.trigger(this.state);
        break;
    }
  },

  onClearJiraData() {
    this.state.integrations.jira.projects = {};
    this.state.integrations.jira.selectedProject = {};
    this.state.integrations.jira.issueTypes = {};
    this.state.integrations.jira.selectedIssueType = {};
    this.trigger(this.state);
  }

});

export default SettingsStore;
