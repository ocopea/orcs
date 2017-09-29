import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-activities.scss';
import ImgMock from './mock-quota-summary.png';


@inject(["stores"])
@observer
export default class Activities extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const {  } = this.props;

    return(
      <div className={styles.Activities}>
        <div className='title'>Activities</div>
        <div className={styles.inside}>
          <img src={ImgMock} />
        </div>
      </div>
    )
  }

}
