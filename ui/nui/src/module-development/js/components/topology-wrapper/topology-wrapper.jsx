import React from 'react';
import styles from './topology-wrapper.scss';
import { observer } from 'mobx-react';


@observer
export default class TopologyWrapper extends React.Component {

  render(){

    return(
      <div className="Topology-wrapper">
        <div className="Topology__container__title">
          <span className="Topology__container__title__span">{this.props.title}</span>
          <div className="Topology__container__title__legend">
            <div className="Topology__container__title__legend__section">
              <span className="app-i"></span>
              <span className="Topology__container__title__legend__label">Application</span>
            </div>
            <div className="Topology__container__title__legend__section">
              <span className="service-i"></span>
              <span className="Topology__container__title__legend__label">Service</span>
            </div>
          </div>
        </div>
        {this.props.component}
      </div>
    )
  }

  constructor(props) {
    super(props);
  }


};
