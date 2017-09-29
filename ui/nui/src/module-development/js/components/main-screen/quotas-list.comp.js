// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../../actions/dev-actions.js';
import MockImg from '../../../assets/images/quotas/mock.png';
import { Line } from 'rc-progress';
import Config from '../../../../module-production/js/config.js';


var QuotasList = React.createClass({
	
	getQuotas: function(){
		
		var infraQuotas = this.props.quotas.map((quota, i)=>{
			
			return 	<tr key={i} title={quota.name}>
						<td width="70px">
							<div className="icon-container">
								<img src={MockImg} />
							</div>
						</td>
						<td width="70px">{Config.getShortName(quota.name)}</td>
						<td width="240px">
							
							<Line 
								percent={quota.precent}
								strokeWidth="2" 
								strokeColor="#7ed396"
								trailColor="#ebeff5" 
								trailWidth="2" 
								strokeLinecap="square"/>  									
									
						</td>
						<td width="40px">{quota.precent}%</td>
					</tr>
		});
		
		return infraQuotas;
		
	},
	
	render: function(){
		
		return(
		
			<div className="quotas-list" style={!this.props.isLeftMenuRender ? {left: 0} : null}>
			
				<div className="title">
					quota services
			
					<div 
						className="close-btn link"
						onClick={Actions.hideQuotasList}>
			
						<span></span>
						<span></span>
			
					</div>
				</div>
			
				<table>
			
					<tbody>
			
						{this.getQuotas()}
			
					</tbody>
				
				</table>
			
			</div>
		
		)
		
	}
	
});

export default QuotasList;