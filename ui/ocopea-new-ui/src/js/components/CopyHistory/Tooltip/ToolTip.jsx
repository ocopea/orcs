import React from 'react';
import { inject } from 'mobx-react';
import styles from './styles-tooltip.scss';
import { Button } from '../../';
import $ from 'jquery'

@inject(["stores"])
export default class ToolTip extends React.Component{
    constructor(props) {
      super(props)
      const mockSize = 10;
      const mockTimeToRestore = 22;
      this.state = {
        size: this.props.copy.size || mockSize,
        timeToRestore: this.props.copy.timeToRestore || mockTimeToRestore
      }

      $(document).click((e) => {
        const parents = $(e.target).parents();
        const tooltipClass = 'copy-history-tooltip';
        const isBackup = e.target.classList.contains('backup');

        let removeTooltip = false;
        parents.each((index, element) => {
          const tooltipClicked = element.classList.contains(tooltipClass);
          if(tooltipClicked) {
            removeTooltip = true;
            return;
          }
        });
        if(!removeTooltip && !isBackup)
          this.props.onRemoveTooltip();
      })

    }

    static propTypes = {
      copy: React.PropTypes.object.isRequired,
      position: React.PropTypes.object.isRequired
    }

    render() {
      const { copy } = this.props;
      return (
         <div className={`copy-history-tooltip ${styles.tooltip}`} style={this.getStyle()}>
            <div className={`section ${styles.copySizeContainer}`}>
							<div className={styles.tooltipTitle}>Copy size</div>
							<div className={styles.copySize}>{this.state.size} GB</div>
			      </div>

						<div className={`section ${styles.timeToRestoreContainer}`}>
							<div className={styles.tooltipTitle}>Estimated time to restore</div>
							<div className={styles.timeToRestore}>{this.state.timeToRestore} min</div>
						</div>

						<div className={`section ${styles.restoreBtnContainer}`}>
							<button
                  type="button"
                  id={styles.btnRestore}
                  className="button-primary"
                  onClick={this.userClickOnTooltipRestore.bind(this, copy)}>restore
              </button>
              <button
                type="button"
                className={`button-secondary ${styles.btnFailOver}`}
                onClick={this.clickOnFailOver}>fail over</button>
						</div>
          </div>
      )
    }

    getStyle(){
        const position = this.props.position || {};
        var style = {
            top: position.top + 70,
            left: position.left - 103,
        }

        return style;
    }

    userClickOnTooltipRestore(copy){
      const uiStore = this.props.stores.ui;
        uiStore.showLightBox(true, uiStore.dialogTypes.restoreCopy, '', copy)
    }

    clickOnFailOver() {
      const failover = true;
      // Actions.userClickOnTooltipRestore(failover);
    }

};
