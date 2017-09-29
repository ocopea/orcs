// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let InlineError = React.createClass({
  render() {
    return(
      <div className="Inline-error">
        <div className="content">{this.props.error}</div>
      </div>
    )
  }
});

export default InlineError;
