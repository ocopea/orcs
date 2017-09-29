import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-logs.scss';
import LogsList from './LogsList.jsx';
import logsTypes from './log-types.js';
import Handler from './LogsHandler.js';
import $ from 'jquery';
import _ from 'lodash';


@observer
export default class Logs extends React.Component{

  constructor(props){
    super(props)
    Handler.clearLogs();
  }

  componentDidMount() {

    if(this.props.websockets){

      const that = this;
      let tags = [];

      this.props.websockets.forEach((socket, i)=>{
        let websocket = new WebSocket(socket.address);

        if(i === 0){
          tags = [...new Set(socket.tags)];
        }else{
          tags = [...new Set(tags.concat(socket.tags))]
        }

        that.listenToWS(websocket)
      });
      Handler.setAllLogsFilters(tags);
    }
  }

  userClickOnRemoveFilter(filter) {
    Handler.toggleLogsFilter(filter);
  }

  getFilters() {
    const data = this.props.data || {};
    const filters = Handler.allFilters || [];
    const selectedFilters = Handler.selectedFilters || [];
    return _.map(filters, (filter, i)=>{
      let isDisabled = selectedFilters.indexOf(filter) === -1;
      const baseClassName = styles.filter;
      let className = isDisabled ?
                      `${baseClassName} disabled` : baseClassName;
      return  <div
                key={i}
                className={className}
                onClick={this.userClickOnRemoveFilter.bind(this, filter)}
                title={filter}>
                  {filter}
              </div>
    });
  }

  seeAllfilters() {
    Handler.setAllFilters(this.props.websockets);
    Handler.seeAllLogsFilters();
  }

  componentWillUnmount() {
    this.shouldCloseWS = true;
  }

  shouldCloseWS = false

  listenToWS(websocket) {
    const that = this;
    // console.log(websocket)
    websocket.onmessage = (e) => {

      if(that.shouldCloseWS){
        Handler.clearLogs();
        websocket.close();
        return;
      }

      let response = JSON.parse(e.data);

      // temporary until status logic is resolved
      response.status = logsTypes.success;

      Handler.receiveLog(response)
    }
  }

  scrollTotop(){
    var scrollTop = $('.Logs__inside ul').height();
    var container = $('.Logs__inside');
    $(container).scrollTop(scrollTop);
  }

  getParsedFiltersOptions(filters){
    return _.map(filters, filter=>{
      return {value: filter, label: filter}
    })
  }

  componentWillReceiveProps(nextProps){
    this.scrollTotop();
  }

  userSearchLogs(e) {
    Handler.userSearchLogs(e.target.value)
  }

  render() {
    const seeAllfilters = this.seeAllfilters.bind(this);
    const filteredLogs = Handler.filteredLogs || [];

    return(
      <div className={styles.Logs}>
       <div className={styles.title}>logs</div>
       <div className={styles.filters}>
        {this.getFilters()}
        <input
          className={styles.elasticSearch}
          type="text" onChange={this.userSearchLogs} />
        <button
            onClick={seeAllfilters}
            className={styles.btnSeeAll}
            type="button">see all</button>
       </div>
       <div className={styles.inside}>
        <LogsList
          items={filteredLogs}
          className={styles.inside}/>
       </div>
      </div>
    )
  }

}
