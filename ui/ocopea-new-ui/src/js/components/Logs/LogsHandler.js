// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import { observable, autorun } from 'mobx';


class LogsHandler {

  websockets = []
  @observable filters = [];
  @observable allLogs = [];
  @observable filteredLogs = [];
  @observable keyword = '';
  @observable allFilters = [];
  @observable selectedFilters = [];

  receiveWebsockets(websockets) {
    this.websockets = websockets;
  }

  setAllLogsFilters(allFilters){
    this.allFilters = [...allFilters];
    allFilters.forEach(filter=>{
      this.filters[filter] = filter;
    });
    this.selectedFilters = allFilters;
  }

  seeAllLogsFilters(){
    this.setAllLogsFilters(this.allFilters);
    this.filteredLogs = this.getFilterLogs();
  }

  receiveLog(log){
    this.allLogs.push(log);
    this.filteredLogs = this.getFilterLogs();
  }

  toggleLogsFilter(filter){
    let selectedFilters = this.selectedFilters;
    let hasFilter = selectedFilters.indexOf(filter) > -1;
    if(hasFilter){
      _.remove(selectedFilters, f=>{return f===filter})
      this.selectedFilters = selectedFilters;
    }else{
      selectedFilters.push(filter);
      this.selectedFilters = selectedFilters;
    }
    this.filteredLogs = this.getFilterLogs();
  }

  addFilter(filter){
    if(this.selectedFilters.indexOf(filter) === -1){
      this.selectedFilters.push(filter);
    }
  }

  getFilterLogs(){
    let filteredLogs = [];
    this.allLogs.filter(log=>{
      if(this.shouldPushLog(log)){
        let handledLog = this.objectifyLog(log, null);
        filteredLogs.push(handledLog);
      }
    });
    return filteredLogs;
  }

  userSearchLogs(text){
    this.keyword = text;
    this.filteredLogs = this.getFilterLogs();
    this.highlightKeyWord(text);
  }

  highlightKeyWord(text){
    let filteredLogs = this.filteredLogs;
    filteredLogs.forEach(log=>{
      log = this.objectifyLog(log, text);
    });
  }

  objectifyLog(log){
    let logObj = {};
    let text = this.keyword.toUpperCase();
    let msg = log.message.toUpperCase();

    if(text && msg.indexOf(text) > -1){
      let startIndex = msg.indexOf(text);
      let endIndex = startIndex + text.length;
      let beginning = msg.substring(0, startIndex);
      let heighlight = msg.substring(startIndex, endIndex);
      let ending = msg.substring(endIndex);

      let obj = {
        beginning: log.message.substring(0, startIndex),
        heighlight: log.message.substring(startIndex, endIndex),
        ending: log.message.substring(endIndex),
        status:'success'
      };

      logObj = {...log, filter :obj};
    }else {
      var obj = {
        beginning: log.message,
        heighlight: '',
        ending: '',
        status:'success'
      };
      logObj = {...log, filter: obj};
    }
    return logObj;
  }

  logsFilterText(logMsg){
    if(logMsg){
      let cleanMsg = logMsg.toUpperCase();
      let cleanKeyWord = this.keyword.toUpperCase();
      return cleanMsg.indexOf(cleanKeyWord) > -1;
    }
    return false;
  }

  logsFilterTag(tags){
    let logs = [];
    let isValid = false;
    if(tags){
      tags.forEach(tag=>{
        if(this.selectedFilters.indexOf(tag) > -1){
          isValid = true;
        }
      });
    }
    return isValid;
  }

  shouldPushLog(log){
    return log.tags.length === 0 ||
           this.logsFilterTag(log.tags) &&
           this.logsFilterText(log.message);
  }

  setAllFilters(websockets){
    let uniqTags = [];
    _.forEach(websockets, (socket, index)=>{
      if(index === 0){
        uniqTags = [...new Set(socket.tags)];
      }else{
        uniqTags = [...new Set(uniqTags.concat(socket.tags))]
      }
    });
    this.setAllLogsFilters(uniqTags);
  }

  clearLogs(){
    this.allLogs.clear();
    this.filteredLogs.clear();
    this.keyword = '';
    this.allFilters.clear();
    this.selectedFilters.clear();
  }

}

const singleton = new LogsHandler;

autorun(()=>{
  // console.log(singleton.deploymentState)
})

export default singleton;
