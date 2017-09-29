// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
// import AppTopology from './appTopology.comp.js';
import AppTopology from '../../../../../shared-components/js/topology/_topology.jsx';
import TopologyDataController from '../../../../../shared-components/js/topology/controllers/topology-data-controller.js';
import TopologyUiController from '../../../../../shared-components/js/topology/controllers/topology-ui-controller.js';
import mockConfiguration from '../../../../../shared-components/js/topology/mockData/configuration/mock-configuration.json';
import AppTemplateConverter from '../../../../../shared-components/js/topology/helpers/converters/appTemplateConverter.js';
import EditAppInstanceName from './editInstanceName.comp.js';
import EntryPoint from './entryPoint.comp.js';
import Actions from '../../../actions/appTopologyActions.js';

import LabelA from '../../../../assets/images/labels/label-a.png';
import LabelB from '../../../../assets/images/labels/label-b.png';

var AppTopologyMain = React.createClass({

    render: function(){
        var className = "application-topology " + this.props.currentStepName;
        //console.log(this.props.general.editAppInstanceName)
        return(

            <div className={className}>

                <EntryPoint />

                <AppTopology configuration={mockConfiguration}/>

                <div className="services-label">
                    <img src={LabelA} />
                    <span>app services</span>
                </div>
                <div className="dependencies-label">
                    <img src={LabelB} />
                    <span>infrastructure service</span>
                </div>

            </div>

        )
    },

    getInitialState() {
      this.initTopology();
      return null;
    },

    initTopology() {
      const instance = this.props.selectedApp;
      const converted = AppTemplateConverter.convert(instance);

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
      TopologyUiController.setShowAlerts(true);
      TopologyUiController.setShowLines(true);
      TopologyUiController.setHighlightRelatedElements(true);
      TopologyUiController.setShowPlanSelectionMenu(false);

      TopologyDataController.init(converted);
    }

});

export default AppTopologyMain;

// <AppTopology
//  state={this.props.state}
//  selectedService={this.props.selectedService}
//  selectedDependency={this.props.selectedDependency}
//  currentStepName={this.props.currentStepName}
//  servicesTranslate={this.props.servicesTranslate}
//  dependenciesTranslate={this.props.dependenciesTranslate}
//  selectedApp={this.props.selectedApp}
//  allServices={this.props.allServices}/>

// <EditAppInstanceName
//         editAppInstanceName={this.props.general.editAppInstanceName}/>
