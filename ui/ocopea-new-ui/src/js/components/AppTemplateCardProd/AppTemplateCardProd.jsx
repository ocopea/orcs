import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-appTemplate-card-prod.scss';
// import userHandler from '../../models/user/user-handler';


@inject(["stores"])
@observer
export default class AppTemplateCard extends React.Component{

  constructor(props){
    super(props)
  }

  static propTypes = {
    appTemplate: React.PropTypes.object.isRequired
  }

  render(){

    const { appTemplate } = this.props;
    const isSelected = this.props.selectedAppTemplate.id === appTemplate.id;

    return(
      <div
        className={!isSelected ? styles.AppTemplateCard : styles.AppTemplateCardSelected}
        onClick={this.onClick.bind(this, appTemplate)}>
        <div className={styles.logo}>
          <img src={APISERVER+appTemplate.img} />
        </div>
        <section>
          <div className={styles.appName}>
            {appTemplate.name}
          </div>
          <div className={styles.description}>
            {appTemplate.description}
          </div>
        </section>
      </div>
    )
  }

  onClick(appTemplate) {
    this.props.onClick(appTemplate);
  }

}
