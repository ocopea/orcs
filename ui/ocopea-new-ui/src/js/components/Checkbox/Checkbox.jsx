// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import styles from './styles-checkbox.scss';

/* TODO make configurable so that we don't break Gal's production module */
/* TODO TOO LATE :) */
const Checkbox = props => (
  <span
    style={{
      width: props.width,
      height: props.height,
      border: props.hideBorder ? 'none' : null }}
    className={styles.Checkbox}>
    {
      props.selected ?
        <span
          className={styles.checkmark} >
          <span
            className="icon-check"
            style={{color: props.checkColor, fontSize: props.checkSize }}/>
        </span> :
        null
    }
  </span>
);

Checkbox.propTypes = {
  selected: React.PropTypes.bool.isRequired,
  width: React.PropTypes.number,
  height: React.PropTypes.number
};

export default Checkbox;
