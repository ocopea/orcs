import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-instance-cards.scss';
import { InstanceCard } from '../';


@inject(["stores"])
export default class InstanceCards extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, list } = this.props;

    return(
      <div className={styles.Cards}>
        {
          list ?
            list.map((item, i) => {
              return <InstanceCard key={i} instance={item}/>
            })
          : null
        }
      </div>
    )
  }

}
