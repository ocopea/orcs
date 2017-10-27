// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import styles from './styles-copy-repositories.scss';

let CopyRepositories = React.createClass({

  render() {
    return (
      <div className={styles.CopyRepositories}>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Size</th>
            </tr>
          </thead>
          <tbody>
            {
              this.props.copyRepositories.length > 0 ?
                this.props.copyRepositories.map((cr, i) => {
                  return (
                    <tr key={cr.name}>
                      <td>{cr.name}</td>
                      {/* dummy size */}
                      <td>{i + 1}GB</td>
                    </tr>
                  )
                }) :
                null
            }
          </tbody>
        </table>
      </div>
    )
  }
});

export default CopyRepositories;
