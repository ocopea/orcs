import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-button-dismiss.scss';


@observer
export default class ButtonDismiss extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, onClick } = this.props;
    const color = {background: this.props.color};

    return(
      <div
        style={this.props.style}
        onClick={onClick}
        className={styles.buttonDismiss}>
          <span style={color}></span>
          <span style={color}></span>
      </div>
    )
  }

}
