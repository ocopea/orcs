// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { LightBox, Button } from '../..';
import styles from './styles-add-copy-repo-lightbox.scss';

class AddCopyRepoDialog extends React.Component {

  static propTypes = {
    onDismiss: React.PropTypes.func.isRequired,
    onSubmit: React.PropTypes.func.isRequired,
  }

  constructor(props) {
    super(props);
    this.changeUser = this.changeUser.bind(this);
    this.changePassword = this.changePassword.bind(this);
    this.changeIp = this.changeIp.bind(this);
    this.state = {
    };
  }

  changeUser(e) {
    this.setState({
      user: e.target.value,
    });
  }

  changePassword(e) {
    this.setState({
      password: e.target.value,
    });
  }

  changeIp(e) {
    this.setState({
      ip: e.target.value,
    });
  }

  render() {
    return (
      <div className={styles.AddCopyRepoDialog}>
        <div className={styles.inside}>

          <section>
            <label htmlFor="user">user</label>
            <input
              id="user"
              value={this.state.user}
              className="input"
              type="text"
              onChange={this.changeUser}
            />
          </section>

          <section>
            <label htmlFor="password">password</label>
            <input
              id="password"
              value={this.state.password}
              className="input"
              type="password"
              onChange={this.changePassword}
            />
          </section>

          <section>
            <label htmlFor="ip">IP</label>
            <input
              id="ip"
              value={this.state.ip}
              className="input"
              type="text"
              onChange={this.changeIp}
            />
          </section>

          <div className={styles.footer}>
            <Button
              onClick={this.props.onDismiss}
              text="cancel"
            />
            <Button
              onClick={() => this.props.onSubmit(this.state.user, this.state.password)}
              primary
              text="add"
            />
          </div>
        </div>
      </div>
    );
  }
}

const AddCopyRepoLightbox = props => (
  <div className={styles.AddCopyRepoLightbox}>
    <LightBox
      title="Add Copy Repository"
      component={
        <AddCopyRepoDialog
          onDismiss={props.onDismiss}
          onSubmit={props.onSubmit}
        />}
      width={456}
      height={390}
      onDismiss={props.onDismiss}
    />
  </div>
);

AddCopyRepoLightbox.propTypes = {
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default AddCopyRepoLightbox;
