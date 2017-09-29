import _ from 'lodash';
import React from 'react';
import styles from './styles-tabs.scss';

export default class Tabs extends React.Component {

  static propTypes = {
    tabs: React.PropTypes.arrayOf(React.PropTypes.shape({
      key: React.PropTypes.string.isRequired,
      name: React.PropTypes.string.isRequired,
      icon: React.PropTypes.string.isRequired,
    })).isRequired,
    onTabSelected: React.PropTypes.func.isRequired,
  }

  constructor(props) {
    super(props);
    this.state = {
      selectedTabKey: props.tabs[0].key,
    };
  }

  onTabSelected(tab) {
    this.setState({ selectedTabKey: tab.key });
    this.props.onTabSelected(tab.key);
  }

  isSelected(tabKey) {
    return tabKey === this.state.selectedTabKey;
  }

  render() {
    return (
      <div className={styles.Tabs}>
        {
          _.map(this.props.tabs, (tab, key) =>
            (
              <button
                key={key}
                onClick={() => this.onTabSelected(tab)}
                className={
                  this.isSelected(tab.key) ?
                    `${styles.tab} ${styles.selected}` :
                    styles.tab
                }
              >
                <span className={`${tab.icon} ${styles.icon}`} />
                <span className={styles.name}>{tab.name}</span>
              </button>
            ))
        }
      </div>
    );
  }
}
