// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import _ from 'lodash';
import moment from 'moment';
import DashboardActions from '../../actions/dev-dashboard-actions.js';
import Config from '../../../../module-production/js/config.js';
import stateOptions from '../../data/deploying-state-options.js';


var DashboardTitle = React.createClass({

  getCreatorAvatar: function(){
    if(this.props.creator !== undefined){
      return APISERVER + '/hub-web-api/user/' + this.props.creator.id + '/avatar';
    }else{
      return ""
    }
  },

  getCreatorName: function(){
    if(this.props.creator !== undefined){
      return `${this.props.creator.firstName} ${this.props.creator.lastName}`
    }else{
      return ""
    }
  },

  parseCreationDate: function(timeStamp){
    if(timeStamp !== undefined){
      var date = new Date(timeStamp),
          day = date.getDate(),
          month = moment(timeStamp)._locale._monthsShort[date.getMonth()],
          year = date.getFullYear(),
          hours = date.getHours(),
          minutes = date.getMinutes();

      return `${day} ${month} ${year} ${hours}:${minutes}`;
    }

  },

  userClickOnDeleteImage: function(){
    DashboardActions.showConfirmDisposeImageDialog();
  },

  getSaveImageBtnClassName: function(){
    const dashboard_title = 'Dashboard__title',
          inside = 'inside',
          section = 'section',
          right = 'right',
          button = 'button',
          link = 'link',
          first = 'first',
          element = '__',
          modifier = '--',
          space = ' ',
          disabled = 'disabled';

    let isLoading = false;

    var base = dashboard_title +
                element + inside +
                element + section +
                element + inside +
                modifier + right +
                element + button +
                space + link;

    if(this.props.isImageLoading){
      if(this.props.imageState.toUpperCase() !== stateOptions.created.toUpperCase()){
        isLoading = true;
      }else{
        isLoading = false;
      }
    }else{
      isLoading = false;
    }

    var disabledClass = disabled + space + base;

    return isLoading ?
              disabledClass : base;
  },

  render: function(){
    // console.log(this.getSaveImageBtnClassName())
    return(
      <div className="Dashboard__title">
        <div className="Dashboard__title__inside">
          <section className={'Dashboard__title__inside__section ' +
                              'Dashboard__title__inside__section--left'}>
            <div className={'Dashboard__title__inside__section__inside ' +
                            'Dashboard__title__inside__section__inside--left'}>
              <div className="Dashboard__title__inside__section__inside--left__icon-container">
                <img src={this.props.appTemplate.img}
                  className={'Dashboard__title__inside__section__inside'+
                             '--left__icon-container__image'}/>
                <div className={'Dashboard__title__inside__section__inside--left__icon-container__label ' +
                                'Dashboard__title__inside__section__inside--left__icon-container__label--instance-name'}
                     title={Config.getTitleOrNull(this.props.selectedInstance.name, 10)}>
                  {this.props.selectedInstance.name !== undefined  ?
                   Config.getShortName(this.props.selectedInstance.name, 10) : null}
                </div>
                <div className={'Dashboard__title__inside__section__inside--left__icon-container__label ' +
                                'Dashboard__title__inside__section__inside--left__icon-container__label--instance-version'}>
                  <span>version: </span>
                  <span>{this.props.appTemplate.version}</span>
                </div>
              </div>
            {/* /Dashboard__title__section__inside--left */}
            </div>
          {/* /Dashboard__title__section--left */}
          </section>
          <section className={'Dashboard__title__inside__section ' +
                              'Dashboard__title__inside__section--middle'}>
            <div className={'Dashboard__title__inside__section__inside ' +
                            'Dashboard__title__inside__section__inside--middle'}>
              <div className={'Dashboard__title__inside__section__inside--middle__container'}>
                <div className="Dashboard__title__inside__section__inside--middle__container--left">
                  <img src={this.getCreatorAvatar()}
                    className={'Dashboard__title__inside__section__inside'+
                               '--middle__container__image'}/>
                  <div className={'Dashboard__title__inside__section__inside'+
                                  '--middle__container__creator-name'}>
                    {this.getCreatorName()}
                  </div>
                  <div className={'Dashboard__title__inside__section__inside'+
                                  '--middle__container--left__date-created'}>
                    {this.parseCreationDate(this.props.selectedInstance.dateCreated)}
                  </div>
                {/* /Dashboard__title__section__inside--middle__container--left */}
                </div>
                <div className={'Dashboard__title__inside__section__inside'+
                                '--middle__container--right'}>
                  <div className={'Dashboard__title__inside__section__inside--'+
                                  'middle__container--right__size'}>size: </div>
                  <div className={'Dashboard__title__inside__section__inside'+
                                  '--middle__container--right__restore-time'}>restore time: </div>
                </div>
              </div>
            {/* /Dashboard__title__section__inside--middle */}
            </div>
          {/* /Dashboard__title__section--middle */}
          </section>
          <section className={'Dashboard__title__inside__section ' +
                              'Dashboard__title__inside__section--right'}>
            <div className={'Dashboard__title__inside__section__inside ' +
                            'Dashboard__title__inside__section__inside--right'}>
              <div className={this.getSaveImageBtnClassName()}
                   onClick={DashboardActions.showCreateImageDialog}>save as</div>
              <span className={'Dashboard__title__inside__section__inside--right__button'+
                              ' link icon-delete '+
                              'Dashboard__title__inside__section__inside--right__button--icon-delete'}
                    onClick={this.userClickOnDeleteImage}></span>
            {/* /Dashboard__title__section__inside--right */}
            </div>
          {/* /Dashboard__title__section--right */}
          </section>
        {/* /Dashboard__title__inside */}
        </div>
      {/* /Dashboard__title */}
      </div>
    )
  }
});

export default DashboardTitle;
