// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevActions from '../../actions/dev-actions.js';
import SettingsActions from '../../actions/dev-settings-actions.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import IntegrationsConfig from './integrations-config/integrations-config.comp.js';
import Permissions from './permissions/permissions.comp.js';
import Config from '../../../../module-production/js/config.js';


let Settings = React.createClass({

  goToIntegrationsHub() {
    DevActions.goToIntegrationsHub();
  },

  componentDidMount() {
    
  },

  isIntegrationsConfig: function() {
    return  this.props.currentLocation.subLocation ===
              DevNavigationOptions.settings.subLocation.integrationsConfig.location;
  },

  isPermissions() {
    return Config.getCurrentHash().subLocation ===
            DevNavigationOptions.settings.subLocation.permissions.location.substring(1);
  },

  componentDidMount() {
    SettingsActions.getIntegrations();
  },

  render() {

    return (
      <div className="Settings">
        {
          this.isIntegrationsConfig() ?
            <IntegrationsConfig
              shareOptions={this.props.shareOptions}
              platformName={this.props.platformName}
              integrations={this.props.integrations} />
          :
          this.isPermissions() ?
            <Permissions />
          :
          <div className="Settings__title">settings</div>
        }
      </div>
    )
  }
});

export default Settings;
