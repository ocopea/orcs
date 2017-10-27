// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { LightBox, Button } from '../..';
import styles from './styles-add-crb-lightbox.scss';

class AddCrbDialog extends React.Component {

  static propTypes = {
    onDismiss: React.PropTypes.func.isRequired,
    onSubmit: React.PropTypes.func.isRequired,
  }

  constructor(props) {
    super(props);
    this.changeUrn = this.changeUrn.bind(this);
    this.changeUrl = this.changeUrl.bind(this);
    this.state = {
      urn: '',
      url: '',
    };
  }

  changeUrn(e) {
    this.setState({
      urn: e.target.value,
    });
  }

  changeUrl(e) {
    this.setState({
      url: e.target.value,
    });
  }

  render() {
    return (
      <div className={styles.AddCrbDialog}>
        <div className={styles.inside}>

          <section>
            <label htmlFor="urn">URN</label>
            <input
              id="urn"
              value={this.state.urn}
              className="input"
              type="text"
              onChange={this.changeUrn}
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

          <div className={styles.footer}>
            <Button
              onClick={this.props.onDismiss}
              text="cancel"
            />
            <Button
              onClick={this.props.onSubmit}
              primary
              text="add"
            />
          </div>
        </div>
      </div>
    );
  }
}

const AddCrbLightbox = props => (
  <div className={styles.AddCrbLightbox}>
    <LightBox
      title="Add Copy Repository"
      component={<AddCrbDialog onDismiss={props.onDismiss} onSubmit={props.onSubmit} />}
      width={456}
      height={390}
      onDismiss={props.onDismiss}
    />
  </div>
);

AddCrbLightbox.propTypes = {
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default AddCrbLightbox;
