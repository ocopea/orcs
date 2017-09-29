// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import SiteConfigActions from '../../../actions/site-config-actions.js';
import MockData from './mockData.js';
import IconUnknown from '../../../../assets/icon-unknown.png';
import SharedActions from '../../../../../shared-actions.js';
import DevNavigationOptions from '../../../data/devNavigationOptions.js';
import _ from 'lodash';


let CopyRepoBroker = React.createClass({
  render() {
    const baseClass = 'Copy-repo-broker';

    return(
      <div className={baseClass}>
        {
          this.props.copyRepos.map((repo, i)=>{

            let isCollapsed = this.state.collapsed.indexOf(i) > -1;
            const isLast = i === this.props.copyRepos.length - 1;

            return (
              <div key={i} >
                <div
                  id={i}
                  onClick={this.toggle.bind(this, i)}
                  className={this.getRowClass(isCollapsed, isLast, baseClass)} >
                    <div className={`${baseClass}__row__logo-container`}>
                      <img
                        src={repo.img || IconUnknown}
                        className={`${baseClass}__row__logo-container__logo`}/>
                    </div>
                    <div className={`${baseClass}__row__name`}>{repo.name}</div>
                    <div className={`${baseClass}__row__description`}>{repo.description}</div>

                    <div className={`${baseClass}__row__icons`}>
                      <div
                        onClick={this.deleteCrb.bind(this, repo)}
                         className={`${baseClass}__row__icon link`}>
                        <span className="icon-delete"></span>
                      </div>
                    </div>
                </div>
                <div
                  className={
                    isCollapsed ?
                    `${baseClass}__row ${baseClass}__row__additional--collapsed ` +
                      `${baseClass}__row ${baseClass}__row__additional` :
                    `${baseClass}__row ${baseClass}__row__additional`
                  }>
                  {
                    this.getTable(MockData)
                  }
                  <div className={`Copy-repo-broker__row__additional__add-cr`}>
                    <button
                      className="button button-primary"
                      onClick={this.addCopyRepo.bind(this, repo)}>add cr</button>
                  </div>
                </div>
              </div>
            )
          })
        }

        <div
         onClick={this.addCrb}
         className={`${baseClass}__row ${baseClass}__row--last`}>
          <span
            className={`${baseClass}__row--last__logo Plus`}>
            <span></span>
            <span></span>
          </span>
          <span className={`${baseClass}__row--last__name`}>Add New</span>
        </div>

      </div>
    )
  },

  getInitialState() {
    return {
      collapsed: [],
      selectedCopyRepo: {}
    }
  },

  addCopyRepo(repo) {    
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.siteConfig.location,
      subLocation: DevNavigationOptions.siteConfig.subLocation.addCr
    });
    SiteConfigActions.showAddCrDialog(true, repo);
  },

  deleteCrb(crb) {
    SiteConfigActions.showConfirmDeleteCrbDialog(true, crb);
  },

  addCrb() {
    SiteConfigActions.showAddCrbDialog(true);
  },

  getRowClass(isCollapsed, isLast, baseClass) {
    let className;
    if(isCollapsed){
      if(isLast){
          className =  `${baseClass}__row `+
                       `${baseClass}__row--last ` +
                       `${baseClass}__row--collapsed`;
      }else{
          className =  `${baseClass}__row `+
                       `${baseClass}__row--collapsed`;
      }
    }else if(isLast){
          className =  `${baseClass}__row ${baseClass}__row--last`;
    }else{
          className =  `${baseClass}__row`;
    }
    return className;
  },

  getTable(data) {
    const list = _.map(data, (d, i)=>{
        return (
          <tr
            className={d.name===this.state.selectedCopyRepo.name ? 'selected':null}
            key={i}
            onClick={this.userSelectedCopyRepo.bind(this, d)}>
              <td>{d.name}</td>
              <td>{d.size}</td>
          </tr>
        )
      })
    return <table>
              <thead>
                <tr>
                  <th>CR name</th>
                  <th>size</th>
                </tr>
              </thead>
              <tbody>
                {
                  list
                }
              </tbody>
            </table>

  },

  userSelectedCopyRepo(repo) {
    this.setState({
      selectedCopyRepo: repo
    })
  },

  toggle(id, event) {
    if(!event.target.classList.contains('icon-delete')){
      let collapsed = this.state.collapsed;
      if(this.state.collapsed.indexOf(id) > -1){
        collapsed = _.remove(collapsed, (d)=>{ return d !== id });
      }else{
        collapsed.push(id);
      }

      this.setState({
        collapsed: collapsed
      })
    }
  }

});

export default CopyRepoBroker;
