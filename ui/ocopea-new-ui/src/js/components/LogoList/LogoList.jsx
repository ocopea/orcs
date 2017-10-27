// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { PlusLogo } from '..';
import LogoListItem from './LogoListItem.jsx';
import styles from './styles-logo-list.scss';

const LogoList = props => (
  <div className={styles.LogoList}>
    {
      props.listItems.map(item => (
        <LogoListItem
          key={item.name}
          name={item.name}
          description={item.description}
          logo={item.logo}
        />
      ))
    }
    <LogoListItem
      name="Add New"
      logo={<PlusLogo />}
      onClick={props.onAddNewClicked}
    />
  </div>
);

const listItemShape = React.PropTypes.shape({
  name: React.PropTypes.string.isRequired,
  description: React.PropTypes.string,
  logo: React.PropTypes.string.isRequired,
});

LogoList.propTypes = {
  listItems: React.PropTypes.arrayOf(listItemShape).isRequired,
  onAddNewClicked: React.PropTypes.func,
};

LogoList.defaultProps = {
  onAddNewClicked: () => null,
};

export default LogoList;
