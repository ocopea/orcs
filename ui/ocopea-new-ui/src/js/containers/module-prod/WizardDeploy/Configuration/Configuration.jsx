import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-configuration.scss';
import stylesWizard from '../styles-wizard-deploy-prod.scss';
import Select from 'react-select';
import { AppTopology } from '../../../../components';
import AppTemplateConfigurationHandler from '../../../../models/AppTemplateConfiguration/appTemplate-configuration-handler';
import AppTemplateConverter from '../../../../components/AppTopology/helpers/converters/appTemplateConverter';
import TopologyUiController from '../../../../components/AppTopology/controllers/topology-ui-controller';
import TopologyDataController from '../../../../components/AppTopology/controllers/topology-data-controller';
import purposes from './purposes.json';
import SiteHandler from '../../../../models/Site/site-handler';


@observer
export default class Configuration extends React.Component{

  constructor(props){
    super(props)
    this.initTopology();
    const sites = props.sites.slice();
    const selectedSite = props.site;
    const defaultSpace = !_.isEmpty(selectedSite) ? selectedSite.spaces[0] : "";
    const spaces = selectedSite.spaces || [];
    const sitesOptions = this.parseSelectOptions(sites);
    const purposesOptions = this.parseSelectOptions(purposes);
    const spacesOptions = this.parseSelectOptions(spaces);
    const topologyConfig = AppTemplateConfigurationHandler.configuration;

    this.state = {
      sitesOptions: sitesOptions,
      purposesOptions: purposesOptions,
      selectedSite: {
        value: selectedSite.name,
        label: selectedSite.name,
        id: selectedSite.id,
        spaces: spacesOptions,
        selectedSpace: defaultSpace
      },
      topologyConfig: topologyConfig
    }
  }

  render(){

    const { } = this.props;

    return(
      <div className={styles.Configuration}>
        <div className={stylesWizard.title}>configuration</div>
        <div className={stylesWizard.subtitle}>
          you can configure run time parameters for your instance
        </div>

        {/* instance name input */}
        <div className={styles.inputInstanceName}>
          <span className="icon-edit"></span>
          <input
            type="text"
            placeholder="write your app name"
            onChange={this.onInstanceNameChange.bind(this)}/>
        </div>
        <div className={styles.selects}>
          {/* site selection */}
          <section>
            <label>site</label>
            <Select
              onChange={this.onSiteChange.bind(this)}
              clearable={false}
              value={this.state.selectedSite}
              options={this.state.sitesOptions}
              searchable={false} />
          </section>
          {/* purpose selection */}
          <section>
            <label>purpose</label>
            <Select
              clearable={false}
              searchable={false}
              value={this.state.purposesOptions[0]}
              options={this.state.purposesOptions} />
          </section>
          {/* space selection */}
          <section>
            <label>space</label>
            <Select
              clearable={false}
              searchable={false}
              value={this.state.selectedSite.selectedSpace}
              options={this.state.selectedSite.spaces} />
          </section>
        </div>

        {/* topology */}
        {
          this.state.topologyConfig ?
            <AppTopology configuration={this.state.topologyConfig} />
          : null
        }

      </div>
    )
  }

  initTopology() {
    const instance = this.props.selectedAppTemplate;
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
    TopologyUiController.setShowPlanSelectionMenu(true);

    TopologyDataController.init(converted);
  }

  onInstanceNameChange(e) {
    const instanceName = e.target.value;
    this.props.onSetInstanceName(instanceName);
  }

  onSiteChange(siteOption){
    const site = SiteHandler.getSiteById(siteOption.id);
    const spaces = this.parseSelectOptions(site.spaces);
    const defaultSpace = spaces[0];
    this.setState({
      selectedSite: {
        value: site.name,
        label: site.name,
        id: site.id,
        spaces: spaces,
        selectedSpace: defaultSpace
      }
    });
    this.props.onSetSelectedSite(site);
  }

  parseSelectOptions(array) {
    return array.map(item => {
      const name = item.name || item;
      return { value: name, label: name, id: item.id, ...item };
    });
  }

}
