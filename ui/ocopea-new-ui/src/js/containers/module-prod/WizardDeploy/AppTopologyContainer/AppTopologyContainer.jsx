// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-app-topology.scss';
import stylesWizard from '../styles-wizard-deploy-prod.scss';
import { AppTopology } from '../../../../components';
import AppTemplateConverter from '../../../../components/AppTopology/helpers/converters/appTemplateConverter.js';
import TopologyUiController from '../../../../components/AppTopology/controllers/topology-ui-controller.js';
import TopologyDataController from '../../../../components/AppTopology/controllers/topology-data-controller.js';
import AppTemplateConfigurationHandler from '../../../../models/AppTemplateConfiguration/appTemplate-configuration-handler';
import _ from 'lodash';


@inject(["stores"])
@observer
export default class AppTopologyContainer extends React.Component{

  constructor(props){
    super(props)
    const siteId = this.props.site.id;
    const appTemplate = this.props.selectedAppTemplate;
    const appTemplateId = appTemplate.id;

    if(appTemplate && siteId && appTemplateId) {
      const hideLoader = true;
      AppTemplateConfigurationHandler.fetchAppTemplateConfig(siteId, appTemplateId, hideLoader);
      this.initTopology(appTemplate);
    }
  }

  render(){

    const { stores } = this.props;
    const configuration = AppTemplateConfigurationHandler.configuration || null;

    return(
      <div className={styles.AppTopology}>
        <div className={stylesWizard.title}>app topology</div>
        <div className={stylesWizard.subtitle}>subtitle</div>
        {
          configuration ? <AppTopology configuration={configuration}/> : null
        }
      </div>
    )
  }

  initTopology(appTemplate) {
    const converted = AppTemplateConverter.convert(appTemplate);
    const size = {
      service: {width: 100, height: 100},
      dependency: {width: 100, height: 100}
    };
    TopologyUiController.setElementSize(size);
    const isScroll = converted ? _.size(converted.dataServices) >= 3 : true;
    TopologyUiController.setContainerWidth(isScroll ? 548 : 300);
    TopologyUiController.setContainerHeight(300);
    TopologyUiController.setShowIconCircle(true);
    TopologyUiController.setShowLogoCircle(false);
    TopologyUiController.setShowState(false);
    TopologyUiController.setShowAlerts(false);
    TopologyUiController.setShowLines(true);
    TopologyUiController.setHighlightRelatedElements(true);
    TopologyUiController.setShowPlanSelectionMenu(false);

    TopologyDataController.init(converted);
  }
}
