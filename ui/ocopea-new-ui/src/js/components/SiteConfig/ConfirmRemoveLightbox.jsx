import React from 'react';
import { LightBox, Button } from '..';
import styles from './styles-confirm-remove-lightbox.scss';

class ConfigRemoveDialog extends React.Component {
  render() {
    return(
      <div>
        <div className={styles.content}>
          Are you sure you want to remove {this.props.removeTarget}?
        </div>
        <div className={styles.footer}>
          <div className={styles.footer}>
            <Button
              onClick={this.props.onDismiss}
              text="cancel"
            />
            <Button
              onClick={this.props.onConfirmRemove}
              primary
              text="confirm"
            />
          </div>
        </div>
      </div>
    )
  }
}

const ConfirmRemoveLightbox = props => (
  <div className={styles.ConfirmRemoveLightbox}>
    <LightBox
      title="Confirm remove"
      component={
        <ConfigRemoveDialog
          onDismiss={props.onDismiss}
          onConfirmRemove={props.onConfirmRemove}
          removeTarget={props.removeTarget}
        />}
        width={500}
        height={200}
        onDismiss={props.onDismiss}
      />
    </div>
);

export default ConfirmRemoveLightbox;
