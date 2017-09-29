// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import logsTypes from '../../data/logs-types.js';
import $ from 'jquery';


var LogsItems = React.createClass({

  getItemClassName(status){
    switch (status) {
      case logsTypes.error:
        return `error`
        break;
      case logsTypes.success:
        return `success`
        break;
    }
  },

  render(){
    const that = this;
    return(
      <ul>
        {
          this.props.items.map((item, i)=>{
            let className = that.getItemClassName(item.status);
            return <li
                    key={i}
                    className={
                      `${this.props.className}__log `+
                      `${this.props.className}__log--${className}`
                    }>
                      <span>[{item.tags[0]}] </span>
                      <span className={`${this.props.className}__log__source-name-sys`}>
                        {new Date(item.timestamp).toString()}
                      </span>
                      <span>
                        <span className={`${this.props.className}__log--beginning`}>
                          {item.filter.beginning}
                        </span>
                        <span className={`${this.props.className}__log--heighlight`}>
                          {item.filter.heighlight}
                        </span>
                        <span className={`${this.props.className}__log--ending`}>
                          {item.filter.ending}
                        </span>
                      </span>
                   </li>
          })
        }
      </ul>
    )
  }
});

LogsItems.proptypes = {
  items: React.PropTypes.array.isRequired // <-------- [ { type: 'error', msg: 'example', timestamp: 0 } ]
}

export default LogsItems;

// <span className={`${this.props.className}__log__type`}>{item.type}: </span>
