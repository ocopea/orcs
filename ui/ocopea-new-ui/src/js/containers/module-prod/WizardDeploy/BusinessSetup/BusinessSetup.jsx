import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-business-setup.scss';
import stylesWizard from '../styles-wizard-deploy-prod.scss';
import plans from './plans.json';
import PlanVisualization from './PlanVisualization.jsx';


@inject(["stores"])
@observer
export default class BusinessSetup extends React.Component{

  constructor(props){
    super(props)
    this.state = {
      selected: {}
    }
  }

  render(){

    const {  } = this.props;

    return(
      <div className={styles.BusinessSetup}>
        <div className={stylesWizard.title}>business setup</div>
        <div className={stylesWizard.subtitle}>subtitle</div>
          <div className={styles.cards}>
          {
            plans.map(plan => {
              const isSelected = plan.name === this.state.selected.name;
              return <div className={styles.card}>
                <div
                  onClick={this.onPlanSelection.bind(this, plan)}
                  className={!isSelected ? `${styles.inside} card-hover` :
                  `${styles.inside} card-selected`}>

                  <div className={styles.details}>details ></div>
                  <div className={styles.logo}>
                    <img src={require(`./assets/${plan.img}`)} />
                  </div>
                  <div className={styles.name}>{plan.name}</div>
                  <div className={styles.description}>{plan.description}</div>
                  <PlanVisualization data={{...plan}}/>
                </div>
              </div>
            })
          }
        </div>
      </div>
    )
  }

  onPlanSelection(plan) {
    this.props.onBusinessPlanSelection(plan);
    this.setState({
      selected: plan
    });
  }

}
