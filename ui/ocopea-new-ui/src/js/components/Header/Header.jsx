import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import DevHeader from './dev/DevHeader.jsx';
import ProdHeader from './prod/ProdHeader.jsx';
import modules from '../../utils/modules';


@inject(["stores"])
export default class Header extends React.Component{

  render(){

    const { t, module } = this.props;
    return(
      <div>
        {
          module === modules.production ?
            <ProdHeader onToggleSettingsMenu={this.props.onToggleSettingsMenu} />
          :
          module === modules.development ||
          module === modules.settings ?
            <DevHeader onToggleSettingsMenu={this.props.onToggleSettingsMenu} />
          :
              null
        }
      </div>
    )
  }

  static propTypes = {
    module: React.PropTypes.string.isRequired,
    loggedInUser: React.PropTypes.object
  };

  constructor(props){
    super(props)
  }

}
