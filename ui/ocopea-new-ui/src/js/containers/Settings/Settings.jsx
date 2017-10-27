// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-settings.scss';
import { AppTemplateList } from '../../components';


@inject(["stores"])
@observer
export default class Settings extends React.Component{

  render(){

    const { t, stores } = this.props;

    const appTemplates = stores.data.appTemplates || [];

    return(

      <div className={styles.Settings}>
        <AppTemplateList appTemplatesList={appTemplates}/>
      </div>
    )
  }

  constructor(props){
    super(props)
  }

};