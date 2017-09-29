// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../module-production/js/config.js';
import DashboardActions from '../../actions/dev-dashboard-actions.js';
import DevActions from '../../actions/dev-actions.js';


var SuccessBar = React.createClass({

  userClickOnDismiss(){
    DashboardActions.setLoadingImage({});
  },

  componentDidUpdate: function(nextProps){
    if(this.props.savedAppImages !== nextProps.savedAppImages){
      DevActions.getSavedImageById(this.props.loadingImage.id);
    }
  },

  userClickOnShare(){
    DevActions.showShareImageDialog(this.props.imageToShare);
  },

  render(){
    var baseClass = 'Dashboard__inside__success-image-saved-container';
    return(
      <div className={baseClass}>
        <span className={`${baseClass}__check-container check-container`}>
          <span className="icon-check"></span>
        </span>
        <span>image </span>
        <span
          className={`${baseClass}__image-name`}
          title={Config.getTitleOrNull(this.props.loadingImage.name, 20)}>
            {Config.getShortName(this.props.loadingImage.name, 20)}
        </span>
        <span> was successfully created</span>
        <div className={`${baseClass}__buttons`}>
          <button
            onClick={this.userClickOnDismiss}
            className={`${baseClass}__buttons__button `+
                      `${baseClass}__buttons__button--remove-success-msg link`}>
              <span>got it</span>
          </button>
          <button
            onClick={this.userClickOnShare}
            className={`${baseClass}__buttons__button `+
                      `${baseClass}__buttons__button--share link`}>
              <span>share</span>
          </button>
        </div>
      </div>
    )
  }
});

SuccessBar.propTypes = {
  loadingImage: React.PropTypes.object.isRequired // <----------{name: "", precent: 0}
}

export default SuccessBar;
