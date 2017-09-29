import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-main-menu.scss';
import modules from '../../utils/modules.json';
import DevMainMenu from './dev/DevMainMenu.jsx';
import ProdMainMenu from './prod/ProdMainMenu.jsx';


@inject(["stores"])
@observer
export default class MainMenu extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, stores } = this.props;
    return(
      <div className={styles.MainMenu}>
        {
          stores.ui.module === modules.development ?
            <DevMainMenu />
          :
          stores.ui.module === modules.production ?
            <ProdMainMenu />
          : null
        }
      </div>
    )
  }

}
