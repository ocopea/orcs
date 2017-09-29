// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../../actions/site-config-actions.js';
import SharedActions from '../../../../../shared-actions.js';
import DevNavigationOptions from '../../../data/devNavigationOptions.js';
import Protocols from './protocols.comp.js';
import NazSelect from '../../shared-image/shared-image-select.comp.js';
import DsbInfoMenu from './dsb-info-menu.comp.js';
import _ from 'lodash';


let DsbDetailsDialog = React.createClass({

  render() {

    const baseClass = "Dsb-details-dialog";
    const selectedDsb = this.props.selectedDsb;

    return (
      <div className={baseClass}>
        <div className={`${baseClass}__title`}>
          <span>details</span>
          <span
           onClick={this.userClickOnDismissDialog}
           className={`${baseClass}__title__icon-close icon-close link`}>
            <span></span>
            <span></span>
          </span>
        {/* ./title */}
        </div>
        <div className={`${baseClass}__inside`}>
          <div
            className={`${baseClass}__inside__section ${baseClass}__inside__section--header`}>
            <div className={`${baseClass}__inside__section--header__inside`}>
              <span className={`${baseClass}__inside__section--header__inside__img-container`}>
                <img
                  src={`${APISERVER}${selectedDsb.img}`}
                  className={`${baseClass}__inside__section--header__inside__img-container__img`}/>
              </span>
              <span className={`${baseClass}__inside__section--header__inside__title`}>{selectedDsb.name}</span>
            </div>
          </div>
          {/* /.header */}
          <div
            className={`${baseClass}__inside__section ${baseClass}__inside__section--description`}>
            <div className={`${baseClass}__inside__section--description__inside`}>
              <div className={`${baseClass}__inside__section--description__inside__title`}>description</div>
              <div className={`${baseClass}__inside__section--description__inside__content`}>{selectedDsb.description}</div>
            </div>
          </div>
          {/* /.description */}
          <div className={`${baseClass}__inside__section ${baseClass}__inside__section--plans`}>
            <div className={`${baseClass}__inside__section--plans__title`}>plans</div>
            <div className={`${baseClass}__inside__section--plans__select-container`}>
              <NazSelect
                placeholder={null}
                type="site-config-dsb-plan"
                options={this.getPlansOptions()}/>
              <span
                onClick={this.userClickOnInfo}
                className={`${baseClass}__inside__section--plans__select-container__icon-info icon-info link info-menu`}></span>
            </div>
          </div>
          {/* /.plans */}
          {
            !_.isEmpty(this.props.selectedPlan.protocols) ?
              <div className={`${baseClass}__inside__section ${baseClass}__inside__section--protocols`}>
                <Protocols protocols={this.props.selectedPlan.protocols}/>
              </div>
            : null
          }
          {/* /.protocols */}
          <div className={`${baseClass}__footer`}>
            <button
              onClick={this.userclickOnSave}
              className={`${baseClass}__footer__button ${baseClass}__footer__button--save button button-primary`}>save</button>
            <button
              onClick={this.userclickOnCancel}
              className={`${baseClass}__footer__button button button-secondary`}>cancel</button>
          </div>
        </div>
      {/* ./inside */}
      {
        this.props.infoMenu.isRender ?
          <DsbInfoMenu selectedPlan={this.props.selectedPlan}/>
        : null
      }
      </div>
    )
  },

  userclickOnCancel() {
    SiteConfigActions.hideDsbDialog();
  },

  userclickOnSave() {

  },

  getInitialState() {
    let plans = {};
    _.forEach(this.props.selectedDsb.plans, plan=>{
      plans[plan.name] = plan;
    });

    SiteConfigActions.setSelectedDsbPlan(this.props.selectedDsb.plans[0].name)
    return {
      selectedPlan: this.props.selectedDsb.plans[0],
      plans: plans
    }
  },

  componentDidMount() {
    document.addEventListener('click', this.removeMenu);
  },

  userClickOnInfo() {
    SiteConfigActions.showDsbPlanInfoMenu();
  },

  removeMenu(e) {
    if(!e.target.classList.contains('info-menu')){
      SiteConfigActions.hideDsbPlanInfoMenu();
    }
  },

  componentWillUnmount() {
    document.removeEventListener('click', this.removeMenu);
  },

  getPlansOptions(baseClass) {
    return _.map(this.props.selectedDsb.plans, (plan, i)=>{
      return {value: plan.name, label: plan.name}
    });
  },

  userSelectedPlan(plan) {
    // console.log(plan)
    this.setState({
      selectedPlan: this.state.plans[plan.name]
    });
  },

  userClickOnDismissDialog() {
    SiteConfigActions.hideDsbDialog();
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: ''
    });
  }

});

export default DsbDetailsDialog;
