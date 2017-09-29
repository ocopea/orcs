import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-statistics.scss';
import { Pie } from '../';
import _ from 'lodash';



@observer
export default class Statistics extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    copySummary: React.PropTypes.object.isRequired
  }

  render(){

    const {  } = this.props;
    var data = this.formatData();
    return(
      <div className={styles.Statistics}>
        <div className="title">statistics</div>        
        <Pie data={data}/>
      </div>
    )
  }

  formatData() {
      return _.map(this.props.copySummary, (value, key) => {
          let obj = {};
          obj.name = key;
          obj.value = value;
          return obj;
      })      
  }

}