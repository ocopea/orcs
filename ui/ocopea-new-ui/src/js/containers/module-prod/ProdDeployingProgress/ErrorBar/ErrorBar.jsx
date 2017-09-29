import React from 'react';
import styles from './styles-error-bar.scss';

const ErrorBar = props => {

  return <div className={styles.ErrorBar}>
    <div className={styles.icon}>
      <span className="icon-x"></span>
    </div>
    <div className={styles.msg}>{props.msg}</div>
  </div>
}

export default ErrorBar;
