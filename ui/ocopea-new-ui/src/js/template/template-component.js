// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
// import styles from './styles-home.scss';
// import userHandler from '../../models/user/user-handler';


@inject(["stores"])
@observer
export default class Component extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const {  } = this.props;

    return(
      <div className={styles.Component}></div>
    )
  }

}
