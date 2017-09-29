// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Isvg from 'react-inlinesvg';
import TopologyParser from '../../../../../shared-components/js/topology-data-parser.js';
import DevNavigationOptions from '../../../data/devNavigationOptions.js';
import _ from 'lodash';
import $ from 'jquery';

//actions
import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import AppTopologyActions from '../../../../../module-production/js/actions/appTopologyActions.js';
import SharedImageActions from '../../../actions/dev-shared-image-actions.js';

//componenets
import DevTopology from '../../dev-topology.comp.js';
import AppTopology from '../../../../../shared-components/js/topology/_topology.jsx';
import MultiSelect from '../../shared-image/shared-image-selects.comp.js';
import TopologyWrapper from '../../topology-wrapper/topology-wrapper.jsx';

import TopologyDataController from '../../../../../shared-components/js/topology/controllers/topology-data-controller.js';
import TopologyUiController from '../../../../../shared-components/js/topology/controllers/topology-ui-controller.js';
import AppTemplateConverter from '../../../../../shared-components/js/topology/helpers/converters/appTemplateConverter.js';

//assets
import PencilIcon from '../../../../assets/images/wizard/config/pencil.svg';


class Config extends React.Component{

  getSelectsData(){

    var selectData = [];
    var baseClass = "Config__inside__selectsContainer__section__select";

    //data for site select
    var sitesSelect = {
      type: 'site',
      className: baseClass,
      onClick: ()=>{},
      options: this.props.config.sitesArray.map(site=>{
        return {
          text: site.name,
          siteId: site.id,
          className: `${baseClass}__option`
        }
      })
    };

    //data for purpose select
    var purposeSelect = {
      type: 'purpose',
      className: baseClass,
      onClick: ()=>{},
      options: [{name:'test/dev'}].map(purpose=>{
        return {
          text:purpose.name,
          className: `${baseClass}__option`
        }
      })
    };

    //data for space select
    var spaces = this.props.config.selectedSite.spaces || [];

    var spaceSelect = {
      type: 'space',
      className: baseClass,
      onClick: ()=>{},
      options: spaces.map(space=>{
        return {
          text:space,
          className: `${baseClass}__option`
        }
      })
    };

    selectData.push(sitesSelect);
    selectData.push(purposeSelect);
    selectData.push(spaceSelect);

    return selectData;
  }

  userChangedAppInstanceName(e){
    DevWizardActions.userChangedAppInstanceName(e.target.value);
  }

  componentDidMount(){
    DevWizardActions.userChangedAppInstanceName('');
    // set initial selected site
    var selectedSiteId = this.props.config.sitesArray[0] ?
                         this.props.config.sitesArray[0].id : undefined;

    if(selectedSiteId !== undefined){
        DevWizardActions.setSelectedSite(selectedSiteId);
    }
  }

  initTopology() {
    const instance = this.props.selectedApp; //this.props.copyDetails.imageDetails.details;
    const appTemplate = AppTemplateConverter.convert(instance);

    const size = {
      service: {width: 134, height: 134},
      dependency: {width: 110, height: 110}
    };

    TopologyUiController.setElementSize(size);
    const isScroll = appTemplate ? _.size(appTemplate.dataServices) >= 3 : true;
    TopologyUiController.setContainerWidth(isScroll ? 625 : 400);
    TopologyUiController.setContainerHeight(330);
    TopologyUiController.setShowIconCircle(false);
    TopologyUiController.setShowLogoCircle(true);
    TopologyUiController.setShowState(false);
    TopologyUiController.setShowAlerts(true);
    TopologyUiController.setShowLines(true);
    TopologyUiController.setHighlightRelatedElements(true);
    TopologyUiController.setShowPlanSelectionMenu(true);

    TopologyDataController.init(appTemplate);
  }

  constructor(props) {
    super(props)
    this.initTopology();
  }

  render(){

    return(
      <div className="Config">

        <div className="Config__title title">configuration</div>
        <div className="Config__subtitle">you can configure runtime parameters for your instance</div>

        <div className="Config__inside">
          <div className="Config__inside__topContainer">
            <div className="Config__inside__configureName">
              <span className="Config__inside__configureName__icon">
                <Isvg src={PencilIcon} className="Config__inside__configureName__icon__image"/>
              </span>
              <input
                type="text"
                placeholder={`write your app name`}
                className="Config__inside__configureName__input"
                onChange={this.userChangedAppInstanceName}/>
            </div>

            <div className="Config__inside__selectsContainer">
              <MultiSelect data={this.getSelectsData()}/>
            </div>

          </div>

          <TopologyWrapper
            component={<AppTopology configuration={this.props.config.configuration} />}
            title="topology" />

        </div>
      </div>
    )
  }
};

export default Config;


// {
//   !_.isEmpty(this.props.config.configuration) ?
//     <DevTopology
//       module={DevNavigationOptions.module}
//       tooltip={this.props.config.tooltip}
//       selectedElement={this.props.appTopologyState.appTopology.selectedElement}
//       isSelectedElementActive={this.props.appTopologyState.appTopology.selectedElement.isActive}
//       activeElements={this.props.config.activeElements}
//       selectedApp={this.props.copyDetails.imageDetails.details}
//       state={this.props.appTopologyState.appTopology}
//       selectedService={this.props.selectedService}
//       selectedDependency={this.props.selectedDependency}
//       currentStepName={this.props.currentStepName}
//       servicesTranslate={this.props.servicesTranslate}
//       dependenciesTranslate={this.props.dependenciesTranslate}
//       allServices={this.props.allServices}
//       configuration={this.props.config.configuration}
//       currentStepName="show"
//       simpleTooltip={this.props.simpleTooltip}
//       />
//   : null
// }
