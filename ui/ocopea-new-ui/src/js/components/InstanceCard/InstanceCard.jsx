import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import { hashHistory } from 'react-router';
import styles from './styles-instance-card.scss';
import UserHandler from '../../models/User/user-handler';
import { Avatar } from '../';
import Helper from '../../utils/helper.js';
import IconDate from './date-icon.svg';
import DefaultLogo from './default-logo.png';
import Sumup from './Sumup.jsx';
import Locations from '../../locations.json';


@inject(["stores"])
export default class InstanceCard extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, instance } = this.props;
    const avatarURL = UserHandler.getUserAvatarURL(instance.appInstance.creatorUserId);
    const dateCreated = Helper.formatDate(instance.appInstance.dateCreated).date;
    const creatorUser = UserHandler.getUserById(instance.appInstance.creatorUserId);
    const creatorFullName = creatorUser ? creatorUser.fullName : null;
    const numberOfAppServices = instance.numberOfAppServices;
    const numberOfInfraServices = instance.numberOfInfraServices;

    return(
      <div
        onClick={this.onCardClick.bind(this, instance)}
        className={styles.Card}
        id="link">
        <div className={styles.inside}>
          {/* header */}
          <div className={styles.header}>
            <div className={styles.creator}>
              <Avatar src={avatarURL} style={{width:24, height:24}} />
              <div
                title={creatorFullName}
                className={styles.creatorName}>{creatorFullName}</div>
            </div>
            <div className={styles.dataCreated}>
              <img className={styles.icon} src={IconDate} />
              {dateCreated}
            </div>
          </div>
          {/* logo */}
          <div className={styles.logo}>
            {
              instance.img
              ? <img className={styles.logoImg} src={instance.img} />
              : <img className={styles.logoImg} src={DefaultLogo} />
            }
            <div onClick={this.openApp.bind(this, instance)} className={styles.iconOpenAppContainer}>
              <span id={styles.iconOpenApp} className="icon-arrow-open"></span>
            </div>
            {/* instance name */}
            <div className={styles.instanceName} title={instance.appInstance.name}>
              {instance.appInstance.name}
            </div>
            {/* sum up */}
          </div>

          <Sumup
            numberOfAppServices={numberOfAppServices}
            numberOfInfraServices={numberOfInfraServices}
            quota={instance.quota} />

        </div>
      </div>
    )
  }

  onCardClick(instance, e) {
    if(e.target.id !== styles.iconOpenApp &&
       e.target.className !== styles.iconOpenAppContainer) {
      const instanceID = instance.appInstance.id;
      hashHistory.push(
        `${Locations.development.dashboard.pathname}/${instanceID}`
      )
    }
  }

  openApp(instance) {
    window.open(
      instance.appInstance.webEntryPointURL,
      'blank'
    )
  }

}
