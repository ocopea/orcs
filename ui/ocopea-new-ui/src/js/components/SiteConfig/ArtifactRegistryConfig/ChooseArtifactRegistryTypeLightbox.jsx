import React from 'react';
import { LightBox, Button, Checkbox } from '../..';
import IconMaven from '../../../../assets/images/site-config/maven-new.png';
import IconDocker from '../../../../assets/images/site-config/docker.png';
import IconGit from '../../../../assets/images/site-config/git.png';
import styles from './styles-choose-artifact-registry-type-lightbox.scss';

class ChooseArtifactRegistryTypeDialog extends React.Component {

  static propTypes = {
    onDismiss: React.PropTypes.func.isRequired,
    onSubmit: React.PropTypes.func.isRequired,
  }

  static atrifactRepoTypes = [
    {
      name: 'maven',
      icon: IconMaven,
      enabled: true,
    },
    {
      name: 'docker',
      icon: IconDocker,
      enabled: false,
    },
    {
      name: 'git',
      icon: IconGit,
      enabled: false,
    },
    {
      name: 'custom',
      icon: null,
      enabled: true,
    },
  ];

  constructor(props) {
    super(props);
    this.state = {
      selectedArtifactRegistryType: null,
    };
  }

  getRows() {
    return ChooseArtifactRegistryTypeDialog.atrifactRepoTypes.map(type => (
      <tr
        key={type.name}
        className={type.enabled ? styles.row : `${styles.row} ${styles.disabledRow}`}
        onClick={() => this.setState({ selectedArtifactRegistryType: type.name })}
      >
        <td className={styles.checkboxContainer}>
          <Checkbox selected={this.state.selectedArtifactRegistryType === type.name} />
        </td>
        <td className={styles.logoContainer}>
          <div className={styles.logoCircle}>
            <img
              src={type.icon}
              className={styles.logoImage}
              alt=""
            />
          </div>
        </td>
        <td className={styles.name}>
          {type.name}
        </td>
      </tr>
    ));
  }

  render() {
    return (
      <div className={styles.ChooseArtifactRegistryTypeLightbox}>
        <div className={styles.inside}>
          <table>
            <tbody>
              { this.getRows() }
            </tbody>
          </table>
        </div>
        <div className={styles.footer}>
          <Button
            onClick={this.props.onDismiss}
            text="cancel"
          />
          <Button
            onClick={() => this.props.onSubmit(this.state.selectedArtifactRegistryType)}
            primary
            text="next"
          />
        </div>
      </div>
    );
  }
}

const ChooseArtifactRegistryTypeLightbox = props => (
  <div className={styles.ChooseArtifactRegistryTypeLightbox}>
    <LightBox
      title="Choose artifact repository type"
      component={<ChooseArtifactRegistryTypeDialog
        onDismiss={props.onDismiss}
        onSubmit={props.onSubmit}
      />}
      width={450}
      height={480}
      onDismiss={props.onDismiss}
    />
  </div>
);

ChooseArtifactRegistryTypeLightbox.propTypes = {
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default ChooseArtifactRegistryTypeLightbox;
