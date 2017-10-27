// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-prod-home.scss';
import Locations from '../../../locations.json';
import { InstanceCardsProd } from '../../../components';


@inject(["stores"])
@observer
export default class ProdHome extends React.Component{

  render(){

    const { stores } = this.props;
    const appInstances = stores.data.appInstances;
    const instances = appInstances ? appInstances.slice() : [];
    return(

      <div className={styles.Home}>

        <InstanceCardsProd list={instances}/>

      </div>

    )
  }

  constructor(props){
    super(props)
    this.props.stores.ui.showMainMenu(true);
  }

}
