import React from 'react';
import styles from './styles-success-bar.scss';
import Locations from '../../../../locations.json';
import { hashHistory } from 'react-router';


const SuccessBar = props => {

  return <div className={styles.SuccessBar}>
    <div className={styles.title}>
      <div className={styles.icon}>
        <span className="icon-check"></span>
      </div>
      <div className={styles.msg}>
        <span className={styles.instanceName}>{props.instance.appInstanceName} </span>
        <span>was successfully deployed</span>
      </div>
    </div>
    <div className={styles.buttons}>
      <button
        onClick={goToDashboard.bind(this, props.instance.id)}
        className={styles.dashboard}>dashboard</button>
      <button
        className={styles.viewApp}
        onClick={viewApp.bind(this, props.instance.webEntryPointURL)}>view app</button>
    </div>
  </div>
}

function viewApp(url) {
  window.open(url, "blank")
}

function goToDashboard(instanceID) {
  hashHistory.push(`${Locations.production.dashboard.pathname}/${instanceID}`);
}
export default SuccessBar;
