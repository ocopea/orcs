// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

let DsbProtocols = React.createClass({

  render() {

    const baseClass = "Dsb-details-dialog__protocols";
    // console.log(this.props)
    return (
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>supported protocols</div>
        <table>
          <thead>
            <tr>
              <th width="90">protocol</th>
              <th>version</th>
            </tr>
          </thead>
          <tbody>
          {
            this.props.protocols.map((protocol, i)=>{
              return (
                <tr key={i}>
                  <td>{protocol.protocolName}</td>
                  <td>{protocol.version || 'any version'}</td>
                </tr>
              )
            })
          }
          </tbody>
        </table>
      </div>

    )
  }

});

export default DsbProtocols;
