// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ProgressBar from '../main-screen/progress-bar.comp.js';
import Config from '../../../../module-production/js/config.js';
import _ from 'lodash';


var LoadingImagesProgressBars = React.createClass({

  populateProgressBars: function(){
    if(!_.isEmpty(this.props.image)){
      var className = this.props.image.name.replace(/ /g,'').replace(/[^\w\s]/gi, '')
      return (
        <div className="Dashboard__inside__image-loading__container">
          <span
            className="Dashboard__inside__image-loading__container__label"
            title={this.props.image.name.length > 10 ? this.props.image.name : null}>
              creating {Config.getShortName(this.props.image.name, 10)}
          </span>
          <ProgressBar
            instanceName={`image-loading-${className}`}
            precent={this.props.image.precent}
            width={880}
            height={8}
            disableAnimation={true}/>
        </div>
      )
      return progressBars;

    }
  },

  render: function(){
    // console.log(this.props)
    return(
      <div className="Dashboard__inside__image-loading">
        {this.populateProgressBars()}
      </div>
    )
  }
});

LoadingImagesProgressBars.propTypes = {
  image: React.PropTypes.object.isRequired
}

export default LoadingImagesProgressBars;
