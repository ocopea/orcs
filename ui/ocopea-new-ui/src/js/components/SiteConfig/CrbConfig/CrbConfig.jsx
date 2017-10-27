// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { LogoList } from '../..';
import AddCrbLightbox from './AddCrbLightbox.jsx';
import IconUnkown from '../../../../assets/images/site-config/icon-unknown.png';

import styles from './styles-crb-config.scss';

export default class CrbConfig extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      showAddCrbLightbox: false,
      crbs: [
        {
          name: 'DDVE',
          logo: IconUnkown,
        },
      ],
    };
  }

  render() {
    return (
      <div className={styles.CrbConfig}>
        <LogoList
          listItems={this.state.crbs}
          onAddNewClicked={() => this.setState({ showAddCrbLightbox: true })}
        />
        {
          this.state.showAddCrbLightbox ?
            <AddCrbLightbox
              onDismiss={() => this.setState({ showAddCrbLightbox: false })}
            /> :
            null
        }
      </div>
    );
  }
}
