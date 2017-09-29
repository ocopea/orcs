// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


var Menu = React.createClass({

  populateList: function(){
    var items = this.props.options.map((option, i)=>{
      var itemClassName = option.itemClassName !== undefined ? ` ${option.itemClassName}` : "";
      return(
        <li
          key={i}
          className={`General-menu-container__list__item ${this.props.className}${itemClassName}`}
          onClick={option.onClick}>
            {option.text}
        </li>
      )
    });
    return items;
  },

  render: function(){
    return(
      <div className={"General-menu-container general-menu "+this.props.className}>
        <ul className={`General-menu-container__list general-menu ${this.props.className}`}>
          {this.populateList()}
        </ul>
      </div>
    )
  }
});

Menu.propTypes = {
  options: React.PropTypes.array.isRequired,
  className: React.PropTypes.string.isRequired
}

export default Menu;
