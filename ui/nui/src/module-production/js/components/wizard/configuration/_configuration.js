// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import _ from 'lodash';
import AppTopology from '../../../../../shared-components/js/topology/_topology.jsx';
import TopologyDataController from '../../../../../shared-components/js/topology/controllers/topology-data-controller.js';
import TopologyUiController from '../../../../../shared-components/js/topology/controllers/topology-ui-controller.js';
import AppTemplateConverter from '../../../../../shared-components/js/topology/helpers/converters/appTemplateConverter.js';
import { observer } from 'mobx-react';
import LabelA from '../../../../assets/images/labels/label-a.png';
import LabelB from '../../../../assets/images/labels/label-b.png';
import EntryPoint from '../appTopology/entryPoint.comp.js';
import DevWizardActions from '../../../../../module-development/js/actions/dev-wizard-actions.js';
import ProdWizardActions from '../../../actions/actions.js';


@observer
class ProductionConfiguration extends React.Component{
  render() {
    const selectedSite = this.props.selectedSite;
    const spaces = selectedSite ? selectedSite.spaces : [];

    return (
      <div className={`${this.props.currentStepName} Configuration-container`}>
         <h1 className="general-title">Configuration</h1>
         <h2 className="subtitle">you can configure runtime parameters for your instance</h2>
         <div className='Configuration-container__input-instance-name'>
           <span className="icon-edit link" onClick={this.editInstanceName.bind(this)}></span>
           <input
             ref={'inputInstanceName'}
             onChange={this.userInputInstanceName.bind(this)}
             className="input input-instance-name"
             type="text" placeholder="write your app name"/>
         </div>
         <div className="selects">
           <section>
             <label>site</label>
             <Select
               searchable={false}
               clearable={false}
               options={this.getSitesOptions()}
               value={this.state.site} />
           </section>
           <section>
             <label>purpose</label>
             <Select
               clearable={false}
               searchable={false}
               value={{value:'Production',label:'Production'}}
               options={[
                 {value:'Test/Dev', label:'Test/Dev'},
                 {value:'Production',label:'Production'}
               ]} />
           </section>
           <section>
             <label>space</label>
             <Select
               clearable={false}
               value={this.state.space}
               searchable={false}
               options={this.getSpacesOptions(spaces)} />
           </section>
         </div>
        <hr />

        <EntryPoint />
        <AppTopology
          baseClass={'Configuration-container'}
          configuration={this.props.configuration} />
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
  }

  constructor(props){
    super(props)
    this.state = {
      site: '',
      instanceName: ''
    }
    this.initTopology();
    DevWizardActions.setSelectedSite(this.props.selectedSite.id, this.props.instance);
  }

  initTopology() {

    let instance = this.props.instance;
    const converted = AppTemplateConverter.convert(instance);

    const size = {
      service: {width: 100, height: 100},
      dependency: {width: 100, height: 100}
    };

    // initialize topology ui
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
    TopologyUiController.setShowPlanSelectionMenu(true);

    TopologyDataController.init(converted);
  }

  componentDidMount() {
    if(this.getSitesOptions().length){
      this.setState({
        site: this.getSitesOptions()[0].value,
        space: this.props.selectedSite.spaces[0]
      })
    }
  }

  getSpacesOptions(spaces) {
    if(spaces && !_.isEmpty(spaces)){
      return spaces.map(space=>{
        return {value: space, label: space}
      });
    }else{
      return [];
    }
  }

  getSitesOptions() {
    return this.props.sites.map(site=>{
      return {value: site.name, label: site.name, id: site.id};
    })
  }

  editInstanceName() {
    this.refs.inputInstanceName.focus();
  }

  userInputInstanceName(e) {
    const instanceName = e.target.value;
    this.setState({
      instanceName: instanceName
    });
    DevWizardActions.userChangedAppInstanceName(instanceName);
  }
};

export default ProductionConfiguration;
