// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SettingsActions from '../../../../actions/dev-settings-actions.js';


let CustomConfig = React.createClass({
  render() {
    const baseClass = this.props.baseClass;

    return(
      <div className="custom-config">
        <div className={`${baseClass}__title`}>
          <span>{this.props.platformName} configuration</span>
          <span
            className={`icon-close link ${baseClass}__title__dismiss-btn`}
            onClick={this.dismissdialog}></span>
        </div>
      </div>
    )
  },

  dismissdialog() {
    SettingsActions.hideIntegrationDialog();
  }

});

export default CustomConfig;
