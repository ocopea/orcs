// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../module-production/js/config.js';
import LogsList from './logs-list.comp.js';
import logsTypes from '../../data/logs-types.js';
import moment from 'moment';
import $ from 'jquery';
import DeployingProgressActions from '../../actions/deploying-progress-actions.js';


var Logs = React.createClass({

  getInitialState(){
    return {
      addMock: false
    }
  },

  componentDidMount() {

    if(this.props.websockets){

      const that = this;
      let tags = [];

      // MOCK WEBSOCKET remove after testing
      if(this.state.addMock){
        const mockTags = ['client-test', 'tag2', 'tag2', 'taaaaaaaaaag3', 'tagtagtag'];
        const mockAddress = {address: "ws://echo.websocket.org/", name: 'mock', tags: mockTags};
        this.props.websockets.push(mockAddress);
      }
      // ==================================

      this.props.websockets.forEach((socket, i)=>{
        let websocket = new WebSocket(socket.address);

        // MOCK WEBSOCKET remove after testing
        if(this.state.addMock && socket.name === 'mock'){
          this.emitToMockSocket(websocket);
        };
        if(this.state.addMock && !socket.name){
          socket.tags = ['server-test', 'tag4', 'tag5']
        };
        // ==================================

        if(i === 0){
          tags = [...new Set(socket.tags)];
        }else{
          tags = [...new Set(tags.concat(socket.tags))]
        }

        that.listenToWS(websocket)
      });
      DeployingProgressActions.setAllLogsFilters(tags);
    }
  },

  userClickOnRemoveFilter(filter) {
    DeployingProgressActions.toggleLogsFilter(filter);
  },

  getFilters() {
    return this.props.data.allFilters.map((filter, i)=>{
      let isDisabled = this.props.data.selectedFilters.indexOf(filter) === -1;
      const baseClassName = 'Logs__filters__filter';
      let className = isDisabled ?
                      `${baseClassName} ${baseClassName}--disalbed` : "Logs__filters__filter";
      return  <div
                key={i}
                className={className}
                onClick={this.userClickOnRemoveFilter.bind(this, filter)}
                title={Config.getTitleOrNull(filter, 15)}>
                  {Config.getShortName(filter, 15)}
              </div>
    });
  },

  seeAllfilters() {
    DeployingProgressActions.setAllFilters(this.props.websockets);
    DeployingProgressActions.seeAllLogsFilters();
  },

  // MOCK WEBSOCKET remove after testing
  emitToMockSocket(websocket) {
    const that = this;
    let count = 0;
    websocket.addEventListener('open', function (event) {
      (function foo(){
        // set timer
        let timer = setTimeout(foo, 1000);
        // condition for stopping
        let condition = false //count > 10;
        // content logs to display
        let content = {
          message: `test-${timer}`,
          tags: ['client-test'],
          status: logsTypes.success,
          timestamp: new Date().getTime()
        };
        // send content to mock server
        websocket.send(JSON.stringify(content));
        // stop emitting
        if(condition){
          clearTimeout(timer);
          return
        }
        count++;
      })();
    });
  },

  componentWillUnmount() {
    this.shouldCloseWS = true;
  },

  shouldCloseWS: false,

  listenToWS(websocket) {
    const that = this;
    // console.log(websocket)
    websocket.onmessage = (e) => {

      if(that.shouldCloseWS){
        // DeployingProgressActions.clearLogs();
        // websocket.close();
        return;
      }

      let response = JSON.parse(e.data);

      // temporary until status logic is resolved
      response.status = logsTypes.success;

      // MOCK WEBSOCKET remove after testing
      that.state.addMock && !response.tags ? response.tags = ['server-test'] : null
      // ==================================

      DeployingProgressActions.receiveLog(response)
    }
  },

  scrollTotop(){
    var scrollTop = $('.Logs__inside ul').height();
    var container = $('.Logs__inside');
    $(container).scrollTop(scrollTop);
  },

  getParsedFiltersOptions(filters){
    return _.map(filters, filter=>{
      return {value: filter, label: filter}
    })
  },

  componentDidUpdate(nextProps){
    this.scrollTotop();
  },

  userSearchLogs(e) {
    DeployingProgressActions.userSearchLogs(e.target.value)
  },

  render() {
    return(
      <div className="Logs">
       <div className="Logs__title">logs</div>
       <div className="Logs__filters">
        {this.getFilters()}
        <input className="Logs__filters__elastic-search" type="text" onChange={this.userSearchLogs} />
        <button
            onClick={this.seeAllfilters}
            className="Logs__filters__btn-see-all link"
            type="button">see all</button>
       </div>
       <div className="Logs__inside">
        <LogsList
          items={this.props.data.filteredLogs}
          className="Logs__inside"/>
       </div>
      </div>
    )
  }
});

Logs.propTypes = {
  filters: React.PropTypes.array.isRequired,
  data: React.PropTypes.object.isRequired
}

export default Logs;
