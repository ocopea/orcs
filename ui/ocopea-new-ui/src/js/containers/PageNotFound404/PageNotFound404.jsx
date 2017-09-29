import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-page-not-found-404.scss';
import Image from './404.png';

@inject(["stores"])
@translate(['general'], { wait: true })
export default class PageNotFound404 extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t } = this.props;

    return(
      <div className={styles.PageNotFound}>
        <img src={Image} />
      </div>
    )
  }

}
