// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import { SearchSelect, SortableTable, Tags } from '../../../components';
import Helper from '../../../utils/helper.js';
import SavedImageHandler from '../../../models/SavedImage/savedImage-handler.js';
import styles from './styles-saved-images-list.scss';

@inject(["stores"])
@observer
export default class SavedImagesList extends React.Component{

  constructor(props){
    super(props)
    props.stores.ui.showMainMenu(false);
    this.state = {filterVal: null, tooltip: null};
  }

  onSelectChange(val) {
    this.setState({filterVal: val});
  }

  getTableHeaders() {
    return [
      {
        'content': '',
        'key': 'appTemplateImg',
        'style': {width: '11%'}
      },
      {
        'content': 'owner',
        'sortKey': 'firstName-uppercase',
        'key': 'owner',
        'style': {width: '20%'}
      },
      {
        'content': 'name',
        'sortKey': 'imageName-uppercase',
        'key': 'name',
        'style': {width: '15%'}
      },
      {
        'content': 'created',
        'sortKey': 'dateCreated',
        'key': 'dateCreated-display',
        'style': {width: '14%'}
      },
      {
        'content': 'tags',
        'key': 'tags',
        'style': {width: '20%'}
      },
      {
        'content': '',
        'key': 'icons',
        'style': {width: '20%'}
      }
    ]
  }

  getTableRows() {
    return this.props.stores.data.savedImages
      .filter(image => this.state.filterVal == null || this.state.filterVal.id == image.id)
      .map(image => {
        const user = this.props.stores.data.usersMap[image.createdByUserId];
        const templateImg = image.appTemplateId && this.props.stores.data.appTemplatesMap[image.appTemplateId] ?
            this.props.stores.data.appTemplatesMap[image.appTemplateId].img : '';
        return {
          'id': image.id,
          'firstName-uppercase': user !== undefined ? user.firstName.toUpperCase() : '',
          'imageName-uppercase': image.name.toUpperCase(),
          'dateCreated': image.creationTime,
          'dateCreated-display': {content: Helper.formatDate(image.creationTime).date},
          'name': {content: Helper.getShortName(image.name, 20)},
          'owner': {content: this.getOwnerCell(image, user)},
          'tags': {content: this.getTags(image)},
          'icons': {content: this.getIcons(image)},
          'appTemplateImg':{content:
            <img
              src={templateImg}
              className={styles.appTemplateImg}
            />
          }
        }
      })
  }

  getOwnerCell(image, user) {
    return (
      <div style={{position: 'relative'}}>
        <img
          src={APISERVER+`/hub-web-api/user/${image.createdByUserId}/avatar`}
          className={styles.avatar}
        />
        <div className={styles.username}>
          {user !== undefined ? Helper.getShortName(`${user.firstName} ${user.lastName}`, 15) : null}
        </div>
      </div>
    )
  }

  getTags(image) {
    return (
      <Tags tags={image.tags}/>
    )
  }

  getIcons(image) {
    return (
      <div className={styles.iconsContainer}>
        <span
          onClick={this.missingOperation.bind(this, 'image info')}
          className={`icon-info link ${styles.icon}`}></span>
        <span
          onClick={this.missingOperation.bind(this, 'image play')}
          className={`icon-play link ${styles.icon}`}></span>
        <span
          onClick={this.missingOperation.bind(this, 'image share')}
          className={`icon-share link ${styles.icon}`}></span>
        <span
          onClick={this.missingOperation.bind(this, 'image delete')}
          className={`icon-delete link ${styles.icon}`}></span>
      </div>
    )
  }

  missingOperation(op) {
    console.warn("missing operation '" + op + "'");
  }

  render(){

    const { t, stores } = this.props;
    const savedImages = stores.data.savedImages;

    return(
        <div className={styles.saved_images_pane}>
            <div className={styles.title}>
              saved images
            </div>
            <div className={styles.sub_title}>
              your saved images of all applications
            </div>
            <div className={styles.search}>
              <SearchSelect
                options={savedImages}
                className={styles.select}
                optionClassName={styles.option}
                placeholder='Search images...'
                onChange={this.onSelectChange.bind(this)}
                />
              <div className={styles.icon_search}>
                <span
                  className='icon-search'
                  onClick={this.focusSearchSelect}/>
              </div>
            </div>
              <SortableTable
                headers={this.getTableHeaders()}
                rows={this.getTableRows()}
                className={styles.table}
                rowKey='id'/>
            {
              this.state.tooltip ?
                <TagsTooltip
                  data={this.props.tooltip}
                />
              :
              null
            }
        {/* /Saved-images */}
        </div>
    )
  }

}
