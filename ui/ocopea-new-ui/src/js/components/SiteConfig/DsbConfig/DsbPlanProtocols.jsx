// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import styles from './styles-dsb-plan-protocols.scss';

let DsbPlanProtocols = React.createClass({

  render() {
    return (
      <div className={styles.DsbPlanProtocols}>
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

export default DsbPlanProtocols;
