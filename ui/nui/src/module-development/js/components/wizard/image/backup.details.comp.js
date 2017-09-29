// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import moment from 'moment';
import InfoIcon from '../../../../assets/images/wizard/image/info-icon.svg';
import Isvg from 'react-inlinesvg';


var BackupDetails = React.createClass({

  getSelectedDateRows: function(){
    var rows = this.props.backups.copies.map((backup, i)=>{
        var backupDate = new Date(backup.timestamp),
            selectedDate = new Date(this.props.backups.selectedDate.timestamp);

        if(this.compareDate(backupDate, selectedDate)){
          var min = backupDate.getMinutes()<10?'0'+backupDate.getMinutes():backupDate.getMinutes();
          return  <tr key={i}>
                    <td>{backupDate.getHours() + ":" + min}</td>
                    <td>{backup.size}</td>
                    <td>{backup.location}</td>
                    <td><Isvg src={InfoIcon} className="info-icon"/></td>
                  </tr>
        }
    });
    return rows;
  },

  compareDate: function(backupDate, selectedDate){
    var backupDay = backupDate.getDate(),
        backupMonth = backupDate.getMonth() + 1,
        selectedDateDay = selectedDate.getDate(),
        selectedDateMonth = selectedDate.getMonth() + 1;

    if(backupDay == selectedDateDay && backupMonth == selectedDateMonth){
      return true;
    }else{
      return false;
    }
  },

  render: function(){
    var date = {};
    if(!_.isEmpty(this.props.backups.selectedDate.dateMoment)){
      var selectedDate = moment(this.props.backups.selectedDate.dateMoment._d),
          weekday = selectedDate.weekday(),
          day = moment()._locale._weekdaysShort[weekday],
          dayInMonth = selectedDate.date(),
          month = moment()._locale._monthsShort[selectedDate.month()],
          year = selectedDate.year();
          date = {weekday: day, dayInMonth: dayInMonth, month: month, year: year }
    }else{
      date = {};
    }

    return(
      <div className="backup-details">
        <div className="title">
          {
            !_.isEmpty(date) ? date.weekday + "," +
                               date.dayInMonth + " " +
                               date.month + "," +
                               date.year : null
          }
        </div>
        <table>
          <thead>
            <tr>
              <th>time</th>
              <th>size</th>
              <th colSpan="2">location</th>
            </tr>
          </thead>
          <tbody>
            {this.getSelectedDateRows()}
          </tbody>
        </table>
      </div>

    )
  }
});

export default BackupDetails;
