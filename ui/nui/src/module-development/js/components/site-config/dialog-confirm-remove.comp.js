// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../actions/site-config-actions.js';


let DialogConfigRemoveArtifact = React.createClass({
  render() {
    const baseClass = "Dialog-confirm-remove";
    return(
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>
          <span>confirm delete</span>
          <span
            className={`icon-close ${baseClass}__title__icon-close link`}
            onClick={this.closeDialog}></span>
        </div>
        <div className={`${baseClass}__inside`}>
          <div className={`${baseClass}__inside__content`}>are you sure you want to remove {this.props.registryName} ?</div>
          <div className={`${baseClass}__inside__buttons-container`}>
            <button
              onClick={this.confirm}
              type="button"
              className={`${baseClass}__inside__buttons-container__button button button-primary`}>confirm</button>
            <button
              onClick={this.closeDialog}
              type="button"
              className={`${baseClass}__inside__buttons-container__button button button-secondary`}>cancel</button>
          </div>
        </div>
      </div>
    )
  },

  confirm() {
    SiteConfigActions.removeMavenRegistry(this.props.registryName);
    SiteConfigActions.hideCofirmRemoveDialog();
  },

  closeDialog() {
    SiteConfigActions.hideCofirmRemoveDialog();
  }
});

export default DialogConfigRemoveArtifact;
