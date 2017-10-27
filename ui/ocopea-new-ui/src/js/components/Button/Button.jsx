// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import styles from './styles-button.scss';

export default class Button extends React.Component {

  static propTypes = {
    primary: React.PropTypes.bool,
    onClick: React.PropTypes.func.isRequired,
    text: React.PropTypes.string.isRequired,
    iconClassName: React.PropTypes.string,
    iconColor: React.PropTypes.string,
    size: React.PropTypes.object
  };

  static defaultProps = {
    enabled: true,
    primary: false,
  }

  constructor(props) {
    super(props);
    this.state = {
      primary: props.primary,
      active: props.active,
    };
  }

  render() {
    let classes = [styles.Button];
    if (this.props.active) {
      classes.push(styles.active);
    } else {
      classes.push(styles.inactive);
    }

    if (this.props.primary) {
      classes.push(styles.primary);
    } else {
      classes.push(styles.secondary);
    }

    const classNames = classes.join(' ');
    return (
      <button
        onClick={this.props.onClick}
        className={`${classNames} Button`}
        style={this.props.size ?
          { width: this.props.size.width, height: this.props.size.height } : null
        }
      >
        {
          this.props.iconClassName ?
            <span
              style={this.props.iconColor ? {color: this.props.iconColor} : null}
              className={`${this.props.iconClassName} ${styles.icon}`}></span>
          : null
        }
        {this.props.text}
      </button>
    );
  }
}
