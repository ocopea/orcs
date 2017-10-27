// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Select from 'react-select';
import styles from './styles-search-select.scss'

export default class SearchSelect extends React.Component{

  constructor(props){
    super(props);
    this.state = {
        value: "",
        options: this.props.options
    }
  }

  static propTypes = {
    options: React.PropTypes.arrayOf(React.PropTypes.shape({
      name: React.PropTypes.string.isRequired,
      id: React.PropTypes.string.isRequired,
      label: React.PropTypes.string.isRequired
    })).isRequired,
    className: React.PropTypes.string.isRequired,
    optionClassName: React.PropTypes.string.isRequired,
    placeholder: React.PropTypes.string,
    onChange: React.PropTypes.func
  }

  static defaultProps = {
    placeholder: "Search...",
    onChange: function() {}
  }

  getSelectOptions(){
    var options = this.props.options.map(option => {
      return(
        { value: option.name, label: option.name, id: option.id, className: this.props.optionClassName }
      )
    });

    return options;
  }

  onBlur(e) {
    this.props.onChange(this.state.value);
  }

  onChange(val) {
    const value = val.value === undefined ? null : val;
    this.setState({value:value});
    this.props.onChange(value);
  }

  render(){

    const { t } = this.props;

    return(
      <div className={styles.wrap}>
        <Select
          className={this.props.className}
          placeholder={this.props.placeholder}
          options={this.getSelectOptions()}
          onChange={this.onChange.bind(this)}
          value={this.state.value}
          filterOptions={this.filterOptions}
          clearable={false}
          autosize={false}
          onBlur={this.onBlur.bind(this)}/>
      </div>
    )
  }

}
