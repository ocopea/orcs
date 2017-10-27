// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-prod-dashboard.scss';
import AppInstanceHandler from '../../../models/AppInstance/appInstance-handler';
import _ from 'lodash';
import {
  Activities,
  Statistics,
  AppAvailabilityZone,
  CopyHistory,
  Sankey } from '../../../components';


@inject(["stores"])
@observer
export default class ProdDashboard extends React.Component{

  constructor(props){
    super(props)

    this.state = {instance: {}, dashboardStats: null, copyHistory: null};

    props.stores.ui.showMainMenu(true);
    const instanceID = this.props.params.instanceID;

    this.updateInstance(instanceID);

    this.fetchDashboardStats(instanceID);

  }

  render(){

    const dashboardStats = AppInstanceHandler.dashboardStats || {};
    const copyHistory = this.state.copyHistory;
    const instance = dashboardStats.appInstance;
    const instanceID = this.props.params.instanceID;
    const copySummary = dashboardStats.copySummary;
    const appGeography = dashboardStats.appGeography;
    const sankeyData = dashboardStats.copyDistributionSankey;

    return(
      <div className={styles.ProdDashboard}>
        <div className={styles.container}>
          <div className={styles.row}>
            <card>
              <Activities />
            </card>
            <card>
              {
                copySummary ?
                  <Statistics
                    copySummary={copySummary}/>
                : null
              }
            </card>
            <card>
              {
                appGeography ?
                  <AppAvailabilityZone appGeography={appGeography} />
                : null
              }
            </card>
          </div>

          <div className={styles.row}>
            <CopyHistory instanceID={instanceID} />
          </div>

          {
            sankeyData ?
              <Sankey data={sankeyData}/>
            : null
          }
        </div>
      </div>
    )
  }

  updateInstance(instanceID) {
    const instance = AppInstanceHandler.getInstanceById(instanceID);    
    if (!instance) {
      AppInstanceHandler.fetchInstanceById(
        instanceID,
        this.fetchInstanceCallBack.bind(this)
      );
    }
  }

  fetchDashboardStats(instanceID) {
    AppInstanceHandler.fetchDashboardStats(instanceID, response => {
      AppInstanceHandler.receiveDashboardStats(response);
      this.updateInstance(instanceID);
    });

  }

  componentWillReceiveProps(nextProps) {

    if(this.props.params.instanceID !== nextProps.params.instanceID) {
      const instanceID = nextProps.params.instanceID;
      this.fetchDashboardStats(instanceID);
    }
  }

  fetchInstanceCallBack(instance) {
    this.setState({
      instance: instance
    });
  }
}
