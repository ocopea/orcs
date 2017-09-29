// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import JiraConfig from './jira/jira-config.comp.js';
import TrelloConfig from './trello/trello-config.comp.js';
import EmailConfig from './email/email-config.comp.js';
import CustomConfig from './custom/custom.comp.js';
import PivotalTracker from './pivotalTracker/pivotalTracker-config.js';


let ConfigurationDialog = React.createClass({

  getDialog(platformName, baseClass) {
    // console.log(this.props.jiraConfiguration)
    switch (platformName) {
      case this.props.shareOptions.jira:
        return <JiraConfig
                  baseClass={baseClass}
                  platformName={platformName}
                  configuration={this.props.integrations.jira}
                  url={this.props.integrations.jira.url}/>
        break;
      case this.props.shareOptions.trello:
        return <TrelloConfig
                  platformName={platformName}
                  baseClass={baseClass}/>
        break;
      case this.props.shareOptions.email:
        return <EmailConfig
                  platformName={platformName}
                  baseClass={baseClass}/>
        break;
      case this.props.shareOptions.custom:
        return <CustomConfig
                  platformName={platformName}
                  baseClass={baseClass}/>
        break;
      case this.props.shareOptions.pivotalTracker:
        return <PivotalTracker
                  platformName={platformName}
                  baseClass={baseClass}/>
        break;
    }
  },

  render() {
    const baseClass = 'Integrations-config-dialog';
    let platformName = this.props.integrations.platformName;

    return (
      <div className={baseClass}>
        {
          this.getDialog(platformName, baseClass)
        }
      </div>
    )
  }
});

ConfigurationDialog.propTypes = {
  integrations: React.PropTypes.object.isRequired
}

export default ConfigurationDialog;
//configuration={this.props.jiraConfiguration}
