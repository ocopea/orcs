// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { LightBox, Button } from '../..';
import CopyRepositories from './CopyRepositories.jsx';

import styles from './styles-crb-details-lightbox.scss';

class CrbDetailsDialog extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  render() {
    const crbInfo = this.props.crbInfo;
    return (
      <div className={styles.CrbDetailsDialog}>
        <div className={styles.header}>
          <span className={styles.logoContainer}>
            <img src={`${APISERVER}${crbInfo.img}`} className={styles.logo}/>
          </span>
          <span className={styles.crbName}>{crbInfo.name}</span>
        </div>

        <div className={styles.title}>description</div>
        <div className={styles.description}>URN: {crbInfo.urn}</div>
        <div className={styles.description}>Type: {crbInfo.type}</div>
        <div className={styles.description}>Version: {crbInfo.version}</div>

        <div className={styles.title}>Copy Repositories</div>
        <CopyRepositories copyRepositories={crbInfo.copyRepositories} />
        <Button text='Add CR' primary onClick={this.props.onAddCopyRepo}/>

        <div className={styles.footer}>
          <Button onClick={this.props.onDismiss} text='cancel' />
          <Button onClick={this.props.onSubmit} primary text='save' />
        </div>
      </div>
    );
  }
}

const CrbDetailsLightBox = props => {
  return (
    <div className={styles.CrbDetailsLightBox}>
      <LightBox
        title="Copy Repository Broker Details"
        component={
          <CrbDetailsDialog
            crbInfo={props.crbInfo}
            onDismiss={props.onDismiss}
            onSubmit={props.onSubmit}
            onAddCopyRepo={props.onAddCopyRepo}
          />
        }
        width={604}
        height={627}
        onDismiss={props.onDismiss}
      />
    </div>
  );
}

CrbDetailsLightBox.propTypes = {
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default CrbDetailsLightBox;
