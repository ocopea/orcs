// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';


let CopyHistoryInlineError = React.createClass({
	
	render: function(){
		
		return (
				this.props.data.isRender ?
			
					<div id="copy-history-error">
						{this.props.data.error}
					</div>

					: 

					null
		)
	}
});

export default CopyHistoryInlineError;