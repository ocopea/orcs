import React from 'react';
import styles from './styles-dsb-plan-tooltip.scss';

export default class DsbPlanTooltip extends React.Component {
  render() {
    const plan = this.props.selectedPlan;
    return (
      <div className={styles.DsbPlanTooltip}>
        <div className={styles.title}>{plan.name}</div>
        <ul className={styles.description}>
          <li>{plan.description}</li>
        </ul>
      </div>
    )
  }
}
