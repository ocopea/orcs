// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { LightBox, Button } from '../..';
import styles from './styles-add-artifact-registry-lightbox.scss';

class AddArtifactRegistryDialog extends React.Component {

  static propTypes = {
    appRepoType: React.PropTypes.string.isRequired,
    onDismiss: React.PropTypes.func.isRequired,
    onSubmit: React.PropTypes.func.isRequired,
  }

  constructor(props) {
    super(props);
    this.changeName = this.changeName.bind(this);
    this.changeUrl = this.changeUrl.bind(this);
    this.changeUsername = this.changeUsername.bind(this);
    this.changePassword = this.changePassword.bind(this);
    this.state = {
      name: '',
      url: '',
      username: '',
      password: '',
    };
  }

  changeName(e) {
    this.setState({
      name: e.target.value,
    });
  }

  changeUrl(e) {
    this.setState({
      url: e.target.value,
    });
  }

  changeUsername(e) {
    this.setState({
      username: e.target.value,
    });
  }

  changePassword(e) {
    this.setState({
      password: e.target.value,
    });
  }

  render() {
    return (
      <div className={styles.AddArtifactRegistryDialog}>
        <div className={styles.inside}>

          <section>
            <label htmlFor="name">Name</label>
            <input
              id="name"
              value={this.state.name}
              className="input"
              type="text"
              onChange={this.changeName}
            />
          </section>

          <section>
            <label htmlFor="url">URL</label>
            <input
              id="url"
              value={this.state.url}
              className="input"
              type="text"
              onChange={this.changeUrl}
            />
          </section>

          {
            this.props.appRepoType === 'maven' ?
              <section>
                <label htmlFor="username">Username</label>
                <input
                  id="username"
                  value={this.state.username}
                  className="input"
                  type="text"
                  onChange={this.changeUsername}
                />
              </section> :
              null
          }
          {
            this.props.appRepoType === 'maven' ?
              <section>
                <label htmlFor="password">Password</label>
                <input
                  id="password"
                  value={this.state.password}
                  className="input"
                  type="password"
                  onChange={this.changePassword}
                />
              </section> :
              null
          }

          <div className={styles.footer}>
            <Button onClick={this.props.onDismiss} text="cancel" />
            <Button onClick={this.props.onSubmit} primary text="save" />
          </div>
        </div>
      </div>
    );
  }
}

const AddArtifactRegistryLightbox = props => (
  <div className={styles.AddArtifactRegistryDialog}>
    <LightBox
      title="Add Artifact Repository"
      component={
        <AddArtifactRegistryDialog
          onDismiss={props.onDismiss}
          onSubmit={props.onSubmit}
          appRepoType={props.appRepoType}
        />
      }
      width={450}
      height={480}
      onDismiss={props.onDismiss}
    />
  </div>
);

AddArtifactRegistryLightbox.propTypes = {
  appRepoType: React.PropTypes.string.isRequired,
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default AddArtifactRegistryLightbox;
