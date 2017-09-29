// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DatePicker from 'react-date-picker';
require('react-date-picker/base.css');
require('react-date-picker/theme/default.css');
import BackupDetails from './backup.details.comp.js';
import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import _ from 'lodash';
import $ from 'jquery';


var Backup = React.createClass({

  componentDidMount: function(){
    this.getAllDaysWithEvents();
    //DevWizardActions.setImageBackupSelectedDate({})
  },

  onChange: function(dateString, moment, e){
    this.getAllDaysWithEvents();
    var isEventCell = e.target.parentElement.classList.contains('event');
    if(isEventCell){
      DevWizardActions.setImageBackupSelectedDate(moment);
    }
  },

  renderDay: function(e){

      this.props.image.backups.copies.forEach(backup=>{

        var backupTimeStamp = this.getMonthAndDayFromTimeStamp(backup.timestamp),
            renderedTimeStamp = this.getMonthAndDayFromTimeStamp(e.timestamp);

        if(_.isEqual(backupTimeStamp, renderedTimeStamp)){
            e.className += " event"
        }

      });

  },

  getAllDaysWithEvents: function(){
    var eventCell = $(".dp-cell.event"),
        eventSpan = $("<span class=event></span>");
    if($(eventCell).children()[1].className != 'event'){
        eventCell.append(eventSpan)
    }
  },

  getMonthAndDayFromTimeStamp: function(timestamp){
    var date = new Date(timestamp),
        day = date.getDate(),
        month = date.getMonth();

    return {day: day, month: month};
  },

  render: function(){
    // console.log(this.props.image.backups)
    return(
      <div className="backup">
        <DatePicker
            weekNumbers={false}
            renderDay={this.renderDay}
            onChange={this.onChange}/>

        <BackupDetails
          backups={this.props.image.backups}/>
      </div>
    )
  }
});

export default Backup;
