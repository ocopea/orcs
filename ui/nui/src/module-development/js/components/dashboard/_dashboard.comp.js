// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';

//actions
import DashboardActions from '../../actions/dev-dashboard-actions.js';
import ProdActions from '../../../../module-production/js/actions/actions.js';

//components
import DashboardTitle from './dashboard-title.comp.js';
import LoadingImageProgressBars from './loading-images-progress-bars.comp.js';
import ImageOrigin from './image-origin.comp.js';
import AppTopology from './dashboard-topology.comp.js';
// import AppTopology from '../../../../shared-components/js/topology/_topology.jsx';
import TopologyDataController from '../../../../shared-components/js/topology/controllers/topology-data-controller.js';
import TopologyUiController from '../../../../shared-components/js/topology/controllers/topology-ui-controller.js';
import AppTemplateConverter from '../../../../shared-components/js/topology/helpers/converters/appTemplateConverter.js';
import mock from '../../../../shared-components/js/topology/mockData/appTemplates/appTemplates.json';

import ServiceInfo from './dashboard-service-info.comp.js';
import InventoryManager from './dashboard-inventory-manager.comp.js';
import SuccessBar from './dashboard.success-bar.comp.js';
import FailBar from './dashboard.fail-bar.comp.js';

import Config from '../../../../module-production/js/config.js';
var GeminiScrollbar = require('react-gemini-scrollbar');
import stateOptions from '../../data/deploying-state-options.js';

var Dashboard = React.createClass({

  componentDidMount: function(){

    // DashboardActions.setLoadingImage(undefined);

    if(this.props.isLeftMenuRender){
      //ProdActions.closeLeftMenu();
    }
    if(!_.isEmpty(this.props.appTemplates))
      this.appTemplate = this.props.appTemplates[this.props.selectedInstance.appTemplateId];
  },

  componentWillUnmount: function(){
    DashboardActions.hideCreateImageDialog();
    DashboardActions.stopFetchingState();
  },

  componentDidUpdate: function(nextProps){

    var appTemplate = this.props.appTemplates[this.props.selectedInstance.appTemplateId];
    if(appTemplate !== undefined){
      this.appTemplate = appTemplate;
    }else{
      this.appTemplate = {img: "", version: ""}
    }

    if(this.props.selectedInstance !== nextProps.selectedInstance){
      const converted = AppTemplateConverter.convert(appTemplate);

      // TopologyUiController.setElementSize(size);
      TopologyUiController.setContainerWidth(560);
      TopologyUiController.setContainerHeight(350);
      TopologyUiController.setShowIconCircle(false);
      TopologyUiController.setShowLogoCircle(true);
      TopologyUiController.setShowState(false);
      TopologyUiController.setShowAlerts(true);
      TopologyUiController.setShowLines(true);
      TopologyUiController.setHighlightRelatedElements(true);
      TopologyUiController.setShowPlanSelectionMenu(false);

      TopologyDataController.init(converted);
    }
  },

  parseLoadingImage: function(image){
    image.precent = image.state.toUpperCase() === stateOptions.creating.toUpperCase() ? 50
                    : image.state.toUpperCase() === stateOptions.created.toUpperCase() ? 100 : 0;
    return image;
  },

  isImageLoading: function(){
    return _.isEmpty(this.props.loadingImage) ? false : true;
  },

  appTemplate: {},

  render: function(){
    // console.log(this.props.appTopologyData)
    return(
      <GeminiScrollbar>
        <div className="Dashboard">
          <DashboardTitle
            selectedInstance={this.props.selectedInstance}
            appTemplate={this.appTemplate}
            creator={this.props.users[this.props.selectedInstance.creatorUserId]}
            isImageLoading={this.isImageLoading()}
            imageState = {this.props.loadingImage && this.props.loadingImage.state ? this.props.loadingImage.state.toUpperCase() : null}/>
          <div className="Dashboard__inside">
            {
              !_.isEmpty(this.props.loadingImage) ?
                this.props.loadingImage.state.toUpperCase() === stateOptions.creating.toUpperCase() ?
                  <LoadingImageProgressBars
                    image={this.parseLoadingImage(this.props.loadingImage)}/>
                :
                this.props.loadingImage.state.toUpperCase() === stateOptions.created.toUpperCase() ?
                  <SuccessBar
                    loadingImage={this.props.loadingImage}
                    imageToShare={this.props.imageToShare}
                    savedAppImages={this.props.savedAppImages}/>
                :
                this.props.loadingImage.state.toUpperCase() === stateOptions.failed.toUpperCase() ?
                  <FailBar loadingImage={this.props.loadingImage}/>
                :
                null
              :
              null
            }

            <ImageOrigin
              savedImages={this.props.imageOriginData}
              users={this.props.users}
              appTemplates={this.props.appTemplates}/>
            {
              this.props.appTopologyData.appServiceTemplates ?

              <AppTopology
                data={this.props.appTopologyData}
                topologyState={this.props.appTopologyState}
                selectedApp={this.props.appTopologyState.appTopology}/>

              : null
            }
            <div className="Dashboard__inside__right-container">
              <ServiceInfo selectedElement={this.props.appTopologyState.appTopology.selectedElement}/>
              <InventoryManager
                selectedElement={this.props.appTopologyState.appTopology.selectedElement}/>
            </div>
          {/* /Dashboard__inside */}
          </div>
        {/* /Dashboard */}
        </div>
      </GeminiScrollbar>
    )
  }

});

export default Dashboard;


// <div className='Dashboard__inside__topology'>
//   <AppTopology />
// </div>
