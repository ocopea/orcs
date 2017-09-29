import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-sortable-table.scss';

export default class SortableTable extends React.Component{

  constructor(props){
    super(props);
    this.state = {sortKey: null, sortDir: 0};
  }

  static propTypes = {
    headers: React.PropTypes.arrayOf(React.PropTypes.shape({
      content: React.PropTypes.string.isRequired,
      key: React.PropTypes.string.isRequired
    })).isRequired,
    rows: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
    className: React.PropTypes.string,
    rowKey: React.PropTypes.string.isRequired
  }

  createHeader(header, i) {
    return (
        <th
          style={header.style !== undefined ? header.style : null}
          onClick={this.onHeaderClick.bind(this, header)}
          key={i}>
            {header.content}
            {this.createSortArrow(header)}
        </th>
    )
  }

  createSortArrow(header) {
    if ('sortKey' in header) {
      if (this.state.sortKey == header.sortKey) {
        if (this.state.sortDir == 1) {
          return (
            <span
              className="icon-arrow-sort"
            />
          )
        }
        if (this.state.sortDir == -1) {
          return (
            <span
              className="icon-arrow-sort"
              style={{transform: "rotateX(180deg)", display: "inline-block", transitionDuration: "0.8s"}}
            />
          )
        }
      }
      return (
        <span
          className="icon-arrow-sort"
          style={{visibility: "hidden"}}
        />
      )
    } else {
      return null;
    }
  }

  createRow(row) {
    return (
      <tr
        key={row[this.props.rowKey]}>
        {
          this.props.headers.map(header => {
            return <td
              style={row[header.key].style !== undefined ? row[header.key].style : null}
              onClick={this.onCellClick.bind(this, row[header.key])}
              key={header.key}>
                {row[header.key].content}
              </td>
          })
        }
      </tr>
    )
  }

  sort(rows) {
    const sortedRows = rows.slice();
    if (this.state.sortKey) {
      const sortKey = this.state.sortKey;
      const sortDir = this.state.sortDir;
      sortedRows.sort(function(a,b) {
        if (a[sortKey] > b[sortKey]) return 1*sortDir;
        if (a[sortKey] < b[sortKey]) return -1*sortDir;
        return 0;
      });
    }
    return sortedRows;
  }

  onHeaderClick(header, event) {
    if ('onClick' in header) {
      header.onClick(event);
    }
    if (!event.isDefaultPrevented() && 'sortKey' in header) {
      if (this.state.sortKey != header.sortKey) {
        this.setState({sortKey: header.sortKey, sortDir: 1});
      } else {
        if (this.state.sortDir == 1) {
          this.setState({sortDir: -1});
        } else if (this.state.sortDir == -1) {
          this.setState({sortDir: 0});
        } else {
          this.setState({sortDir: 1});
        }
      }
    }
  }

  onCellClick(cell, event) {
    if ('onClick' in cell) {
      cell.onClick(event);
    }
  }

  render(){

    const { t } = this.props;

    return(
      <table className={`${this.props.className} ${styles.table}`}>
        <thead>
          <tr>
            {this.props.headers.map((header, i) => this.createHeader(header, i))}
          </tr>
        </thead>
        <tbody>
          {this.sort(this.props.rows).map(row => this.createRow(row))}
        </tbody>
      </table>
    )
  }
}
