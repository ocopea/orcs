import React from 'react';
import { LogoList } from '../..';
import AddDsbLightbox from './AddDsbLightbox.jsx';
import IconUnkown from '../../../../assets/images/site-config/icon-unknown.png';

import styles from './styles-dsb-config.scss';

export default class DsbConfig extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      showAddDsbLightbox: false,
      dsbs: [
        {
          name: 'shpanblob-dsb',
          description: 'ShpanBlob DSB Reference Implementation',
          logo: IconUnkown,
        },
        {
          name: 'shpanblob-dup-dsb',
          description: 'ShpanBlob Duplicate DSB Reference Implementation',
          logo: IconUnkown,
        },
        {
          name: 'h2-dsb',
          description: 'H2 DSB Reference Implementation',
          logo: '/hub-web-api/images/dsb-protocol/postgres',
        },
      ],
    };
  }

  render() {
    return (
      <div className={styles.DsbConfig}>
        <LogoList
          listItems={this.state.dsbs}
          onAddNewClicked={() => this.setState({ showAddDsbLightbox: true })}
        />
        {
          this.state.showAddDsbLightbox ?
            <AddDsbLightbox
              onDismiss={() => this.setState({ showAddDsbLightbox: false })}
            /> :
            null
        }
      </div>
    );
  }
}
