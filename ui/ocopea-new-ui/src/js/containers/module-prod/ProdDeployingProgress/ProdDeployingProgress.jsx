// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { autorun } from 'mobx';
import styles from './styles-deploying-progress.scss';
import AppInstanceHandler from '../../../models/AppInstance/appInstance-handler';
import AppInstanceConverter from '../../../components/AppTopology/helpers/converters/appInstanceConverter';
import TopologyDataController from '../../../components/AppTopology/controllers/topology-data-controller';
import TopologyUiController from '../../../components/AppTopology/controllers/topology-ui-controller';
import StateHandler from '../../../components/AppTopology/helpers/handleState.js';
import { AppTopology, ProgressBarCount, Logs } from '../../../components';
import SuccessBar from './SuccessBar/SuccessBar.jsx';
import ErrorBar from './ErrorBar/ErrorBar.jsx';
import { hashHistory } from 'react-router';
import _ from 'lodash';
import uiStore from '../../../stores/ui-store';
import Locations from '../../../locations.json';
import LogsHandler from '../../../components/Logs/LogsHandler';
import Loader from '../../_Entry/loader.gif';


@inject(["stores"])
@observer
export default class ProdDeployingProgress extends React.Component{

  constructor(props){
    super(props)
    this.instanceID = this.props.params.appTemplateID;
    AppInstanceHandler.receiveState({});
    StateHandler.clearState();
    //props.stores.ui.showMainMenu(false);
    this.fetchState(this.instanceID);
    this.state = { websockets: [], stopFetch: false };
    props.stores.ui.showMainMenu(false);
  }

  render(){

    const {  } = this.props;
    const deploymentState = AppInstanceHandler.deploymentState;
    const instanceName = deploymentState ?
      deploymentState.appInstanceName : "";
    const instanceState = deploymentState.state;
    const isRunning = instanceState === StateHandler.stateDictionary.running;
    const isError = instanceState === StateHandler.stateDictionary.error;
    const websockets = LogsHandler.websockets; //this.state.websockets;

    return(
      <div className={styles.DeployingProgress}>
        <div className={styles.inside}>
          {
            instanceName ?
              <div>
                <div className={styles.topBar}>
                  {
                    isRunning && !StateHandler.status.inProgress.length ?
                      <SuccessBar instance={deploymentState}/>
                    :
                    isError ?
                      <ErrorBar msg={deploymentState.stateMessage} />
                    :
                    <ProgressBarCount
                      instanceName={instanceName}
                      done={StateHandler.status.done.slice()}
                      inProgress={StateHandler.status.inProgress.slice()}
                      height={8}
                      width={500}/>
                  }
                </div>
                <AppTopology instanceState={instanceState}/>
                <Logs websockets={websockets} />
              </div>
            : <img src={Loader} className='master-loader' />
          }
        </div>
      </div>
    )
  }

  componentDidMount() {
    this.initTopology();
  }

  componentWillUnmount() {
    this.stopFetch = true;
  }

  fetchState(instanceID) {

    const shouldFetch = this.shouldFetch.bind(this);
    const stopFetch = this.stopFetch;
    (function _fetch() {
      let timer = setTimeout(() => {
        const instance = AppInstanceHandler.deploymentState;
        if(shouldFetch(instance.state) && !stopFetch) {
          AppInstanceHandler.fetchState(instanceID);
          if(!_.isEmpty(instance)) {
            const converted = AppInstanceConverter.convert(instance);
            TopologyDataController.init(converted);
          }
          _fetch();
        }else{
          clearTimeout(timer);
          return;
        }
      }, 1000)
    })()
  }

  shouldFetch(instanceState) {
    const isDoneFetching = StateHandler.isDoneFetching(TopologyDataController, instanceState) || this.stopFetch;
    if(isDoneFetching) AppInstanceHandler.fetchAppInstance();
    return !isDoneFetching;
  }

  initTopology(instance) {

    const size = {
      service: {width: 100, height: 100},
      dependency: {width: 100, height: 100}
    };
    TopologyUiController.setElementSize(size);
    TopologyUiController.setContainerHeight(300);
    TopologyUiController.setShowIconCircle(true);
    TopologyUiController.setShowLogoCircle(false);
    TopologyUiController.setShowState(true);
    TopologyUiController.setShowAlerts(true);
    TopologyUiController.setShowLines(true);
    TopologyUiController.setHighlightRelatedElements(true);
    TopologyUiController.setShowPlanSelectionMenu(false);

  }

}


autorun(()=>{
  const pathname = hashHistory.getCurrentLocation().pathname;
  const instanceId = _.split(pathname, '/')[3];
  const currentLocation = uiStore.currentLocation || {};
  const isDeployingProgress = currentLocation.pathname ===
                                Locations.production.deployingProgress.pathname;
  if(isDeployingProgress) {
    AppInstanceHandler.fetchLogs(instanceId, response => {
      LogsHandler.receiveWebsockets(response);
    });
  }
})
