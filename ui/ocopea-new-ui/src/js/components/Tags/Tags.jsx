import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-tags.scss';
import Helper from '../../utils/helper.js';

class Tag extends React.Component {

  constructor(props) {
    super(props)
  }

  static propTypes = {
    text: React.PropTypes.string.isRequired,
    maxLength: React.PropTypes.number
  }

  static defaultProps = {
    maxLength: 0
  }

  render() {

    const { t } = this.props;

    const shortenText = this.props.maxLength > 0 && this.props.text.length > this.props.maxLength
    return(
      <div
        className={styles.tag}
        title={shortenText ? this.props.text : null}>
        {shortenText ? Helper.getShortName(this.props.text, this.props.maxLength) : this.props.text}
      </div>
    )
  }
}

class MoreTags extends React.Component {

  constructor(props) {
    super(props);
    this.state = {showTooltip: false};
    this.handleClickBoundMethod = this.handleClick.bind(this);
  }

  static propTypes = {
    tags: React.PropTypes.arrayOf(React.PropTypes.string).isRequired
  }

  showTagsTooltip() {
    this.setState({showTooltip: true});
    document.addEventListener('click', this.handleClickBoundMethod, false);
  }

  handleClick(event) {
    const tooltipDom = this.tooltip.domNode;
    if (tooltipDom != event.target && !tooltipDom.contains(event.target)) {
      document.removeEventListener('click', this.handleClickBoundMethod, false);
      this.setState({showTooltip: false});
    }
  }

  render() {

    const { t } = this.props;

    return(
      <div className={styles.moreTags}
        onClick={this.showTagsTooltip.bind(this)}>
        <span/>
        <span/>
        <span/>
        { this.state.showTooltip ?
            <TagsTooltip
              ref={(tooltip) => this.tooltip = tooltip}
              tags={this.props.tags}
              maxLength={4}/> :
            null
        }
      </div>
    )
  }
}

class TagsTooltip extends React.Component {

  constructor(props) {
    super(props)
  }

  static propTypes = {
    style: React.PropTypes.object,
    tags: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
    maxLength: React.PropTypes.number
  }

  render() {
    const { t } = this.props;

    const maxLength = this.props.maxLength;
    return (
      <div
        ref={(div) => this.domNode = div}
        className={styles.tagsTooltip}
        style={this.props.style}>
        <div className={styles.inside}>
          {this.props.tags.map((tag, i) => {
            return (
              <Tag
                key={i}
                text={tag}
                maxLength={maxLength}/>
            )
          })}
        </div>
      </div>
    )
  }
}

export default class Tags extends React.Component {

  constructor(props) {
    super(props)
  }

  static propTypes = {
    tags: React.PropTypes.arrayOf(React.PropTypes.string).isRequired
  }

  render() {
    const { t } = this.props;

    const tags = this.props.tags;
    return (
      <div>
        {tags.map((tag, i) => {
          if (i < 2) {
            return(
              <Tag
                key={i}
                text={tag}
                maxLength={4}
              />
            )
          } else if(i==2) {
            return(
              <MoreTags key={i} tags={tags.slice(2, tags.length)}/>
            )
          }
        })}
      </div>
    )
  }
}
