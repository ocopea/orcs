// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-app-market.scss';
import stylesWizard from '../styles-wizard-deploy-prod.scss';
import { AppTemplateCardsProd } from '../../../../components';


@inject(["stores"])
@observer
export default class AppMarket extends React.Component{

  constructor(props){
    super(props)
  }

  static propTyoes = {
    appTemplates: React.PropTypes.array.isRequired,
    onSetSelectedAppTemplate: React.PropTypes.func.isRequired,
    selectedAppTemplate: React.PropTypes.object.isRequired
  }

  render(){

    const { appTemplates, selectedAppTemplate, onSetSelectedAppTemplate } = this.props;

    return(
      <div className={styles.AppMarket}>
        <div className={stylesWizard.title}>app market</div>
        <div className={stylesWizard.subtitle}>subtitle</div>
        {
          appTemplates && appTemplates.length ?
            <AppTemplateCardsProd
              selectedAppTemplate={selectedAppTemplate}
              onSetSelectedAppTemplate={onSetSelectedAppTemplate}
              appTemplates={appTemplates}/>
          : null
        }
      </div>
    )
  }

}
