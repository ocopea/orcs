// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-app-template-list.scss';
import { Avatar } from '../';

@inject(["stores"])
export default class AppTemplateList extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

      const { appTemplatesList } = this.props;

      return(
          <div className={styles.AppTemplateList}>
              <div className={styles.appTemplateListTitle}>
                  Application Templates
              </div>
              <div className={styles.appTemplateListSubTitle}>
                  Available application templates
              </div>
              <table>
                  <thead>
                  <tr>
                      <th>name</th>
                  </tr>
                  </thead>
                  <tbody>
                  {
                      appTemplatesList ?
                          appTemplatesList.map(currTemplate => {
                              const appTemplateImg = currTemplate ? APISERVER + currTemplate.img : null;

                              return (
                                  <tr key={currTemplate.id}>
                                      <td>
                                          <Avatar id="appTemplate-img" src={appTemplateImg} style={{width: 30, height: 30}}/>
                                          <span className={styles.appTemplateName}>{currTemplate.name}</span>
                                      </td>
                                  </tr>
                              )
                          })
                          : null
                  }
                  </tbody>
              </table>
          </div>
      );
  }
}
