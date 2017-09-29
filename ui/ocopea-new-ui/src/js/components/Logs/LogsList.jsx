import React from 'react';
import $ from 'jquery';
import logsTypes from './log-types.js';
import { observer } from 'mobx-react';
import styles from './styles-logs.scss';


@observer
export default class LogsList extends React.Component{

  constructor(props){
    super(props)
  }

  getItemClassName(status){
    switch (status) {
      case logsTypes.error:
        return `error`
        break;
      case logsTypes.success:
        return `success`
        break;
    }
  }

  render(){
    const that = this;
    return(
      <ul>
        {
          this.props.items.map((item, i)=>{
            let className = that.getItemClassName(item.status);
            return <li
                    key={i}
                    className={styles.log}>
                      <span className={styles.tag}>[{item.tags[0]}] </span>
                      <span className={styles.sourceNameSys}>
                        {new Date(item.timestamp).toString()}
                      </span>
                      <span>
                        <span className="beginning">
                          {item.filter.beginning}
                        </span>
                        <span className="heighlight">
                          {item.filter.heighlight}
                        </span>
                        <span className="ending">
                          {item.filter.ending}
                        </span>
                      </span>
                   </li>
          })
        }
      </ul>
    )
  }
};

LogsList.proptypes = {
  items: React.PropTypes.array.isRequired // <-------- [ { type: 'error', msg: 'example', timestamp: 0 } ]
}
