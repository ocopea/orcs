import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-dashboard.scss';


@inject(["stores"])
@observer
export default class TopContainer extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, instance } = this.props;
    console.log(instance)
    return(
      <div className={styles.TopContainer}>
        top container
      </div>
    )
  }

}
