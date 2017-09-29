import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-instance-card-prod.scss';
// import userHandler from '../../models/user/user-handler';


@inject(["stores"])
@observer
export default class Sumup extends React.Component{

  render(){

    const { t } = this.props;

    return(
      <div className={styles.Sumup}>
        <section>
          <div className={styles.number}>{this.props.productionInstances}</div>
          <div className={styles.description}>production instance</div>
        </section>
        <section>
          <div className={styles.number}>{this.props.nonProductionInstances}</div>
          <div className={styles.description}>non-production instance</div>
        </section>
        <section>
          <div className={styles.number}>{this.props.offLineCopies}</div>
          <div className={styles.description}>offline copies</div>
        </section>
      </div>
    )
  }

  constructor(props){
    super(props)
  }

  static propTypes = {
    productionInstances: React.PropTypes.number.isRequired,
    nonProductionInstances: React.PropTypes.number.isRequired,
    offLineCopies: React.PropTypes.number.isRequired
  }

}
