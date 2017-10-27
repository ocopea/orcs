// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-instances-list.scss';
import UserHandler from '../../models/User/user-handler';
import AppTemplateHandler from '../../models/AppTemplate/appTemplate-handler';
import { Avatar } from '../';
import Helper from '../../utils/helper.js';
import _ from 'lodash';
import { hashHistory } from 'react-router';
import Locations from '../../locations.json';


@inject(["stores"])
@observer
export default class InstancesList extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, list } = this.props;

    return(
      <div className={styles.InstancesList}>
        <table>
          <thead>
            <tr>
              <th>name</th>
              <th>owner</th>
              <th>created</th>
              <th>origin</th>
              <th colSpan={2}>app / service quota</th>
            </tr>
          </thead>
          <tbody>
            {
              list ?
                list.map(instance => {
                  const appInstance = instance.appInstance;
                  const creatorID = appInstance.creatorUserId;
                  const creator = UserHandler.getUserById(creatorID);
                  const appTemplate = AppTemplateHandler.getAppTemplateById(appInstance.appTemplateId);
                  const appTemplateImg = appTemplate ? APISERVER + appTemplate.img : null;
                  const dateCreated = Helper.formatDate(instance.dateCreated).date;
                  const maxDsbQuota = _.maxBy(instance.quota.dsbQuota, 'value').value;
                  const minDsbQuota = _.minBy(instance.quota.dsbQuota, 'value').value;

                  return (
                    <tr onClick={this.onCardClick.bind(this, appInstance)}>
                      <td>
                        <Avatar id="appTemplate-img" src={appTemplateImg} style={{width: 30, height: 30}}/>
                        <span className={styles.appInstanceName}>{appInstance.name}</span>
                      </td>
                      <td>{creator.fullName}</td>
                      <td>{dateCreated}</td>
                      <td></td>
                      <td>
                        <span className={styles.dsbQuotaMin}>{instance.quota.psbQuota}%</span>
                        <span className={styles.dsbQuotaMax}>{minDsbQuota}% - {maxDsbQuota}%</span>
                      </td>
                      <td><span className="icon-arrow-open"></span></td>
                    </tr>
                  )
                })
              : null
            }
          </tbody>
        </table>
      </div>
    )
  }

  onCardClick(instance, e) {
    const instanceID = instance.id;
    hashHistory.push(
      `${Locations.development.dashboard.pathname}/${instanceID}`
    )
  }

}
