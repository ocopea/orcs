// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevDashboardAction from '../../actions/dev-dashboard-actions.js';
import Config from '../../../../module-production/js/config.js';


var ConfirmDisposeImageDialog = React.createClass({
  render: function(){
    return(
      <div className="Dispose-image-dialog">
        <div className="Dispose-image-dialog__title">
          <span className="Dispose-image-dialog__title__label">dispose instance</span>
          <span
            className="close-btn"
            onClick={DevDashboardAction.hideConfirmDisposeImageDialog}>
              <span></span>
              <span></span>
          </span>
        {/* /Dispose-image-dialog__title */}
        </div>
        <div className="Dispose-image-dialog__inside">
          <div className="Dispose-image-dialog__inside__label">
            <span title={Config.getTitleOrNull(this.props.instanceName, 20)}>
              are you sure you want to dispose {Config.getShortName(this.props.instanceName, 20)}</span>
          </div>
          <div className="Dispose-image-dialog__inside__buttons-container">
            <div className="Dispose-image-dialog__inside__buttons-container__wrap">
              <button
                tabIndex='0'
                className={'button button-primary '+
                           'Dispose-image-dialog__inside__buttons-container__button'}
                onClick={DevDashboardAction.hideConfirmDisposeImageDialog}>cancel</button>
              <button
                tabIndex='0'
                className={'button button-secondary '+
                           'Dispose-image-dialog__inside__buttons-container__button'}
                onClick={DevDashboardAction.stopApp.bind(this, this.props.appInstanceId)}>delete
              </button>
            </div>
          </div>
        {/* /Dispose-image-dialog__inside */}
        </div>
      {/* /Dispose-image-dialog */}
      </div>
    )
  }
});

ConfirmDisposeImageDialog.propTypes = {
  instanceName: React.PropTypes.string.isRequired
}

export default ConfirmDisposeImageDialog;
