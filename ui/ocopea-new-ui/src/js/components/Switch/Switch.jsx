import React from 'react';
// import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import _ from 'lodash';
import { observer } from 'mobx-react';


@observer
class SwitchBtn extends React.Component{

  getSwitchClass(isActive){
    return this.props.selectedElement.isActive ? 'on' : 'off'
  }

  userClickOnSwitch(){
    var type = this.props.selectedElement.componentType,
        id = type === 'service' ?
                      this.props.selectedElement.serviceId :
                      this.props.selectedElement.index,
        isActive = !this.props.selectedElement.isActive,
        options = {type: type, id: id, isActive: isActive}
    // if(id !== undefined){
    //   DevWizardActions.toggleSwitch(options);
    // }
    if(this.props.onSwitch){
      this.props.onSwitch();
    }
  }

  componentWillReceiveProps(){
    this.forceUpdate();
  }

  getClassName(className){
    return this.props.className === undefined ?
      className
      :
      `${className} ${this.props.className}`
  }

  render(){

    return(
      <div className={this.getClassName('Switch')}
          onClick={this.userClickOnSwitch.bind(this)}>
        <span
          className={this.getClassName(`Switch__dot Switch__dot__${this.getSwitchClass()}`)}>

            <div className={this.getClassName('Switch__dot__icon')}>
              <span className={this.getClassName('Switch__dot__icon__span')}></span>
              <span className={this.getClassName('Switch__dot__icon__span')}></span>
            </div>

        </span>
      </div>
    )
  }
};

export default SwitchBtn;
