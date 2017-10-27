// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-dev-main-menu.scss';
import Locations from '../../../locations.json';
import { FiltersList } from '../../';


@inject(["stores"])
@observer
export default class DevMainMenu extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { stores } = this.props;

    return(
      <div className={styles.MainMenu}>
        {
          stores.ui.currentLocation.pathname === Locations.development.home.pathname ?
            <div>
              <div className={styles.createNew}>
                <button className={styles.btnCreateNew}>create new</button>
              </div>
              <div className={styles.title}>filter by</div>
              <FiltersList />
            </div>
          : null
        }
      </div>
    )
  }

}
