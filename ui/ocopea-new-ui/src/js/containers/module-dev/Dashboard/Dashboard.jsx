// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-dashboard.scss';
import TopContainer from './Dashboard.jsx';
import AppInstanceHandler from '../../../models/AppInstance/appInstance-handler';


@inject(["stores"])
export default class Dashboard extends React.Component{

  constructor(props){
    super(props)
    this.state = {
      instance: {}
    }
  }

  componentDidMount() {
    const appInstance = AppInstanceHandler.getInstanceById(this.props.params.instanceID);
    if(!appInstance) {
      AppInstanceHandler.fetchInstanceById(this.props.params.instanceID, this.callBack.bind(this));
    }else{
      this.setState({
        instance: appInstance
      })
    }
  }

  callBack(instance) {
    this.setState({
      instance: instance
    })
  }

  render(){

    const { t, stores } = this.props;
    const instance = this.state.instance;

    return(
      <div className={styles.Dashboard}>
        {
          instance ?
          <div>
            <div>{instance.name}</div>
          </div>
          :
          <div>instance not found</div>
        }
      </div>
    )
  }

}
