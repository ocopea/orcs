import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-site-config-left-menu.scss';

@inject('stores')
@observer
export default class SiteConfigLeftMenu extends React.Component {
  static propTypes = {
  };

  render() {
    const { stores } = this.props;
    return (
      <div className={styles.SiteConfigLeftMenu}>
        <div className={styles.title}>site setup</div>
        <ul>
          {
            stores.data.sites.map((site) => {
              const selectedSiteId = this.props.selectedSite ? this.props.selectedSite.id : null;
              const classNames = selectedSiteId === site.id ?
                `${styles.listItem} ${styles.selected}` :
                styles.listItem;
              return (
                <li
                  className={classNames}
                  key={site.id}
                >
                  {site.name}
                </li>
              );
            })
          }
        </ul>
      </div>
    );
  }
}
