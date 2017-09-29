// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../module-production/js/config.js';
import moment from 'moment';
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import SortableTable from '../../../../shared-components/js/sortable-table.comp.js';
var GeminiScrollbar = require('react-gemini-scrollbar');


var ImageOriginList = React.createClass({

  getTags: function(tags){
    var tagsElements = tags.map((tag, i)=>{
      var className = i === 0 ?
        "Image-origin-list__inside__table__tbody__tr__td__tags__tag " +
        "Image-origin-list__inside__table__tbody__tr__td__tags__tag--first" :
        "Image-origin-list__inside__table__tbody__tr__td__tags__tag";

      if(i < 3){
        return(
          <div
            key={i}
            className={className}
            title={tag.length > 5 ? tag : null}>
              {Config.getShortName(tag, 5)}
          </div>
        )
      }else{
        return <div
                key={i}
                className={"Image-origin-list__inside__table"+
                          "__tbody__tr__td__tags__see-more link"}>...</div>
      }

    });
    return tagsElements;
  },

  parseDate: function(date){    
    var day = moment(date).date(),
        month = moment()._locale._monthsShort[moment(date).month()],
        year = moment(date).year();

    return `${day} ${month} ${year}`
  },

  getHeader: function(){
    return [
      {
        'content':'owner',
        'onClick': DevDashboardActions.sortImageOriginByUserName.bind(this, 'owner'),
        'style': {'width': '25%'},
        'colSpan': 2
      },
      {
        'content':'name',
        'onClick': DevDashboardActions.sortImageOrignByImageName.bind(this, 'name'),
        'style': {'width': '20%'}
      },
      {
        'content':'created',
        'onClick': DevDashboardActions.sortImageOriginByDate.bind(this, 'created'),
        'style': {'width': '20%'}
      },
      {
        'content':'tag',
        'onClick': function(){},
        'style': {}
      }
    ]
  },

  getBody: function(){

    var rows = this.props.data.map((image, i)=>{

      var user = this.props.users[image.createdByUserId] == undefined ? {id:""} :
                 this.props.users[image.createdByUserId];
      var username = `${user.firstName} ${user.lastName}`;

      return {
        'tr': {},
        'td':[
          {
            'cell':{
              'content':<img
                          className="Image-origin-list__inside__table__tbody__tr__td__avatar"
                          src={APISERVER + `/hub-web-api/user/${user.id}/avatar`}/>
            }
          },
          {
            'cell':{
              'content': Config.getShortName(username, 15)
            }
          },
          {
            'cell':{
              'content': Config.getShortName(image.name, 15)
            }
          },
          {
            'cell':{
              'content': image.dateCreated !== undefined ?
                         this.parseDate(image.dateCreated) :
                         this.parseDate(image.creationTime)
            }
          },
          {
            'cell':{
              'content': this.getTags(image.tags)
            }
          }
        ]
      }
    });
    return rows
  },

  render: function(){
    // console.log(this.props.sortTableBy)
    return(
        <div className="Image-origin-list">

          <div className="Image-origin-list__title">
            <span className="Image-origin-list__title__span">image origin</span>
            <span
              className="close-btn"
              onClick={DevDashboardActions.hideImageOriginList}>
                <span></span>
                <span></span>
            </span>
          </div>

          <div className="Image-origin-list__inside">
          <GeminiScrollbar>

            <SortableTable
              header={this.getHeader()}
              body={this.getBody()}
              rootClassName='Image-origin-list__inside__table'
              sortBy={this.props.sortTableBy}
            />

          {/* /Image-origin-list__inside */}
          </GeminiScrollbar>
          </div>
        {/* /Image-origin-list */}
        </div>
    )
  }
});

export default ImageOriginList;
