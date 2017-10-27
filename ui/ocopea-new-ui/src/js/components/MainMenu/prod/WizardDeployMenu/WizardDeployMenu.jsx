// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-wizard-deploy-menu.scss';
import _ from 'lodash';
import { Checkbox, IconArrow } from '../../../';
import Helper from '../../../../utils/helper';
import { hashHistory } from 'react-router';


@inject(["stores"])
@observer
export default class WizardDeployMenu extends React.Component{

  constructor(props){
    super(props)

    this.state = {
      currentStep: this.props.stores.ui.currentLocation
    }
  }

  componentWillReceiveProps() {
    this.setState({
      currentStep: this.props.stores.ui.currentLocation
    })
  }

  static propTypes = {
    steps: React.PropTypes.object.isRequired
  }

  render(){

    const { steps } = this.props;

    return(
      <div className={styles.WizardDeployMenu}>
        {
          _.map(steps, step => {
            const isSelected = step.key === this.state.currentStep.key;
            const isComplete = this.isComplete(step);

            return <div className={styles.row}>
              {
                isComplete ?
                  <Checkbox
                    selected={true}
                    width={18}
                    height={18}
                    checkSize={10}
                    hideBorder={true}
                    checkColor={'#fff'} /> :
                isSelected ? <IconArrow /> :
                <span id="circle" className={styles.circle}></span>
              }
              <span className={
                  isComplete ? styles.labelCompleted :
                  isSelected ? styles.labelSelected : null}>{step.name}</span>
            </div>
          })
        }
      </div>
    )
  }

  isComplete(step) {
    const keys = _.keys(this.props.steps);
    const stepKey = step.key;
    const currentStepKey = this.state.currentStep.key;

    const currentStepIndex = keys.indexOf(currentStepKey);
    const stepIndex = keys.indexOf(stepKey);
    return stepIndex < currentStepIndex;
  }

}
