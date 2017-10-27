// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-restore-copy.scss';
import Select from 'react-select';
import { Button } from '../';
import { hashHistory } from 'react-router';
import _ from 'lodash';
import uuid from 'uuid';
import AppInstanceHandler from '../../models/AppInstance/appInstance-handler';


@inject(["stores"])
@observer
export default class RestoreCopy extends React.Component{

  constructor(props){
    super(props)
    const instance = this.props.instance;

    const purposes = [
      { label: 'test/dev' },
      { label: 'analytics' },
      { label: 'other' }
    ];

    const sites = [
      { label: 'dev space on cloud' },
      { label: 'local dev machine' },
      { label: 'durham esx2' }
    ]

    this.state = {
      instance: instance,
      appName: "",
      purposes: {
        options: purposes,
        selected: purposes[0]
      },
      sites: {
        options: sites,
        selected: sites[0]
      }
    };

  }

  static propTypes = {
    copy: React.PropTypes.object.isRequired
  }

  render(){
    const { copy } = this.props;
    const { instance, purposes, sites } = this.state;
    const data = [{value: 'a', label: 'a'}]
    const uniq = uuid().substring(0, 5);

    return(
      <div className={styles.RestoreCopy}>
        <section>
          <label>purpose</label>
          <Select
            searchable={false}
            clearable={false}
            options={purposes.options}
            onChange={this.onPurposeSelection.bind(this, purposes)}
            value={purposes.selected} />
        </section>
        <section>
          <label>app name</label>
          <input ref="inputAppName" onChange={this.onAppNameChange.bind(this)} className="input" defaultValue={`${instance.name}-${uniq}`} />
        </section>
        <section>
          <label>site</label>
          <Select
            searchable={false}
            clearable={false}
            options={sites.options}
            onChange={this.onSiteSelection.bind(this, sites)}
            value={sites.selected} />
        </section>
        <Button
          onClick={this.onRestore.bind(this, copy)}
          primary={true}
          text={"restore"}
          className={styles.btnSubmit} />
      </div>
    )
  }

  componentDidMount() {
    const defaultAppName = this.refs.inputAppName.value;
    this.setState({
      appName: defaultAppName
    })
  }

  onAppNameChange(e) {
    this.setState({
      appName: e.target.value
    })
  }

  onPurposeSelection(data, selected){
    this.setState({
      purposes: {
        options: data.options,
        selected: selected
      }
    })
  }

  onSiteSelection(data, selected){
    this.setState({
      sites: {
        options: data.options,
        selected: selected
      }
    })
  }

  onRestore(copy) {
    const appInstanceName = this.state.appName;
    const copyId = copy.copyId
    const originAppInstanceId = this.props.instance.id;
    const purpose = copy.purpose;
    const data = {
      appInstanceName: appInstanceName,
      copyId: copyId,
      originAppInstanceId: originAppInstanceId,
      purpose: purpose
    }
    AppInstanceHandler.repurposeCopy(data);
    this.props.stores.ui.showLightBox(false);
  }
}
