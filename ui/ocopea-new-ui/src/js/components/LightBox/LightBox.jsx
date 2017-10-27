// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-light-box.scss';
import { ButtonDismiss } from '../';
import Helper from '../../utils/helper.js';

const TITLE_MAX_LENGTH = 30;

@inject(['stores'])
@observer
export default class LightBox extends React.Component {

  static propTypes = {
    width: React.PropTypes.number.isRequired,
    height: React.PropTypes.number.isRequired,
    component: React.PropTypes.node.isRequired,
    title: React.PropTypes.string.isRequired,
    onDismiss: React.PropTypes.func,
  }

  constructor(props) {
    super(props);
    this.userClickOnDismiss = this.userClickOnDismiss.bind(this);
  }

  isLoading() {
    return this.props.stores.ui.pendingRequests.length > 0;
  }

  userClickOnDismiss() {
    this.props.stores.ui.showLightBox(false);
    if(this.props.onDismiss)
      this.props.onDismiss();
  }

  render() {

    const style = {};
    style.width = this.props.width;
    style.height = this.props.height;
    return (
      <div
        style={style}
        className={styles.lightBox}
      >
        {/* title */}
        <div className={styles.title}>
          <span title={Helper.getTitleOrNull(this.props.title, TITLE_MAX_LENGTH)}>
            {Helper.getShortName(this.props.title, TITLE_MAX_LENGTH)}
          </span>
          <ButtonDismiss
            style={{ float: 'right', width: 15, height: 15 }}
            onClick={this.userClickOnDismiss}
          />
        </div>
        {/* inside */}
        <div className={styles.inside}>
          { this.props.component }
        </div>
      </div>
    );
  }
}
