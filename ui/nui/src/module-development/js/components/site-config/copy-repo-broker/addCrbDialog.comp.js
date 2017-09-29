// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../../actions/site-config-actions.js';


const AddCrbDialog = React.createClass({

  render() {
    const baseClass = "Add-crb-dialog";

    return(
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>
          <span>add copy repo</span>
          <span
            onClick={this.onDismiss}
            className={`${baseClass}__title__icon-close icon-close link`}>
              <span></span>
              <span></span>
          </span>
        </div>
        <div className={`${baseClass}__inside`}>
          <section>
            <label>urn</label>
            <input className="input" type="text" onChange={this.changeUrn} />
          </section>
          <section>
            <label>url</label>
            <input className="input" type="text" onChange={this.changeUrl} />
          </section>
          <div className={`${baseClass}__inside__footer`}>
            <button onClick={this.onDismiss} type="button" className="button button-secondary">cancel</button>
            <button onClick={this.onSubmit} type="button" className="button button-primary">add</button>
          </div>
        </div>
      </div>
    )
  },

  getInitialState() {
    return {
      urn: '',
      url: ''
    }
  },

  changeUrn(e) {
    this.setState({
      urn: e.target.value
    })
  },

  changeUrl(e) {
    this.setState({
      url: e.target.value
    })
  },

  onDismiss() {
    SiteConfigActions.showAddCrbDialog(false);
  },

  onSubmit() {
    SiteConfigActions.addCrb(this.state.urn, this.state.url);
  }
});

export default AddCrbDialog;
