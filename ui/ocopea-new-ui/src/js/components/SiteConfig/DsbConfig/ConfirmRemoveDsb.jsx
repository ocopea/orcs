// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../../actions/site-config-actions.js';


const ConfirmRemoveDsb = React.createClass({
  render() {
    
    const baseClass = "Confirm-remove-dsb-dialog";

    return (
      <div className={baseClass}>
        {/* title */}
        <div className={`${baseClass}__title`}>
          <span>confirm delete</span>
          {/* close dialog btn */}
          <span
            onClick={this.onDismiss}
            className={`${baseClass}__title__icon-close icon-close link`}>
              <span></span>
              <span></span>
          </span>
        </div>
        <div className={`${baseClass}__inside`}>
          are you sure you want to delete {this.props.selectedDsb.name} ?
        </div>

        <div className={`${baseClass}__inside__footer`}>
          <button
            onClick={this.onDismiss}
            className={`${baseClass}__inside__footer__button button button-primary`}>cancel</button>
          <button
            onClick={this.onConfirm.bind(this, this.props.selectedDsb)}
            className={`${baseClass}__inside__footer__button button button-secondary`}>delete</button>

        </div>

      </div>
    )
  },

  onDismiss() {
    SiteConfigActions.showConfirmRemoveDsbDialog(false);
  },

  onConfirm(dsb) {
    SiteConfigActions.deleteDsb(dsb)
  }
});

export default ConfirmRemoveDsb;
