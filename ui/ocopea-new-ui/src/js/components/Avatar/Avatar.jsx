import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-avatar.scss';


export default class Avatar extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    src: React.PropTypes.string.isRequired,
    style: React.PropTypes.object,
  }

  render(){

    const { t, style, src } = this.props;

    return(
      <img id={this.props.id} className={styles.Avatar} style={style} src={src} />
    )
  }

}
