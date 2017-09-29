// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../../actions/site-config-actions.js';


const AddDsbDialog = React.createClass({
  render() {
    const baseClass = 'Add-dsb-dialog'
    return (
      <div className={baseClass}>
        {/* title */}
        <div className={`${baseClass}__title`}>
          <span>add data service</span>
          {/* close dialog btn */}
          <span
            onClick={this.closeDialog}
            className={`${baseClass}__title__icon-close icon-close link`}>
              <span></span>
              <span></span>
          </span>
        </div>

        {/* inside */}
        <div className={`${baseClass}__inside`}>
          {/* urn */}
          <section>
            <label>urn</label>
            <input value={this.state.urn} className='input' type="text" onChange={this.changeUrn} />
          </section>
          {/* url */}
          <section>
            <label>url</label>
            <input value={this.state.url} className='input' type="text" onChange={this.changeUrl} />
          </section>

          <div className={`${baseClass}__inside__footer`}>
            <button
              onClick={this.onSubmit}
              className={`${baseClass}__inside__footer__button button button-secondary`}>submit</button>
            <button
              onClick={this.onDismiss}
              className={`${baseClass}__inside__footer__button button button-primary`}>cancel</button>
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

  onSubmit() {    
    SiteConfigActions.addDsb(this.state.urn, this.state.url)
  },

  onDismiss() {
    SiteConfigActions.showAddDsbDialog(false);
  },

  changeUrn(e) {
    this.setState({
      urn: e.target.value
    });
  },

  changeUrl(e) {
    this.setState({
      url: e.target.value
    });
  },

  closeDialog() {
    console.log('close')
    SiteConfigActions.showAddDsbDialog(false);
  }
});

export default AddDsbDialog
