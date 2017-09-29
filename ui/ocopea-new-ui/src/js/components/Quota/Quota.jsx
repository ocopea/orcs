import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-quota.scss';
import { ProgressBar } from '../';
import uuid from 'uuid';


export default class Quota extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    imgUrl: React.PropTypes.string,
    name: React.PropTypes.string.isRequired,
    precent: React.PropTypes.number.isRequired
  }

  render(){

    const { t } = this.props;

    return(
      <div style={this.props.style} className={styles.Quota}>
        <div
          style={{background:this.props.iconFill}}
          className={styles.icon}>
            {this.props.icon}
        </div>
        <div
          style={{WebkitBoxOrient: "vertical"}}
          className={styles.rightContainer}>
          <div
            title={this.props.name}
            className={styles.label}>{this.props.name}</div>
          <ProgressBar
            id={this.props.id}
            precent={this.props.precent}
            instanceName={this.props.name}
            progressBarFill={this.props.progressBarFill}/>
        </div>
      </div>
    )
  }

}
