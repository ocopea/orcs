// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let SimpleTooltip = React.createClass({
  render() {

    const baseClass = "Naz-tooltip Naz-simple-tooltip";
    
    return(
      <div className={baseClass} style={this.props.data.position}>
        <div className={`${baseClass}__inside`}>
          <div className={`${baseClass}__inside__title`}>{this.props.data.title}</div>
          <div className={`${baseClass}__inside__error`}>{this.props.data.error}</div>
        </div>
      </div>
    )
  }
});

export default SimpleTooltip;
