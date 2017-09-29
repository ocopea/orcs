// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import IconUnkown from '../../../../assets/icon-unknown.png';
import SharedActions from '../../../../../shared-actions.js';
import SiteConfigActions from '../../../actions/site-config-actions.js';
import DevNavigationOptions from '../../../data/devNavigationOptions.js';


let DataServiceBroker = React.createClass({

  render() {
    const baseClass = "Data-service-broker";
    // console.log(this.props.selectedPlan.protocols)
    return(
      <div className={baseClass}>
        {
          this.props.dsbs.map((dsb, i)=>{
            // console.log(dsb)
              return (
                <div key={i} className={`${baseClass}__row`}>
                  <div className={`${baseClass}__row__logo-container`}>
                    <img
                      src={dsb.img ? APISERVER+dsb.img : IconUnkown}
                      className={`${baseClass}__row__logo-container__logo`}/>
                  </div>
                  <div className={`${baseClass}__row__name`}>
                    {dsb.name}
                  </div>
                  <div className={`${baseClass}__row__description`}>
                    {dsb.description}
                  </div>
                  <div className={`${baseClass}__row__icons`}>
                    <div className={`${baseClass}__row__icon link`} onClick={this.userClickOnInfo.bind(this, dsb.urn)}>
                      <span className="icon-info"></span>
                    </div>
                    <div
                      onClick={this.deleteDsb.bind(this, dsb)}
                       className={`${baseClass}__row__icon link`}>
                      <span className="icon-delete"></span>
                    </div>
                  </div>
                </div>
              )
          })
        }

        <div
         onClick={this.addArtifact}
         className={`${baseClass}__row ${baseClass}__row--last`}>
          <span
            className={`${baseClass}__row--last__logo Plus`}>
            <span></span>
            <span></span>
          </span>
          <span className={`${baseClass}__row--last__name`}>Add New</span>
        </div>

      </div>
    )
  },

  deleteDsb(dsb) {
    SiteConfigActions.showConfirmRemoveDsbDialog(true, dsb)
  },

  addArtifact() {
    SiteConfigActions.showAddDsbDialog(true);
  },

  userClickOnInfo(urn) {
    SiteConfigActions.showDsbDialog(urn);
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: DevNavigationOptions.siteConfig.subLocation.dsbDetailsDialog
    });
  }

});

export default DataServiceBroker;
