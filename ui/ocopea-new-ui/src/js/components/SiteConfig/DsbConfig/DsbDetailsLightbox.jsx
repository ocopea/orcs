// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { LightBox, Button, SearchSelect } from '../..';
import DsbPlanProtocols from './DsbPlanProtocols.jsx';
import DsbPlanTooltip from './DsbPlanTooltip.jsx';

import styles from './styles-dsb-details-lightbox.scss';

class DsbDetailsDialog extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      selectedPlan: props.dsbInfo.plans[0],
      showPlanTooltip: false,
    };
    this.userClickOnInfo = this.userClickOnInfo.bind(this);
    this.hidePlanTooltip = this.hidePlanTooltip.bind(this);
  }

  render() {
    const dsbInfo = this.props.dsbInfo;
    const baseClass = '';

    return (
      <div className={styles.DsbDetailsDialog}>
        <div className={styles.header}>
          <span className={styles.logoContainer}>
            <img src={`${APISERVER}${dsbInfo.img}`} className={styles.logo}/>
          </span>
          <span className={styles.dsbName}>{dsbInfo.name}</span>
        </div>
        {/* /.header */}
        <div className={styles.title}>description</div>
        <div className={styles.description}>{dsbInfo.description}</div>
        {/* /.description */}
        <div className={styles.title}>plans</div>
        <div className={styles.plansSelectContainer}>
          <SearchSelect
            placeholder={this.state.selectedPlan.name}
            options={dsbInfo.plans.map(plan => { return {value: plan.name, name: plan.name}; })}
            searchable={false}
            className={styles.plansSelect}
            onChange={selection => {
              const selectedPlan = dsbInfo.plans.find(plan => plan.name === selection.value);
              this.setState({ selectedPlan });
            }}
          />
          <span onClick={this.userClickOnInfo} className={`icon-info ${styles.iconInfo}`}></span>
        </div>
        {/* /.plans */}
        {
          this.state.selectedPlan.protocols.length > 0 ?
            <div>
              <div className={styles.title}>Supported Protocols</div>
              <DsbPlanProtocols protocols={this.state.selectedPlan.protocols}/>
            </div>
            : null
        }
        {/* /.protocols */}
        <div className={styles.footer}>
          <Button onClick={this.props.onDismiss} text='cancel' />
          <Button onClick={this.props.onSubmit} primary text='save' />
        </div>
        {
          this.state.showPlanTooltip ?
            <DsbPlanTooltip selectedPlan={this.state.selectedPlan} />
            : null
        }
      </div>
    );
  }

  userClickOnInfo() {
    this.setState({ showPlanTooltip: true });
  }

  componentDidMount() {
    document.addEventListener('click', this.hidePlanTooltip);
  }

  hidePlanTooltip(e) {
    if(!e.target.classList.contains('icon-info')){
      this.setState({ showPlanTooltip: false });
    }
  }

  getPlansOptions(baseClass) {
    return _.map(this.props.dsbInfo.plans, (plan, i)=>{
      return {value: plan.name, label: plan.name}
    });
  }

  userSelectedPlan(plan) {
    // console.log(plan)
    this.setState({
      selectedPlan: this.state.plans[plan.name]
    });
  }

  userClickOnDismissDialog() {
    //SiteConfigActions.hideDsbDialog();
    //SharedActions.navigate({
    //module: DevNavigationOptions.module,
    //location: DevNavigationOptions.siteConfig.location,
    //subLocation: ''
    //});
  }
}

const DsbDetailsLightBox = props => (
  <div className={styles.DsbDetailsLightBox}>
    <LightBox
      title="Data Service Broker Details"
      component={<DsbDetailsDialog dsbInfo={props.dsbInfo} onDismiss={props.onDismiss} onSubmit={props.onSubmit}/>}
      width={604}
      height={627}
      onDismiss={props.onDismiss}
    />
  </div>
);

DsbDetailsLightBox.propTypes = {
  onDismiss: React.PropTypes.func.isRequired,
  onSubmit: React.PropTypes.func.isRequired,
};

export default DsbDetailsLightBox;
