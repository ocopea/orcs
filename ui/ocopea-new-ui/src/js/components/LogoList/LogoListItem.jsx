// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import styles from './styles-logo-list-item.scss';

const LogoListItem = props => (
  <div className={styles.LogoListItem} onClick={props.onClick} role="button" tabIndex="0">
    <section className={styles.logoSection}>
      <span className={styles.logoCircle}>
        {
          typeof props.logo === 'string' ?
            <img src={props.logo} className={styles.logoImage} alt="" /> :
            props.logo
        }
      </span>
    </section>
    <section className={styles.nameSection}>
      <span className={styles.name}> {props.name} </span>
    </section>
    <section className={styles.descriptionSection}>
      <span className={styles.description}> {props.description} </span>
    </section>
    <section className={styles.iconsSection}>
      { props.onInfoClicked ? <button className={`icon-info ${styles.icon}`} /> : null }
      { props.onEditClicked ? <button className={`icon-edit ${styles.icon}`} /> : null }
      { props.onDeleteClicked ? <button className={`icon-delete ${styles.icon}`} /> : null }
    </section>
  </div>
);

LogoListItem.propTypes = {
  logo: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.node]),
  name: React.PropTypes.string.isRequired,
  description: React.PropTypes.string,
  onClick: React.PropTypes.func,
  onEditClicked: React.PropTypes.func,
  onInfoClicked: React.PropTypes.func,
  onDeleteClicked: React.PropTypes.func,
};

LogoListItem.defaultProps = {
  logo: null,
  description: '',
  onClick: null,
  onEditClicked: null,
  onDeleteClicked: null,
  onInfoClicked: null,
};

export default LogoListItem;
