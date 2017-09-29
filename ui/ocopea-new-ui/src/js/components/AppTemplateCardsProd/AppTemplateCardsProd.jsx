import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-appTemplate-cards-prod.scss';
import { AppTemplateCardProd } from '../';


@inject(["stores"])
@observer
export default class AppTemplateCards extends React.Component{

  constructor(props){
    super(props)    
  }

  static propTypes = {
    appTemplates: React.PropTypes.array.isRequired
  }

  render(){

    const { appTemplates } = this.props;

    return(
      <div className={styles.AppTemplateCards}>
        {
          appTemplates.map(appTemplate => {
            return (
              <AppTemplateCardProd
                selectedAppTemplate={this.props.selectedAppTemplate}
                onClick={this.props.onSetSelectedAppTemplate.bind(this)}
                key={appTemplate.id}
                appTemplate={appTemplate} />
            )
          })
        }
      </div>
    )
  }

}
