import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-instance-cards-prod.scss';
import { InstanceCardProd } from '../';
import Locations from '../../locations.json';
import { hashHistory } from 'react-router';


@inject(["stores"])
@observer
export default class InstanceCardsProd extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    list: React.PropTypes.array.isRequired
  }

  render(){

    const { t, list } = this.props;

    return(
      <div className={styles.InstanceCardsProd}>
        {
          list ?
            list.map((item, i) => {
              return <InstanceCardProd key={i} instance={item} onCardClick={this.onCardClick} />
            })
          : null
        }
      </div>
    )
  }

  onCardClick(instance) {
    const instanceID = instance.id;
    hashHistory.push(`${Locations.production.dashboard.pathname}/${instanceID}`)
  }

}
