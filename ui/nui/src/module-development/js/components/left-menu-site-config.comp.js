// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import siteConfigActions from '../actions/site-config-actions.js';
import _ from 'lodash';

let SiteConfigLeftMenu = React.createClass({
  render() {
    const baseClass = "Site-config__left-menu";
    return(
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>site setup</div>
        <ul>
          {
            _.map(this.props.sites, (site, key)=>{
              let selectedSiteId = this.props.selectedSite ? this.props.selectedSite.id : null;
              let clsasName = selectedSiteId === site.id ?
                              `${baseClass}__list-item ${baseClass}__list-item--selected` :
                              `${baseClass}__list-item`;
              return  <li
                        className={clsasName}
                        onClick={this.userSelectedSite.bind(this, site.id)}
                        key={key}>
                          {site.name}
                      </li>
            })
          }
        </ul>
      </div>
    )
  },

  userSelectedSite(siteId) {
    siteConfigActions.userSelectedSite(siteId)
  }

});

export default SiteConfigLeftMenu;
