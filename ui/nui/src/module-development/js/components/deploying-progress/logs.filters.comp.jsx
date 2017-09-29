import React from 'react';


let LogsFilters = React.createClass({
  render() {
    return (
      <div className={this.props.className}>
        logs filter
      </div>
    )
  }
});

LogsFilters.propTypes = {
  filters: React.PropTypes.array.isRequired
}

export default LogsFilters;
