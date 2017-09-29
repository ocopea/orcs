// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../actions/site-config-actions.js';
import Config from '../../../../module-production/js/config.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';

// components
import AppRepositories from './app-repositories.comp.js';
import Tabs from './tabs.comp.js';
import DataServiceBroker from './data-service-broker/data-service-broker.comp.js';
import CopyRepoBroker from './copy-repo-broker/copy-repo-broker.comp.js';


let SiteConfig = React.createClass({

  render() {

    const baseClass =       this.props.baseClass;
    const defaultTab =      Object.keys(this.state.tabs)[0];
    let selectedRegistry =  this.props.selectedRegistry || {};
    let dsbs =              this.props.selectedRegistry ?
                              this.props.selectedRegistry.dsbs : [];
    let copyRepos =          this.props.selectedRegistry ?
                              this.props.selectedRegistry.copyRepos : null

    return(
      <div className="Site-config">
        <div className="Site-config__inside">
          <div className="Site-config__inside__title">site setup</div>
          <div className="Site-config__inside__subtitle">subtitle</div>
          <Tabs
            list={this.state.tabs}
            selected={this.props.selectedTab}
            className="link Site-config__inside__tabs__tab" />

          {
            this.props.selectedTab === this.props.tabs.appRepositories.key ?
              <AppRepositories registries={this.state.registries}/>
            :
            this.props.selectedTab === this.props.tabs.dataServiceBroker.key ?
              <DataServiceBroker dsbs={dsbs} />
            :
            this.props.selectedTab === this.props.tabs.copyRepositoryBroker.key
            && copyRepos ?
              <CopyRepoBroker copyRepos={copyRepos}/>
            :
            null
          }

        </div>
      </div>
    )
  },

  getInitialState() {

    const tabs = {
      appRepositories: {
        name: this.props.tabs.appRepositories.name,
        icon: 'icon-settings',
        onClick: SiteConfigActions.userSelectedTab.bind(this, this.props.tabs.appRepositories.key)
      },
      dataServiceBroker: {
        name: this.props.tabs.dataServiceBroker.name,
        icon: 'icon-db',
        onClick:  SiteConfigActions.userSelectedTab.bind(this, this.props.tabs.dataServiceBroker.key)
      },
      copyRepositoryBroker: {
        name: this.props.tabs.copyRepositoryBroker.name,
        icon: 'icon-reports',
        onClick:  SiteConfigActions.userSelectedTab.bind(this, this.props.tabs.copyRepositoryBroker.key)
      }
    };

    return {
      tabs: tabs,
      registries: []
    }
  },

  componentDidMount() {
    this.setState({
      registries: this.props.selectedRegistry ?
                    this.props.selectedRegistry.artifactRegistries : []
    });
  },

  componentDidUpdate(nextProps) {
    if(this.props.selectedRegistry !== nextProps.selectedRegistry){
      this.setState({
        registries: this.props.selectedRegistry ?
                      this.props.selectedRegistry.artifactRegistries : []
      });
    }
  }

});

export default SiteConfig;
