// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

// components
import DevActions from '../../../actions/dev-actions.js';
import SettingsActions from '../../../actions/dev-settings-actions.js';

// assets
import JiraIcon from '../../../../assets/images/saved-images/Jira.png';
import EmailIcon from '../../../../assets/images/saved-images/E_MAIL.png';
import TrelloIcon from '../../../../assets/images/saved-images/Trello.png';
import PivotalTrackerIcon from '../../../../assets/images/saved-images/pivotaltracker.png';

import DevNavigationsOptions from '../../../data/devNavigationOptions.js';
import Config from '../../../../../module-production/js/config.js';

import _ from 'lodash';


let IntegrationsConfig = React.createClass({

  componentDidMount() {
    window.addEventListener('hashchange', ()=>{
      let location = {
        integrations: DevNavigationsOptions.settings.subLocation.integrationsConfig.location.substring(1),
        currentSub:   Config.getCurrentHash().subLocation
      }

      if(location.currentSub === location.integrations){
        this.isSelected() ?
          SettingsActions.showIntegrationDialog() :
          SettingsActions.hideIntegrationDialog();
      }else {
        SettingsActions.hideIntegrationDialog()
      }
      SettingsActions.hideJiraCredentialDialog();
    });
  },

  isSelected() {
    return Config.getCurrentHash().additional !== undefined;
  },

  goToConfiguration(platform) {
    DevActions.goToConfigurePlatform(platform);
  },

  render() {

    const platformName = this.props.platformName;
    const baseClass = "Integrations-config";
    
    return(
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>Integrations</div>
        <div className={`${baseClass}__sub-title`}>choose app to config</div>

        <div className={`${baseClass}__integrations`}>

          <div
            onClick={this.goToConfiguration.bind(this, this.props.shareOptions.jira)}
            className={`${baseClass}__integrations__integration`}>

              <div className={`${baseClass}__integrations__integration__icon`}>
                <img src={JiraIcon} className={`${baseClass}__integrations__integration__icon__img ` +
                                               `${baseClass}__integrations__integration__icon__img--jira`}/>
              </div>
              <div className={`${baseClass}__integrations__integration__name`}>Jira</div>
              <div className={`${baseClass}__integrations__integration__description`}>bug tracking system by Attlasian</div>

          {/* / Jira */}
          </div>

          <div
            onClick={this.goToConfiguration.bind(this, this.props.shareOptions.trello)}
            className={`${baseClass}__integrations__integration`}>

              <div className={`${baseClass}__integrations__integration__icon`}>
                <img src={TrelloIcon} className={`${baseClass}__integrations__integration__icon__img`}/>
              </div>
              <div className={`${baseClass}__integrations__integration__name`}>Trello</div>
              <div className={`${baseClass}__integrations__integration__description`}>Online KanBan board</div>

          {/* / Trello */}
          </div>

          <div
            onClick={this.goToConfiguration.bind(this, this.props.shareOptions.email)}
            className={`${baseClass}__integrations__integration`}>

              <div className={`${baseClass}__integrations__integration__icon`}>
                <img src={EmailIcon} className={`${baseClass}__integrations__integration__icon__img`}/>
              </div>
              <div className={`${baseClass}__integrations__integration__name`}>Email</div>
              <div className={`${baseClass}__integrations__integration__description`}>Configure Email settings</div>

          {/* / Email */}
          </div>

          <div
            onClick={this.goToConfiguration.bind(this, this.props.shareOptions.pivotalTracker)}
            className={`${baseClass}__integrations__integration`}>

              <div className={`${baseClass}__integrations__integration__icon`}>
                <img src={PivotalTrackerIcon} className={`${baseClass}__integrations__integration__icon__img`}/>
              </div>
              <div className={`${baseClass}__integrations__integration__name`}>Pivotal Tracker</div>
              <div className={`${baseClass}__integrations__integration__description`}>Configure Pivotal Tracker settings</div>

          {/* / Pivotal Tracker */}
          </div>

          <div
            onClick={this.goToConfiguration.bind(this, this.props.shareOptions.custom)}
            className={`${baseClass}__integrations__integration`}>

              <div className={`${baseClass}__integrations__integration__icon`}>
                <div className="Plus"><span></span><span></span></div>
              </div>
              <div className={`${baseClass}__integrations__integration__name`}>Add Custom</div>
              <div className={`${baseClass}__integrations__integration__description`}>Add Custom</div>

          {/* / Custom */}
          </div>

        </div>

      </div>
    )
  }
});

export default IntegrationsConfig;

// <button onClick={this.goToConfiguration.bind(this, 'jira')}>jira</button>
// <button onClick={this.goToConfiguration.bind(this, 'trello')}>trello</button>
// <button onClick={this.goToConfiguration.bind(this, 'email')}>email</button>
