// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-instance-card-prod.scss';
import AppTemplateHandler from '../../models/AppTemplate/appTemplate-handler';
import User from '../../models/User/user-model';
import UserHandler from '../../models/User/user-handler';
import { Avatar } from '../../components';
import Sumup from './Sumup.jsx';


@inject(["stores"])
@observer
export default class InstanceCardProd extends React.Component{

  constructor(props){
    super(props)
    const creatorID = props.instance.creatorUserId;
    this.state = {
      creator: UserHandler.getUserById(creatorID),
      creatorAvatar: UserHandler.getUserAvatarURL(creatorID),
      productionInstances: this.props.productionInstances || Math.floor(Math.random() * 50),
      nonProductionInstances: this.props.nonProductionInstances || Math.floor(Math.random() * 50),
      offLineCopies: this.props.offLineCopies || Math.floor(Math.random() * 50),
    }
  }

  static propTypes = {instance: React.PropTypes.object.isRequired}

  render(){

    const { instance } = this.props;
    const appTemplate = AppTemplateHandler.getAppTemplateById(instance.appTemplateId) || {};

    return(
      <div className={styles.InstanceCardProd}>
        <span className="icon-link"></span>
        <div className={styles.inside} onClick={this.props.onCardClick.bind(this, instance)}>
          {/* Logo */}
          {
            <div className={styles.logo}>
              <img src={APISERVER+appTemplate.img} />
            </div>
          }
          {/* Instance name*/}
          <div className={styles.instanceName}>{instance.name}</div>
          <Sumup
            productionInstances={this.state.productionInstances}
            nonProductionInstances={this.state.nonProductionInstances}
            offLineCopies={this.state.offLineCopies}/>
          {/* creator */}
          <div className={styles.creator}>
            <Avatar
              src={this.state.creatorAvatar}
              style={{width:30, height:30, position: 'relative', top:10}}/>
            <span className={styles.fullName}>
              <span>created by</span>
              <span
                title={this.state.creator.fullName}
                className={styles.creatorName}>{this.state.creator.fullName}</span>
            </span>
          </div>
        </div>
      </div>
    )
  }

}
