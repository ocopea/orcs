// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../module-production/js/config.js'
import DevNavigationOptions from '../data/devNavigationOptions.js';
import SettingsActions from '../actions/dev-settings-actions.js';


let SettingsLeftMenu = React.createClass({

  getClassName(currentLocation) {
    const baseClass = "left-menu__settings";
    if(Config.getCurrentHash().subLocation === currentLocation){
      return `${baseClass}__item ${baseClass}__item--selected`;
    }else{
      return `${baseClass}__item`;
    }
  },

  click(navigationString) {
    SettingsActions.leftMenuNavigation(navigationString)
  },

  render() {
    const locations = {
      integrations: DevNavigationOptions.settings.subLocation.integrationsConfig.location.substring(1),
      permissions: DevNavigationOptions.settings.subLocation.permissions.location.substring(1)
    }
    const baseClass = "left-menu__settings";
    return(
      <ul className={baseClass}>
        <li
          onClick={this.click.bind(this, locations.integrations)}
          className={this.getClassName(locations.integrations)}>connected apps</li>
        <li
          onClick={this.click.bind(this, locations.permissions)}
          className={this.getClassName(locations.permissions)}>peremissions</li>
      </ul>
    )
  }
});

export default SettingsLeftMenu;
