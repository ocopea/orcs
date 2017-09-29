// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import NazSelect from '../../../shared-image/shared-image-select.comp.js';
import Config from '../../../../../../module-production/js/config.js';
import SettingsActions from '../../../../actions/dev-settings-actions.js';
import SharedActions from '../../../../../../shared-actions.js';
import DevNavigationOptions from '../../../../data/devNavigationOptions.js';
import $ from 'jquery';
import _ from 'lodash';


let JiraConfig = React.createClass({

  getDisabledClassName(empty, className) {
    return empty ? className + ' disabled' : className;
  },

  render() {

    const baseClass = this.props.baseClass;
    const connectionDetails = this.props.connectionDetails;
    let selectedIssueType = this.props.configuration.selectedIssueType;
    let selectedProject = this.props.configuration.selectedProject;

    const issueTypesClassName = `${baseClass}__inside__section `+
                                `${baseClass}__inside__section--issueType`;

    const projectsClassName =   `${baseClass}__inside__section `+
                                `${baseClass}__inside__section--project`;

    return(
      <div className={`${baseClass} ${baseClass}__jira-config`}>
        <div className={`${baseClass}__title`}>
          <span>{this.props.platformName} configuration</span>
          <span
            className={`icon-close link ${baseClass}__title__dismiss-btn`}
            onClick={this.dismissdialog}></span>
        </div>
        <div className={`${baseClass}__inside`}>
          <section
            className={`${baseClass}__inside__section `+
                       `${baseClass}__inside__section--url`}>
              <label className={`${baseClass}__inside__section__label`}>url</label>
              <div className="flex">
                <input
                  type='text'
                  defaultValue={this.props.url}
                  ref="jiraUrl"
                  value={this.state.url}
                  onChange={this.userChangedUrl}
                  className={`input url ${baseClass}__inside__section__input--url ` +
                             `${baseClass}__inside__section__input`} />
                <button
                  type="button"
                  onClick={this.submitUrl}
                  className={'button button-primary ' +
                             `${baseClass}__inside__section--url__submit-btn`}>fetch</button>
              </div>
          {/* ${baseClass}__inside__section--url */}
          </section>
          <section className={this.getDisabledClassName(_.isEmpty(selectedProject), projectsClassName)}>
            <label className={`${baseClass}__inside__section__label`}>project</label>
            <NazSelect
              type="jira-config-project"
              className={`${baseClass}__inside__section--project__select`}
              options={this.getProjectsOptions()}/>
          {/* ${baseClass}__inside__section--project */}
          </section>
          <section className={this.getDisabledClassName(_.isEmpty(selectedIssueType), issueTypesClassName)}>
            <label className={`${baseClass}__inside__section__label`}>issue type</label>
            <NazSelect
              type="jira-config-issueType"
              className={`${baseClass}__inside__section--issueType__select`}
              options={this.getIssueTypesOptions()}/>
          {/* ${baseClass}__inside__section--issueType */}
          </section>
          <div className={`${baseClass}__inside__footer`}>
            <button
              className={`${baseClass}__inside__footer__button button button-primary`}
              type="button"
              onClick={this.userClickOnSave}>save</button>
            <button
              className={`${baseClass}__inside__footer__button button button-secondary`}
              type="button"
              onClick={this.dismissdialog}>cancel</button>
          {/* ${baseClass}__inside__footer */}
          </div>
        </div>
      </div>
    )
  },

  submitUrl() {

    const url = this.refs.jiraUrl.value;
    if(this.isUrlValid(url)){
      // SettingsActions.showJiraCredentialDialog(this.getCleanUrl(url), this.getPort(url));
      SettingsActions.showJiraCredentialDialog(url, this.getPort(url), this.getCleanUrl(url));
    }

  },

  isUrlValid(url) {
    const hasHttp = this.validationHasHttp(url);
    const hasPort = this.validationHasPort(url);
    return hasHttp && hasPort;
  },

  getPort(url) {
    return url.substring(url.lastIndexOf(':')+1);
  },

  getCleanUrl(url) {
    const isHttps = url.indexOf('https');
    indexOfHost = isHttps === -1 ? 7 : 8;
    return url.substring(indexOfHost, url.indexOf(this.getPort(url))-1);
  },

  validationHasPort(url) {
    return !isNaN(Number(this.getPort(url))) && Number(this.getPort(url)) > 0;
  },

  validationHasHttp(url) {
    return url.indexOf('http') > -1;
  },

  getProjectsOptions() {

    const selectedProject = this.props.configuration.selectedProject;
    const selectedProjectId = !_.isEmpty(selectedProject) ? this.props.configuration.selectedProject.id : '';
    const projects = this.props.configuration.projects;

    let sortedProjects = _.sortBy(projects, project=>{
      return project.id !== selectedProjectId;
    });

    return this.parseOptions(sortedProjects);
  },

  getIssueTypesOptions() {

    const selectedIssueType = this.props.configuration.selectedIssueType;
    const selectedIssueTypeId = !_.isEmpty(selectedIssueType) ? this.props.configuration.selectedIssueType.id : '';
    const issueTypes = this.props.configuration.issueTypes;

    let sortedProjects = _.sortBy(issueTypes, issueType=>{
      return issueType.id !== selectedIssueTypeId;
    });

    return this.parseOptions(sortedProjects);
  },

  parseOptions(options) {
    return _.map(options, o=>{
      let name = Config.getShortName(o.name, 25);
      return { value: name, label: name, id: o.id }
    })
  },

  userClickOnSave() {
    SettingsActions.submitJiraConfig(this.state.url)
  },

  getInitialState() {
    return {
      url: null
    }
  },

  componentDidMount() {
    SettingsActions.getIntegrations();
    $('.input.url').focus();
  },

  userChangedUrl(e) {
    SettingsActions.configureJiraUrl(e.target.value);
    this.setState({
      url: e.target.value
    });
  },

  componentDidUpdate(nextProps) {
    if(this.props.url !== nextProps.url){

      let issueType, project;
      const issueTypeSelect = $(`.${this.props.baseClass}__inside__section--issueType__select .Select-placeholder`);
      const projectsSelect = $(`.${this.props.baseClass}__inside__section--project__select .Select-placeholder`);

      if(issueTypeSelect[0]){
        issueType = $(issueTypeSelect)[0].textContent;
      }else{
        issueType = '';
      }

      if(projectsSelect[0]){
        project = projectsSelect[0].textContent;
      }else{
        project = '';
      }

      this.setState({
        url: this.props.url,
        issueType: issueType,
        project: project
      });

    }
  },

  dismissdialog() {
    SettingsActions.hideIntegrationDialog();
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.settings.location,
      subLocation: DevNavigationOptions.settings.subLocation.integrationsConfig.location
    })
  }

});

export default JiraConfig;
