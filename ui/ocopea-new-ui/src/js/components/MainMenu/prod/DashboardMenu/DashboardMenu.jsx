// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React, { PropTypes, Component } from 'react';
import { observer } from 'mobx-react';
import { observable } from 'mobx';
import { hashHistory } from 'react-router';
import styles from './styles-dashboard-menu.scss';
import AppTemplateHandler from '../../../../models/AppTemplate/appTemplate-handler';
import _ from 'lodash';
import Locations from '../../../../locations.json';
import { Button } from '../../../../components'


@observer
export default class DashboardMenu extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    instances: PropTypes.array.isRequired
  }

  render(){

    const { instances } = this.props;

    return(
      <div className={styles.DashboardMenu}>
        <Button
          primary={true}
          text={'add application'}
          iconClassName='icon-create'
          iconColor='#fff'
          onClick={this.goToDeployWizard.bind(this)}
          size={{width:'100%', height:'42px'}}/>
        <ul>
        {
          instances.map(instance => {
            const _location = hashHistory.getCurrentLocation().pathname;
            const selectedInstanceId = _.split(_location, '/')[3];
            const appTemplate = instance.appTemplate;
            return <Row
              instance={instance}
              appTemplate={appTemplate}
              isSelected={selectedInstanceId === instance.id}
              key={instance.id} />
          })
        }
        </ul>
      </div>
    )
  }

  goToDeployWizard() {
    hashHistory.push(Locations.production.wizardDeploy.steps.appMarket.pathname);
  }

}


class Row extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    const { instance, isSelected, appTemplate } = this.props;
    const instanceAppTemplate = instance.appTemplate;
    const logoSrc = appTemplate ? `${APISERVER}${appTemplate.img}` : '';

    return (
        appTemplate ?
          <li
            onClick={this.navigate.bind(this, instance)}
            key={instance.id}
            className={isSelected ? styles.selected : null}>
            <div className={styles.templateLogo}>
              <img src={logoSrc} />
            </div>
            <div className={styles.content}>
              <span className={styles.instanceName}>{instance.name}</span>
              <span className={styles.appTemplateName}>{appTemplate.name}</span>
            </div>
          </li>
        : null
    )
  }

  navigate(instance) {
    const instanceID = instance.id;
    hashHistory.push(`${Locations.production.dashboard.pathname}/${instanceID}`)
  }

}
