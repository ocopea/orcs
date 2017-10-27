// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { Tabs, ArtifactRegistryConfig, DsbConfig, CrbConfig } from '../../components';
import styles from './styles-site-config.scss';

export default class SiteConfig extends React.Component {

  static tabs = [
    {
      key: 'artifactRegistry',
      name: 'artifact registry',
      icon: 'icon-settings',
      content: <ArtifactRegistryConfig />,
    },
    {
      key: 'dataServiceBroker',
      name: 'data service broker',
      icon: 'icon-db',
      content: <DsbConfig />,
    },
    {
      key: 'copyRepositoryBroker',
      name: 'copy repository broker',
      icon: 'icon-reports',
      content: <CrbConfig />,
    },
  ];

  constructor(props) {
    super(props);
    this.onTabSelected = this.onTabSelected.bind(this);
    this.state = {
      selectedTabKey: 'artifactRegistry',
    };
  }

  onTabSelected(tabKey) {
    this.setState({ selectedTabKey: tabKey });
  }

  render() {
    const selectedTab = SiteConfig.tabs.find(tab => tab.key === this.state.selectedTabKey);
    return (
      <div className={styles.SiteConfig}>
        <div className={styles.inside}>
          <div className={styles.title}>Site Settings</div>
          <div className={styles.subtitle}>Configure site settings</div>
          <Tabs
            tabs={SiteConfig.tabs}
            selectedTabKey={this.state.selectedTabKey}
            onTabSelected={tabKey => this.onTabSelected(tabKey)}
          />
          { selectedTab.content }
        </div>
      </div>
    );
  }
}
