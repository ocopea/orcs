// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

//actions
import ProdActions from '../../../../module-production/js/actions/actions.js';
import DevActions from '../../actions/dev-actions.js';
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import SharedActions from '../../../../shared-actions.js';

//components
import Quotas from './quotas.comp.js';
import Filters from './filters.comp.js';
import Card from './card.comp.js';
import GeneralMenu from '../../../../shared-components/js/general-menu.comp.js';
import SortableTable from '../../../../shared-components/js/sortable-table.comp.js';
var GeminiScrollbar = require('react-gemini-scrollbar');

//assets
import MockImg from '../../../assets/images/main-screen-card/mock.png';
import OpenIcon from '../../../assets/images/open-icon.svg';

//scripts
import DevNavigationOptions from '../../data/devNavigationOptions.js';
import moment from 'moment';
import _ from 'lodash';
import $ from 'jquery';
import Isvg from 'react-inlinesvg';
import Config from '../../../../module-production/js/config.js';


var DevMainScreen = React.createClass({

  shouldComponentUpdate: function(nextProps){
    // console.log(this.props.filteredInstances)
      return this.props.filteredInstances !== nextProps.filteredInstances ||
             this.props.view !== nextProps.view ||
             this.props.applications !== nextProps.applications ||
             this.props.isLeftMenuRender !== nextProps.isLeftMenuRender ||
             this.props.isSortByMenuRender !== nextProps.isSortByMenuRender ||
             this.props.sortTableBy !== nextProps.sortTableBy;
  },

  componentDidMount: function(){
    ProdActions.openLeftMenu();
  },

  populateCards: function(){

      var cards = this.props.filteredInstances.map((instance, i)=>{
          return <Card
                      key={i}
                      instance={instance}
                      img={this.getAppTemplateImage(instance.appTemplateId)}
                      creator={this.props.users[instance.creatorUserId]}
                      filteredInstances={this.props.filteredInstances}/>

      });
      return cards;
  },

  parseDate: function(timeStamp){
    var date = new Date(timeStamp),
        day = moment(timeStamp).format('D'),
        month = date.getMonth(),
        monthName = moment()._locale._monthsShort[month],
        year = date.getFullYear(),
        fullDate = `${day} ${monthName} ${year}`;

    return fullDate;
  },

  getDsbRange: function(dsb){
    var values = _.values(dsb),
        min = _.min(values),
        max = _.max(values);

    if(min != max){
        return range = `${min}%-${max}%`
    }else{
        return `${min}%`
    }
  },

  getAppTemplateImage: function(appTemplateId){

    return this.props.applications[appTemplateId] !== undefined ?
          <img
            className="appTemplateIcon"
            src={this.props.applications[appTemplateId].img} /> :
          <img
            className="appTemplateIcon"
            src={MockImg} />;
  },

  goToDashboard: function(instance){
    DevDashboardActions.userClickOnMainScreenCard(instance);

    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.dashboard.location,
      subLocation: "/"+instance.id
    })

  },

  isSelectedSort: function(type){
    return type === this.props.instancesSortedBy ? 'selected' : undefined
  },

  getTableBody: function(){
    var that = this;
    var rows = this.props.filteredInstances.map((instance, i)=>{
      var creator = this.props.users[instance.creatorUserId];
      var appTemplateImage = this.getAppTemplateImage(instance.appTemplateId);
      return(
        {
          'tr': {'onClick':function(){that.goToDashboard(instance)}},
          'td':[
            {
              'cell':{
                'content': <div>
                            <img src={appTemplateImage.props.src} className="appTemplateIcon"/>
                              {Config.getShortName(instance.name, 15)}
                            </div>
              }
            },
            {
              'cell':{
                'content':`${creator.firstName} ${creator.lastName}`
              }
            },
            {
              'cell':{
                'content':this.parseDate(instance.dateCreated)
              }
            },
            {
              'cell':{
                'content': ""
              }
            },
            {
              'cell':{
                'content':   <div>
                               <span className="psb-quota">
                                  <span className="dot"></span>
                                  {instance.quota.psbQuota}%
                               </span>
                               <span className="dsb-quota">
                                  <span className="dot"></span>
                                  {this.getDsbRange(instance.quota.dsbQuota)}
                               </span>
                               <span><Isvg src={OpenIcon} className="open-icon"/></span>
                             </div>
             }
            }
          ]
        }
      )
    });

    return rows;
  },

  getTableHeader: function(){
    return [
      {
        'content': 'name',
        'style':{'width':'20%'},
        'onClick': DevActions.sortInstances.bind(this, 'name')
      },
      {
        'content': 'owner',
        'style':{'width':'15%'},
        'onClick': DevActions.sortInstances.bind(this, 'owner')
      },
      {
        'content': 'created',
        'style':{'width':'15%'},
        'onClick': DevActions.sortInstances.bind(this, 'created')
      },
      {
        'content': 'origin',
        'style':{'width':'15%'}
      },
      {
        'content': 'quota',
        'style':{'width':'35%'},
        'onClick': DevActions.sortInstances.bind(this, 'quota')
      }
    ]
  },

	render:function(){

		return(

			<div className="main-screen">

          <Quotas
              quotas={this.props.quotas}/>

          <div className="inside">
            <GeminiScrollbar>
              <Filters
                filters={this.props.allFilters}
                view={this.props.view}
                isSortByMenuRender={this.props.isSortByMenuRender}/>

              {
                this.props.isSortByMenuRender ?
                  <GeneralMenu
                    options={
                      [
                        {
                          text: 'owner',
                          img: null,
                          onClick: DevActions.sortInstances.bind(this, 'owner'),
                          itemClassName: this.isSelectedSort('user')
                        },
                        {
                          text: 'name',
                          img: null,
                          onClick: DevActions.sortInstances.bind(this, 'name'),
                          itemClassName: this.isSelectedSort('app type')
                        },
                        {
                          text: 'quota',
                          img: null,
                          onClick: DevActions.sortInstances.bind(this, 'quota'),
                          itemClassName: this.isSelectedSort('quota')
                        },
                        {
                          text: 'created',
                          img: null,
                          onClick: DevActions.sortInstances.bind(this, 'created'),
                          itemClassName: this.isSelectedSort('created')
                        }
                      ]
                    }
                    className="sort-by-menu"
                  />
                :
                null
              }

              <div className="cards-container">
                  {
                    this.props.view == 'grid' ?
                      this.populateCards()
                    :
                    this.props.view == 'list' ?
                      <SortableTable
                        header={this.getTableHeader()}
                        body={this.getTableBody()}
                        rootClassName='list-view-table'
                        sortBy={this.props.sortTableBy}
                      />
                    :
                    null
                  }
              {/* /card-container */}
              </div>
            </GeminiScrollbar>
          {/* /inside */}
          </div>
      {/* /main-screen */}
			</div>

		)

	}

});

export default DevMainScreen;
