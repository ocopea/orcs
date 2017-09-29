import React from 'react';
import { LightBox, Button } from '../..';
import styles from './styles-add-dsb-lightbox.scss';

class AddDsbDialog extends React.Component {

  static propTypes = {
    onDismiss: React.PropTypes.func.isRequired,
    onSubmit: React.PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.changeUrl = this.changeUrl.bind(this);
    this.changeUrn = this.changeUrn.bind(this);
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
      <div className={styles.AddDsbLightbox}>
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
              text="submit"
            />
          </div>
        </div>
      </div>
    );
  }
}

const AddDsbLightbox = props => (
  <div className={styles.AddDsbLightbox}>
    <LightBox
      title="Add Data Service"
      component={<AddDsbDialog onDismiss={props.onDismiss} onSubmit={props.onSubmit} />}
      width={456}
      height={390}
      onDismiss={props.onDismiss}
    />
  </div>
);

AddDsbLightbox.propTypes = {
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default AddDsbLightbox;
