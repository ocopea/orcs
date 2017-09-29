// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../../module-production/js/config.js';

var Footer = React.createClass({
  render: function(){

    var selectedStructure = this.props.selectedStructure;

    return(
      <div className="footer">
          {
            selectedStructure !== undefined ?
            <ul>
              <li title={selectedStructure.name.length > 10 ? selectedStructure.name : null}>
                {Config.getShortName(selectedStructure.name, 10)}
              </li>
              <li>
                <label>size: </label>
                <span>{selectedStructure.size == undefined ? '2GB' : selectedStructure.size}</span>
              </li>
              <li className="no-padding-left">
                <label>{selectedStructure.version !== undefined ? 'version:' : null}</label>
                <span>{selectedStructure.version}</span>
              </li>
              {
                selectedStructure.elementType == 'service' ?
                  <li className="no-padding-left">
                    <label>instances: </label>
                    <span>1</span>
                  </li>
                :
                null
              }
            </ul>

          :
            null
          }
      </div>
    )
  }
});

export default Footer;
