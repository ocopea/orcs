// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import d3 from 'd3';
import styles from './styles-progress-bar-count.scss';
import { observer } from 'mobx-react';


@observer
export default class ProgressBarCount extends React.Component{
  constructor(props) {
    super(props)
  }

  render(){

    var instanceName = this.props.instanceName;
    const done = this.props.done || [];
    const inProgress = this.props.inProgress || [];
    const count = done.length + inProgress.length;

    return(
      <div className={styles.ProgressBarCount}>
        <div className={styles.status}>
          {done.length}/{count}
        </div>
        <div
          className={styles.deployingMsg}
          title={this.props.instanceName}>
            Deploying {instanceName}
        </div>
      </div>
    );
  }

  componentWillReceiveProps(){
    this.initBar();
  }

  initBar(){

    d3.select(`.${styles.ProgressBarCount} svg`).remove();
    const doneCount = this.props.done || [];
    const inProgressCount = this.props.inProgress || [];

    var height = this.props.height,
        width = this.props.width;

    var scale = d3.scale.linear()
                .domain([0, doneCount.length + inProgressCount.length])
                .range([0, width]);

    var svg = d3.select(`.${styles.ProgressBarCount}`).append('svg')
                      .attr({
                        width: width,
                        height: height,
                        class: styles.container
                      });

    var range = svg.append('g').classed('range-container', true)
                  .append('rect')
                  .attr({
                    class: styles.range,
                    width: width,
                    height: height
                  });

    var done = svg.append('g').classed('done-container', true)
                .append('rect')
                .attr({
                  class: styles.done,
                  width: scale(this.props.done.length),
                  height: height
                });

  }

}

ProgressBarCount.propTypes = {
  done: React.PropTypes.array.isRequired,
  inProgress: React.PropTypes.array.isRequired,
  height: React.PropTypes.number.isRequired,
  width: React.PropTypes.number.isRequired,
  instanceName: React.PropTypes.string
}
