import React from 'react';
import { LogoList } from '../..';
import RegistryTypes from './RegistryTypes.js';
import ChooseArtifactRegistryTypeLightbox from './ChooseArtifactRegistryTypeLightbox.jsx';
import AddArtifactRegistryLightbox from './AddArtifactRegistryLightbox.jsx';
import IconMaven from '../../../../assets/images/site-config/maven-new.png';
import IconCustom from '../../../../assets/images/site-config/custom.png';

import styles from './styles-artifact-registry-config.scss';

export default class ArtifactRegistryConfig extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      showChooseArtifactRegistryTypeLightbox: false,
      showAddArtifactRegistryLightbox: false,
      registries: [
        {
          name: 'Maven',
          logo: IconMaven,
          type: RegistryTypes.maven,
        },
        {
          name: 'Custom',
          logo: IconCustom,
          type: RegistryTypes.custom,
        },
      ],
    };
  }

  render() {
    return (
      <div className={styles.ArtifactRegistryConfig}>
        <LogoList
          listItems={this.state.registries}
          onAddNewClicked={() => this.setState({ showChooseArtifactRegistryTypeLightbox: true })}
        />
        {
          this.state.showChooseArtifactRegistryTypeLightbox ?
            <ChooseArtifactRegistryTypeLightbox
              onDismiss={() => this.setState({ showChooseArtifactRegistryTypeLightbox: false })}
              onSubmit={selectedArtifactRegistryType => this.setState({
                showChooseArtifactRegistryTypeLightbox: false,
                showAddArtifactRegistryLightbox: true,
                selectedArtifactRegistryType,
              })}
            /> :
            null
        }
        {
          this.state.showAddArtifactRegistryLightbox ?
            <AddArtifactRegistryLightbox
              appRepoType={this.state.selectedArtifactRegistryType}
              onDismiss={() => this.setState({ showAddArtifactRegistryLightbox: false })}
              onSubmit={() => this.setState({ showAddArtifactRegistryLightbox: false })}
            /> :
            null
        }
      </div>
    );
  }
}
